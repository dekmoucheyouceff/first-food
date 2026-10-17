package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TabibMaklaDatabase
import com.example.data.model.FirestoreFollowerReview
import com.example.data.model.FirestoreRestaurant
import com.example.data.model.FollowerReview
import com.example.data.model.Restaurant
import com.example.data.repository.FirestoreFoodRepository
import com.example.data.repository.FoodRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    DOCTOR, // طبيب الماكلة (الناقد والمسؤول)
    FOLLOWER // متابع ومجتمع عشاق الأكل
}

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepository: FoodRepository
    private val firestoreRepository: FirestoreFoodRepository

    init {
        val db = TabibMaklaDatabase.getDatabase(application, viewModelScope)
        localRepository = FoodRepository(db.restaurantDao(), db.followerReviewDao())
        firestoreRepository = FirestoreFoodRepository(application)

        // Observe cloud restaurants and sync to local Room database in real-time
        viewModelScope.launch {
            try {
                firestoreRepository.observeRestaurants().collect { cloudRestaurants ->
                    if (cloudRestaurants.isNotEmpty()) {
                        val entities = cloudRestaurants.map { cr ->
                            Restaurant(
                                id = cr.id.hashCode().toLong().let { if (it <= 0) -it + 1000 else it },
                                name = cr.name,
                                category = cr.category,
                                city = cr.city,
                                address = cr.address,
                                phone = cr.phone,
                                priceRange = cr.priceRange,
                                doctorRating = cr.doctorRating.toFloat(),
                                foodTasteRating = cr.foodTasteRating.toFloat(),
                                cleanlinessRating = cr.cleanlinessRating.toFloat(),
                                serviceRating = cr.serviceRating.toFloat(),
                                valueRating = cr.valueRating.toFloat(),
                                ambianceRating = cr.ambianceRating.toFloat(),
                                doctorVerdict = cr.doctorVerdict,
                                recommendedDishes = cr.recommendedDishes,
                                doctorNotes = cr.doctorNotes,
                                isDoctorGoldChoice = cr.isDoctorGoldChoice,
                                isFavorite = false
                            )
                        }
                        entities.forEach { localRepository.addRestaurant(it) }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirestoreSync", "Cloud sync observation notice: ${e.message}")
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = Firebase.auth.currentUser

    private val _userRole = MutableStateFlow(UserRole.FOLLOWER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _onlyGoldChoice = MutableStateFlow(false)
    val onlyGoldChoice: StateFlow<Boolean> = _onlyGoldChoice.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    // Filtered restaurants feed
    val restaurants: StateFlow<List<Restaurant>> = combine(
        localRepository.allRestaurants,
        _searchQuery,
        _selectedCategory,
        _onlyGoldChoice,
        _onlyFavorites
    ) { all, query, category, goldOnly, favOnly ->
        all.filter { restaurant ->
            val matchesQuery = query.isBlank() ||
                    restaurant.name.contains(query, ignoreCase = true) ||
                    restaurant.city.contains(query, ignoreCase = true) ||
                    restaurant.category.contains(query, ignoreCase = true) ||
                    restaurant.recommendedDishes.contains(query, ignoreCase = true)

            val matchesCategory = category == "الكل" || restaurant.category == category
            val matchesGold = !goldOnly || restaurant.isDoctorGoldChoice
            val matchesFav = !favOnly || restaurant.isFavorite

            matchesQuery && matchesCategory && matchesGold && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRestaurantsCount: StateFlow<Int> = localRepository.totalRestaurantsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalReviewsCount: StateFlow<Int> = localRepository.totalReviewsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Currently viewed restaurant details
    private val _selectedRestaurantId = MutableStateFlow<Long?>(null)
    val selectedRestaurantId: StateFlow<Long?> = _selectedRestaurantId.asStateFlow()

    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurantId.flatMapLatest { id ->
        if (id != null) localRepository.getRestaurantById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedRestaurantReviews: StateFlow<List<FollowerReview>> = _selectedRestaurantId.flatMapLatest { id ->
        if (id != null) localRepository.getReviewsForRestaurant(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedRestaurantAvgRating: StateFlow<Float?> = _selectedRestaurantId.flatMapLatest { id ->
        if (id != null) localRepository.getAverageFollowerRating(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User interactions
    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }

    fun toggleUserRole() {
        _userRole.value = if (_userRole.value == UserRole.FOLLOWER) UserRole.DOCTOR else UserRole.FOLLOWER
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleGoldFilter() {
        _onlyGoldChoice.value = !_onlyGoldChoice.value
    }

    fun toggleFavoritesFilter() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun selectRestaurant(restaurantId: Long?) {
        _selectedRestaurantId.value = restaurantId
    }

    fun toggleFavorite(restaurant: Restaurant) {
        viewModelScope.launch {
            localRepository.toggleFavorite(restaurant.id, !restaurant.isFavorite)
        }
    }

    fun addRestaurant(
        name: String,
        category: String,
        city: String,
        address: String,
        phone: String,
        priceRange: String,
        doctorRating: Float,
        foodTasteRating: Float,
        cleanlinessRating: Float,
        serviceRating: Float,
        valueRating: Float,
        ambianceRating: Float,
        doctorVerdict: String,
        recommendedDishes: String,
        doctorNotes: String,
        isDoctorGoldChoice: Boolean,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val localRestaurant = Restaurant(
                name = name.trim(),
                category = category,
                city = city.trim(),
                address = address.trim(),
                phone = phone.trim(),
                priceRange = priceRange,
                doctorRating = doctorRating,
                foodTasteRating = foodTasteRating,
                cleanlinessRating = cleanlinessRating,
                serviceRating = serviceRating,
                valueRating = valueRating,
                ambianceRating = ambianceRating,
                doctorVerdict = doctorVerdict.trim(),
                recommendedDishes = recommendedDishes.trim(),
                doctorNotes = doctorNotes.trim(),
                isDoctorGoldChoice = isDoctorGoldChoice || (doctorRating >= 9.0f)
            )
            val newLocalId = localRepository.addRestaurant(localRestaurant)

            // Publish to Firestore Cloud
            val currentUid = Firebase.auth.currentUser?.uid ?: ""
            if (currentUid.isNotEmpty()) {
                val cloudItem = FirestoreRestaurant(
                    name = name.trim(),
                    category = category,
                    city = city.trim(),
                    address = address.trim(),
                    phone = phone.trim(),
                    priceRange = priceRange,
                    doctorRating = doctorRating.toDouble(),
                    foodTasteRating = foodTasteRating.toDouble(),
                    cleanlinessRating = cleanlinessRating.toDouble(),
                    serviceRating = serviceRating.toDouble(),
                    valueRating = valueRating.toDouble(),
                    ambianceRating = ambianceRating.toDouble(),
                    doctorVerdict = doctorVerdict.trim(),
                    recommendedDishes = recommendedDishes.trim(),
                    doctorNotes = doctorNotes.trim(),
                    isDoctorGoldChoice = isDoctorGoldChoice || (doctorRating >= 9.0f),
                    authorId = currentUid
                )
                firestoreRepository.addRestaurant(cloudItem)
            }

            onSuccess(newLocalId)
        }
    }

    fun addFollowerReview(
        restaurantId: Long,
        followerName: String,
        rating: Float,
        agreement: String,
        dishOrdered: String,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val review = FollowerReview(
                restaurantId = restaurantId,
                followerName = if (followerName.isNotBlank()) followerName.trim() else "عاشق للأكل",
                rating = rating,
                agreement = agreement,
                dishOrdered = dishOrdered.trim(),
                comment = comment.trim(),
                visitTime = "الآن"
            )
            localRepository.addFollowerReview(review)

            // Publish to Firestore Cloud
            val currentUid = Firebase.auth.currentUser?.uid ?: ""
            if (currentUid.isNotEmpty()) {
                val cloudReview = FirestoreFollowerReview(
                    restaurantId = restaurantId.toString(),
                    followerId = currentUid,
                    followerName = if (followerName.isNotBlank()) followerName.trim() else "عاشق للأكل",
                    rating = rating.toDouble(),
                    agreement = agreement,
                    dishOrdered = dishOrdered.trim(),
                    comment = comment.trim()
                )
                firestoreRepository.addReview(restaurantId.toString(), cloudReview)
            }

            onSuccess()
        }
    }

    fun markReviewHelpful(reviewId: Long) {
        viewModelScope.launch {
            localRepository.markReviewHelpful(reviewId)
        }
    }

    fun deleteRestaurant(restaurant: Restaurant, onDeleted: () -> Unit) {
        viewModelScope.launch {
            localRepository.deleteRestaurant(restaurant)
            if (_selectedRestaurantId.value == restaurant.id) {
                _selectedRestaurantId.value = null
            }
            onDeleted()
        }
    }
}
