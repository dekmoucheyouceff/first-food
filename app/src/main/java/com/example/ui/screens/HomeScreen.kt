package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.data.model.Restaurant
import com.example.ui.components.RestaurantCard
import com.example.ui.theme.BadgeDoctorGold
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandYellow
import com.example.ui.viewmodel.FoodViewModel
import com.example.ui.viewmodel.UserRole

@Composable
fun HomeScreen(
    viewModel: FoodViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddRestaurant: () -> Unit,
    onNavigateToAddReview: (Long?) -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val restaurants by viewModel.restaurants.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val onlyGoldChoice by viewModel.onlyGoldChoice.collectAsState()
    val onlyFavorites by viewModel.onlyFavorites.collectAsState()
    val totalCount by viewModel.totalRestaurantsCount.collectAsState()
    val totalReviews by viewModel.totalReviewsCount.collectAsState()

    val categories = FoodCategory.values().map { it.titleArabic }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_restaurant_list"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header: Doctor Branding & Mode Switcher
            item {
                HomeHeader(
                    userRole = userRole,
                    onToggleRole = { viewModel.toggleUserRole() },
                    totalRestaurants = totalCount,
                    totalReviews = totalReviews,
                    userEmail = viewModel.currentUser?.email ?: "",
                    onSignOut = onSignOut
                )
            }

            // Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_restaurant_input"),
                        placeholder = {
                            Text(
                                "ابحث عن مطعم، مدينة، صنف أو طبق...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Quick Filter Chips (Categories & Special Badges)
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Gold choice toggle
                        item {
                            FilterChip(
                                selected = onlyGoldChoice,
                                onClick = { viewModel.toggleGoldFilter() },
                                label = { Text("الوصفة الذهبية", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (onlyGoldChoice) Color.White else BadgeDoctorGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BadgeDoctorGold,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Favorites toggle
                        item {
                            FilterChip(
                                selected = onlyFavorites,
                                onClick = { viewModel.toggleFavoritesFilter() },
                                label = { Text("المفضلة", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (onlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (onlyFavorites) Color.White else Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD32F2F),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Food Categories
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Section Header: Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "الكل") "كل المطاعم المجربة" else "مطاعم $selectedCategory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${restaurants.size} مطعم",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Restaurant Cards Feed
            if (restaurants.isEmpty()) {
                item {
                    EmptyRestaurantsPlaceholder(
                        searchQuery = searchQuery,
                        userRole = userRole,
                        onAddRestaurant = onNavigateToAddRestaurant
                    )
                }
            } else {
                items(
                    items = restaurants,
                    key = { it.id }
                ) { restaurant ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onNavigateToDetail(restaurant.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(restaurant) }
                        )
                    }
                }
            }
        }

        // Floating Action Button based on User Mode
        ExtendedFloatingActionButton(
            onClick = {
                if (userRole == UserRole.DOCTOR) {
                    onNavigateToAddRestaurant()
                } else {
                    onNavigateToAddReview(null)
                }
            },
            icon = {
                Icon(
                    imageVector = if (userRole == UserRole.DOCTOR) Icons.Default.Add else Icons.Default.RateReview,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = if (userRole == UserRole.DOCTOR) "إضافة تقييم الطبيب" else "شارك تجربتك في مطعم",
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = if (userRole == UserRole.DOCTOR) BrandYellow else Color.Black,
            contentColor = if (userRole == UserRole.DOCTOR) Color.Black else BrandYellow,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("main_fab_button")
        )
    }
}

@Composable
fun HomeHeader(
    userRole: UserRole,
    onToggleRole: () -> Unit,
    totalRestaurants: Int,
    totalReviews: Int,
    userEmail: String = "",
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Brand Bar + Role Switcher + Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tabib_logo),
                        contentDescription = "شعار طبيب الماكلة",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "طبيب الماكلة 🩺",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (userEmail.isNotBlank()) "متصل: ${userEmail.take(18)}..." else "التشخيص الغذائي وتقييم المطاعم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Switcher Pill between Doctor & Follower
                    Surface(
                        onClick = onToggleRole,
                        shape = RoundedCornerShape(20.dp),
                        color = if (userRole == UserRole.DOCTOR) BrandYellow else BrandBlack,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandYellow),
                        modifier = Modifier.testTag("role_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (userRole == UserRole.DOCTOR) Icons.Default.LocalPharmacy else Icons.Default.People,
                                contentDescription = null,
                                tint = if (userRole == UserRole.DOCTOR) Color.Black else BrandYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userRole == UserRole.DOCTOR) "وضع الطبيب" else "وضع المتابع",
                                color = if (userRole == UserRole.DOCTOR) Color.Black else BrandYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle quote / greeting
            Text(
                text = if (userRole == UserRole.DOCTOR)
                    "أهلاً بك يا دكتور! يمكنك هنا إضافة المطاعم التي جربتها وتحديد تشخيصك الكامل وتقييم كل معيار."
                else
                    "مرحباً بك في مجتمع طبيب الماكلة! تصفح تقييمات الطبيب، وقارنها بتجارب المتابعين، وشاركنا رأيك الصادق في أي مطعم.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "مطاعم مجربة", value = "$totalRestaurants 🍽️")
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                StatItem(label = "تجارب المتابعين", value = "$totalReviews 💬")
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                StatItem(label = "تقييم تشريحي", value = "10/10 🩺")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyRestaurantsPlaceholder(
    searchQuery: String,
    userRole: UserRole,
    onAddRestaurant: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isNotEmpty())
                "لا توجد مطاعم مطابقة لبحثك: \"$searchQuery\""
            else
                "لم تتم إضافة مطاعم في هذا التصنيف بعد",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "يمكنك إضافة مطعم جديد ووضع تقييم طبيب الماكلة الرسمي له.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
