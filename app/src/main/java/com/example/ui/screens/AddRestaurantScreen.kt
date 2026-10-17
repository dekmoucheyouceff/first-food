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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalPharmacy
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.FoodCategory
import com.example.ui.components.InteractiveRatingPicker
import com.example.ui.theme.BadgeDoctorGold
import com.example.ui.viewmodel.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRestaurantScreen(
    viewModel: FoodViewModel,
    onBack: () -> Unit,
    onRestaurantAdded: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("مشاوي وشواية") }
    var city by remember { mutableStateOf("الجزائر العاصمة") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("$$") }

    // Doctor criteria
    var foodTasteRating by remember { mutableFloatStateOf(8.5f) }
    var cleanlinessRating by remember { mutableFloatStateOf(8.5f) }
    var valueRating by remember { mutableFloatStateOf(8.0f) }
    var serviceRating by remember { mutableFloatStateOf(8.0f) }
    var ambianceRating by remember { mutableFloatStateOf(8.0f) }

    var doctorVerdict by remember { mutableStateOf("") }
    var recommendedDishes by remember { mutableStateOf("") }
    var doctorNotes by remember { mutableStateOf("") }
    var isGoldChoice by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculate auto average
    val calculatedOverall = (foodTasteRating + cleanlinessRating + valueRating + serviceRating + ambianceRating) / 5f

    val availableCategories = FoodCategory.values().filter { it != FoodCategory.ALL }.map { it.titleArabic }
    val commonCities = listOf("الجزائر العاصمة", "وهران", "قسنطينة", "عنابة", "سطيف", "تيزي وزو", "بليدة", "باتنة")
    val priceOptions = listOf("$", "$$", "$$$")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة تقييم طبيب الماكلة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_restaurant_back")) {
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
            // Error banner if any
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

            // General Information Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "بيانات المطعم الأساسية 🍽️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; errorMessage = null },
                            label = { Text("اسم المطعم *") },
                            placeholder = { Text("مثال: شواية القصبة الأصيلة") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restaurant_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Category Chips
                        Text(
                            text = "تصنيف المطبخ:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableCategories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // City Quick Selection
                        Text(
                            text = "المدينة:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonCities.forEach { c ->
                                FilterChip(
                                    selected = city == c,
                                    onClick = { city = c },
                                    label = { Text(c, fontSize = 12.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("المدينة (أو اكتب مدينة أخرى)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("العنوان بالتفصيل أو المعلم الشهير") },
                            placeholder = { Text("مثال: حي القصبة، قرب ساحة الشهداء") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف للحجز (اختياري)") },
                            placeholder = { Text("مثال: 0550 12 34 56") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Price Range
                        Text(
                            text = "مستوى الأسعار:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            priceOptions.forEach { opt ->
                                val label = when (opt) {
                                    "$" -> "اقتصادي ($)"
                                    "$$" -> "متوسط ($$)"
                                    else -> "مرتفع ($$$)"
                                }
                                FilterChip(
                                    selected = priceRange == opt,
                                    onClick = { priceRange = opt },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Doctor Clinical Criteria Evaluation Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المعايير التشريحية لطبيب الماكلة 🔬",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Overall Calculated
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "المعدل: ${String.format("%.1f", calculatedOverall)} / 10",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        InteractiveRatingPicker(
                            label = "1. جودة ولذة الأكل والمكونات",
                            currentRating = foodTasteRating,
                            onRatingChanged = { foodTasteRating = it }
                        )

                        InteractiveRatingPicker(
                            label = "2. نظافة المطعم والمطبخ",
                            currentRating = cleanlinessRating,
                            onRatingChanged = { cleanlinessRating = it }
                        )

                        InteractiveRatingPicker(
                            label = "3. السعر مقابل القيمة المقدمة",
                            currentRating = valueRating,
                            onRatingChanged = { valueRating = it }
                        )

                        InteractiveRatingPicker(
                            label = "4. سرعة وجودة الخدمة وتعامل العمال",
                            currentRating = serviceRating,
                            onRatingChanged = { serviceRating = it }
                        )

                        InteractiveRatingPicker(
                            label = "5. الديكور والأجواء والراحة",
                            currentRating = ambianceRating,
                            onRatingChanged = { ambianceRating = it }
                        )
                    }
                }
            }

            // Doctor Verdict & Signature Dishes Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "التشخيص ووصفة الطبيب 📜",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = doctorVerdict,
                            onValueChange = { doctorVerdict = it },
                            label = { Text("تشخيص وخلاصة تقييم طبيب الماكلة *") },
                            placeholder = { Text("مثال: لحم طري مشوي على الحطب بنكهة أصلية وتتبيلة متوازنة جداً...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("doctor_verdict_input"),
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = recommendedDishes,
                            onValueChange = { recommendedDishes = it },
                            label = { Text("الأطباق الموصوفة طبياً (افصل بينها بفواصل) *") },
                            placeholder = { Text("مثال: شواء مشكل، مرقاز، سلطة مشوية") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recommended_dishes_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = doctorNotes,
                            onValueChange = { doctorNotes = it },
                            label = { Text("نصائح وتحذيرات الطبيب (اختياري)") },
                            placeholder = { Text("مثال: المكان يزدحم بعد 8 مساءً، يفضل الحجز المسبق") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doctor_notes_input"),
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Gold Choice Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "منح شهادة الوصفة الذهبية ⭐",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BadgeDoctorGold
                                )
                                Text(
                                    text = "تمنح للمطاعم الاستثنائية التي تفوقت في كل الاختبارات",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isGoldChoice || calculatedOverall >= 9.0f,
                                onCheckedChange = { isGoldChoice = it }
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "يرجى كتابة اسم المطعم"
                            return@Button
                        }
                        if (doctorVerdict.isBlank()) {
                            errorMessage = "يرجى كتابة تشخيص الطبيب وخلاصة التقييم"
                            return@Button
                        }

                        viewModel.addRestaurant(
                            name = name,
                            category = selectedCategory,
                            city = if (city.isNotBlank()) city else "الجزائر العاصمة",
                            address = if (address.isNotBlank()) address else "وسط المدينة",
                            phone = phone,
                            priceRange = priceRange,
                            doctorRating = calculatedOverall,
                            foodTasteRating = foodTasteRating,
                            cleanlinessRating = cleanlinessRating,
                            serviceRating = serviceRating,
                            valueRating = valueRating,
                            ambianceRating = ambianceRating,
                            doctorVerdict = doctorVerdict,
                            recommendedDishes = recommendedDishes,
                            doctorNotes = doctorNotes,
                            isDoctorGoldChoice = isGoldChoice || (calculatedOverall >= 9.0f),
                            onSuccess = { newId ->
                                onRestaurantAdded(newId)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_doctor_review_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نشر تقييم طبيب الماكلة الرسمي 🩺",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
