package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FirestoreFollowerReview
import com.example.data.model.FirestoreRestaurant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FirestoreFoodRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addRestaurant_authenticatedDoctor_createsDocument() = runBlocking {
        val uid = signInTestUser(DOCTOR_EMAIL)
        val repository = FirestoreFoodRepository(firestore)

        val restaurant = FirestoreRestaurant(
            name = "مطعم الاختبار " + UUID.randomUUID().toString().take(6),
            category = "مشاوي وشواية",
            city = "الجزائر العاصمة",
            doctorRating = 9.4,
            doctorVerdict = "تشخيص ممتاز وطعم أصيل",
            authorId = uid
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addRestaurant(restaurant)
        }

        assertTrue("Expected addRestaurant to succeed", result.isSuccess)
        val docId = result.getOrThrow()
        assertNotNull(docId)
    }

    @Test
    fun observeRestaurants_authenticatedUser_receivesEmissions() = runBlocking {
        val uid = signInTestUser(DOCTOR_EMAIL)
        val repository = FirestoreFoodRepository(firestore)

        val uniqueName = "مطعم مراقبة " + UUID.randomUUID().toString().take(6)
        val restaurant = FirestoreRestaurant(
            name = uniqueName,
            category = "أطباق تقليدية",
            city = "قسنطينة",
            doctorRating = 9.6,
            doctorVerdict = "شخشوخة أصلية ممتازة",
            authorId = uid
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addRestaurant(restaurant)
        }
        val docId = createResult.getOrThrow()

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRestaurants().first { items -> items.any { it.id == docId } }
        }

        assertTrue("List should contain created restaurant", list.any { it.id == docId })
    }

    @Test
    fun addReview_authenticatedFollower_succeeds() = runBlocking {
        val docUid = signInTestUser(DOCTOR_EMAIL)
        val repository = FirestoreFoodRepository(firestore)

        val restaurant = FirestoreRestaurant(
            name = "مطعم برجر الاختبار",
            category = "برجر وبيتزا",
            city = "وهران",
            doctorRating = 8.8,
            doctorVerdict = "سماش برجر محترف",
            authorId = docUid
        )
        val restId = repository.addRestaurant(restaurant).getOrThrow()

        // Switch user to follower
        val followerUid = signInTestUser(FOLLOWER_EMAIL)
        val review = FirestoreFollowerReview(
            restaurantId = restId,
            followerId = followerUid,
            followerName = "أمين الوهراني",
            rating = 9.0,
            agreement = "متفق تماماً مع الطبيب",
            comment = "طعم خيالي وسرعة في التقديم"
        )

        val reviewResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addReview(restId, review)
        }
        assertTrue("Follower review should succeed", reviewResult.isSuccess)
    }

    @Test
    fun addRestaurant_unauthenticatedUser_failsFast() = runBlocking {
        auth.signOut()
        val repository = FirestoreFoodRepository(firestore)

        val restaurant = FirestoreRestaurant(
            name = "مطعم غير مصرح",
            category = "مشاوي",
            city = "الجزائر",
            doctorRating = 8.0,
            doctorVerdict = "فحص"
        )

        val result = repository.addRestaurant(restaurant)
        assertTrue("Unauthenticated add should fail", result.isFailure)
    }

    private companion object {
        const val DOCTOR_EMAIL = "doctor@tabibelmakla.com"
        const val FOLLOWER_EMAIL = "follower@tabibelmakla.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
