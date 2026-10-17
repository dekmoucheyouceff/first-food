package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.model.FirestoreFollowerReview
import com.example.data.model.FirestoreRestaurant
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFoodRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول قبل إجراء العمليات السحابية")
    }

    // Real-time observation of restaurants
    fun observeRestaurants(): Flow<List<FirestoreRestaurant>> = flow {
        val path = "restaurants"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreRestaurant::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // Real-time observation of follower reviews for a restaurant
    fun observeReviews(restaurantId: String): Flow<List<FirestoreFollowerReview>> = flow {
        val path = "restaurants/$restaurantId/reviews"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFollowerReview::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // Add new restaurant by Doctor
    suspend fun addRestaurant(restaurant: FirestoreRestaurant): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val collectionRef = db.collection("restaurants")
        val docRef = collectionRef.document()
        val payload = restaurant.toMap().toMutableMap().apply {
            put("authorId", uid)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // Add review by Follower
    suspend fun addReview(restaurantId: String, review: FirestoreFollowerReview): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val collectionRef = db.collection("restaurants").document(restaurantId).collection("reviews")
        val docRef = collectionRef.document()
        val payload = review.toMap().toMutableMap().apply {
            put("restaurantId", restaurantId)
            put("followerId", uid)
            put("helpfulCount", 0L)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }

        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // Increment helpful count on review
    suspend fun incrementHelpful(restaurantId: String, reviewId: String): Result<Unit> {
        val docRef = db.collection("restaurants").document(restaurantId).collection("reviews").document(reviewId)
        return try {
            docRef.update("helpfulCount", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // Delete restaurant (author only)
    suspend fun deleteRestaurant(restaurantId: String): Result<Unit> {
        val docRef = db.collection("restaurants").document(restaurantId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
