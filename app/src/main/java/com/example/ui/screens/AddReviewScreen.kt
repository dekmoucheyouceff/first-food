package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveRatingPicker
import com.example.ui.theme.GourmetSage
import com.example.ui.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReviewScreen(
    initialRestaurantId: Long?,
    viewModel: FoodViewModel,
    onBack: () -> Unit,
    onReviewAdded: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allRestaurants by viewModel.restaurants.collectAsState()

    var selectedRestaurantId by remember {
        mutableLongStateOf(initialRestaurantId ?: allRestaurants.firstOrNull()?.id ?: 0L)
    }

    var followerName by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(9.0f) }
    var agreement by remember { mutableStateOf("متفق تماماً مع الطبيب") }
    var dishOrdered by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val agreementOptions = listOf(
        "متفق تماماً مع الطبيب",
        "متفق نوعاً ما",
        "أرى أن التقييم مبالغ فيه",
        "يستحق أكثر مما قال الطبيب"
    )

    val currentSelectedRestaurant = allRestaurants.find { it.id == selectedRestaurantId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = GourmetSage,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشاركة تجربة متابع",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_review_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Restaurant Selection / Confirmation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "المطعم الذي جربته 🍽️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GourmetSage
                        )

                        if (initialRestaurantId != null && currentSelectedRestaurant != null) {
                            // Pre-selected restaurant
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentSelectedRestaurant.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${currentSelectedRestaurant.city} - ${currentSelectedRestaurant.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "طبيب: ${String.format("%.1f", currentSelectedRestaurant.doctorRating)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Selection Chips
                            Text(
                                text = "اختر المطعم من القائمة:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allRestaurants.forEach { rest ->
                                    FilterChip(
                                        selected = selectedRestaurantId == rest.id,
                                        onClick = { selectedRestaurantId = rest.id },
                                        label = { Text(rest.name, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Restaurant,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GourmetSage,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Follower Identity & Rating Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "بيانات تجربتك الشخصية 📝",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GourmetSage
                        )

                        // Follower Name
                        OutlinedTextField(
                            value = followerName,
                            onValueChange = { followerName = it; errorMessage = null },
                            label = { Text("اسمك أو لقبك كمتابع *") },
                            placeholder = { Text("مثال: عبد الرؤوف العاصمي") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("follower_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Follower Rating
                        InteractiveRatingPicker(
                            label = "تقييمك الإجمالي للتجربة",
                            currentRating = rating,
                            onRatingChanged = { rating = it }
                        )

                        // Agreement with Doctor
                        Text(
                            text = "هل تتفق مع تشخيص طبيب الماكلة لهذا المطعم؟",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            agreementOptions.forEach { opt ->
                                FilterChip(
                                    selected = agreement == opt,
                                    onClick = { agreement = opt },
                                    label = { Text(opt, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GourmetSage,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Dish Ordered
                        OutlinedTextField(
                            value = dishOrdered,
                            onValueChange = { dishOrdered = it },
                            label = { Text("الطبق أو الوجبة التي طلبتها *") },
                            placeholder = { Text("مثال: طبق مشاوي مشكل + بطاطا") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("follower_dish_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Comment Body
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it; errorMessage = null },
                            label = { Text("نص تجربتك ورأيك الصادق بالتفصيل *") },
                            placeholder = { Text("صف لنا الطعم، النظافة، وقت الانتظار، السعر، وهل تنصح المتابعين بزيارته...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("follower_comment_input"),
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (selectedRestaurantId == 0L) {
                            errorMessage = "يرجى اختيار المطعم أولاً"
                            return@Button
                        }
                        if (followerName.isBlank()) {
                            errorMessage = "يرجى إدخال اسمك أو لقبك"
                            return@Button
                        }
                        if (comment.isBlank()) {
                            errorMessage = "يرجى كتابة نص التجربة ورأيك"
                            return@Button
                        }

                        viewModel.addFollowerReview(
                            restaurantId = selectedRestaurantId,
                            followerName = followerName,
                            rating = rating,
                            agreement = agreement,
                            dishOrdered = if (dishOrdered.isNotBlank()) dishOrdered else "وجبة كاملة",
                            comment = comment,
                            onSuccess = {
                                onReviewAdded(selectedRestaurantId)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_follower_review_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GourmetSage),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نشر تجربتي في عيادة طبيب الماكلة 🚀",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
