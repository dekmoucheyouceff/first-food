package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FollowerReview
import com.example.data.model.Restaurant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Restaurant::class, FollowerReview::class],
    version = 1,
    exportSchema = false
)
abstract class TabibMaklaDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao
    abstract fun followerReviewDao(): FollowerReviewDao

    companion object {
        @Volatile
        private var INSTANCE: TabibMaklaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TabibMaklaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TabibMaklaDatabase::class.java,
                    "tabib_makla_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.restaurantDao(), database.followerReviewDao())
                }
            }
        }

        private suspend fun populateInitialData(
            restaurantDao: RestaurantDao,
            reviewDao: FollowerReviewDao
        ) {
            // Seed sample restaurants reviewed by Tabib El Makla
            val r1 = Restaurant(
                id = 1,
                name = "شواية القصبة الأصيلة",
                category = "مشاوي وشواية",
                city = "الجزائر العاصمة",
                address = "حي القصبة العتيق، قرب ساحة الشهداء",
                phone = "0550 12 34 56",
                priceRange = "$$",
                doctorRating = 9.4f,
                foodTasteRating = 9.8f,
                cleanlinessRating = 9.0f,
                serviceRating = 9.2f,
                valueRating = 9.5f,
                ambianceRating = 9.5f,
                doctorVerdict = "تشخيص طبيب الماكلة: لحم طري يذوب في الفم، شوي على الحطب الصافي مع تتبيلة تبرز طعم اللحم بدون بهارات زائدة. المرقاز تحفة فنية حقيقية!",
                recommendedDishes = "شواء مشكل غنمي، مرقاز القصبة، سلاطة مشوية مشحرشة",
                doctorNotes = "المحل صغير ومزدحم وقت الظهيرة، ننصح بالمجيء قبل الساعة الواحدة أو الحجز مسبقاً. الخبز التقليدي يقدم ساخناً ومقرمشاً.",
                isDoctorGoldChoice = true,
                isFavorite = true
            )

            val r2 = Restaurant(
                id = 2,
                name = "برجر بارك كيتشن",
                category = "برجر وبيتزا",
                city = "وهران",
                address = "شارع الأمير عبد القادر، وسط المدينة",
                phone = "0770 98 76 54",
                priceRange = "$$",
                doctorRating = 8.8f,
                foodTasteRating = 9.1f,
                cleanlinessRating = 9.2f,
                serviceRating = 8.5f,
                valueRating = 8.4f,
                ambianceRating = 8.8f,
                doctorVerdict = "تشخيص طبيب الماكلة: سماش برجر محترف بقشرة مقرمشة (Crust) ممتازة، الخبز البريوش خفيف وهش. صوص الترافل يعطي لمسة فخمة بدون إزعاج للسان.",
                recommendedDishes = "دبل سماش بيكون مدخن، بطاطا تشيزي هلابينو، صوص الثومية المنزلية",
                doctorNotes = "البطاطا المقرمشة متبلة ببهارات الكاجون. السعر مرتفع قليلاً مقارنة بالحجم لكن الجودة تشفع له.",
                isDoctorGoldChoice = false,
                isFavorite = false
            )

            val r3 = Restaurant(
                id = 3,
                name = "دار الكسكسي والشخشوخة العريقة",
                category = "أطباق تقليدية",
                city = "قسنطينة",
                address = "طريق السويقة، المدينة القديمة",
                phone = "0661 45 67 89",
                priceRange = "$$",
                doctorRating = 9.6f,
                foodTasteRating = 9.9f,
                cleanlinessRating = 9.4f,
                serviceRating = 9.3f,
                valueRating = 9.6f,
                ambianceRating = 9.8f,
                doctorVerdict = "تشخيص طبيب المaكلة: شخشوخة قسنطينية ظفر أصلية باللحم العلوش، مرق معسل ولذيذ مع بهار رأس الحانوت المعتق. المكان ينقلك للزمن الجميل.",
                recommendedDishes = "شخشوخة قسنطينية باللحم، كسكسي بالمرقة البيضاء والقرفة، طاجين العين بالفواكه الجافة",
                doctorNotes = "وصفة علاجية لأي شخص يبحث عن الدفء والطعم الجزائري الأصيل. خدمة محترمة وضيافة شاي بالنعناع بعد الوجبة.",
                isDoctorGoldChoice = true,
                isFavorite = true
            )

            val r4 = Restaurant(
                id = 4,
                name = "سمك الميناء الأزرق",
                category = "مأكولات بحرية",
                city = "عنابة",
                address = "الكورنيش الجديد، رصيف الميناء",
                phone = "0561 22 33 44",
                priceRange = "$$$",
                doctorRating = 9.1f,
                foodTasteRating = 9.4f,
                cleanlinessRating = 9.1f,
                serviceRating = 8.8f,
                valueRating = 8.5f,
                ambianceRating = 9.7f,
                doctorVerdict = "تشخيص طبيب الماكلة: سمك طازج يصطاد في الصباح ويطهى أمامك. السردين المشوي على الجمر لا يعلى عليه، والجمبري بصلصة الثوم والليمون لا يقاوم.",
                recommendedDishes = "سمك دوراد مشوي، روبيان سوتيه بالزبدة والليمون، شوربة ثمار البحر",
                doctorNotes = "إطلالة بانورامية مباشرة على البحر المتوسط. الأسعار تختلف بحسب وزن الصيد اليومي، استفسر عن السعر قبل الطلب.",
                isDoctorGoldChoice = true,
                isFavorite = false
            )

            val r5 = Restaurant(
                id = 5,
                name = "محلبة ومطعم الفارس الشعبي",
                category = "أكل شوارع وشعبي",
                city = "الجزائر العاصمة",
                address = "شارع حسيبة بن بوعلي، رويسو",
                phone = "0540 88 99 00",
                priceRange = "$",
                doctorRating = 8.7f,
                foodTasteRating = 9.0f,
                cleanlinessRating = 8.3f,
                serviceRating = 8.6f,
                valueRating = 9.8f,
                ambianceRating = 7.8f,
                doctorVerdict = "تشخيص طبيب الماكلة: كرانتيكا وهران سخونة بالكمون والهريسة في الخبز الباغيت المقرمش مع مشروب سيلكتو بارد، وصفة السعادة الشعبية بأقل تكلفة!",
                recommendedDishes = "كرانتيكا دوبل بالبيض والهريسة، ساندويتش كبدة مشرملة، قلب اللوز العاصمي",
                doctorNotes = "المطعم شعبي جداً وبسيط، لا تتوقع ديكوراً فخماً ولكن النكهة أصيلة وشعبية حقيقية وتستحق الزيارة.",
                isDoctorGoldChoice = false,
                isFavorite = false
            )

            restaurantDao.insertAll(listOf(r1, r2, r3, r4, r5))

            // Seed follower experiences
            val reviews = listOf(
                FollowerReview(
                    restaurantId = 1,
                    followerName = "سفيان الجزائري",
                    rating = 9.5f,
                    agreement = "متفق تماماً مع الطبيب",
                    dishOrdered = "طبق شواء مشكل + مرقاز",
                    comment = "والله يا دكتور تشخيصك في محله! اللحم طري وزبدة ما تشبعش منو، ونظافة ممتازة مقارنة بمحلات الشواية. شكراً على النصيحة.",
                    visitTime = "منذ يومين",
                    helpfulCount = 14
                ),
                FollowerReview(
                    restaurantId = 1,
                    followerName = "مريم كمال",
                    rating = 8.8f,
                    agreement = "متفق تماماً مع الطبيب",
                    dishOrdered = "مرقاز مع سلطة مشوية",
                    comment = "طعم خيالي خاصة المرقاز مع الهريسة، المشكلة الوحيدة الانتظار أخذ نصف ساعة لأن المحل كان مليان.",
                    visitTime = "منذ 4 أيام",
                    helpfulCount = 7
                ),
                FollowerReview(
                    restaurantId = 2,
                    followerName = "أمين من وهران",
                    rating = 9.0f,
                    agreement = "يستحق أكثر مما قال الطبيب",
                    dishOrdered = "دبل سماش ترافل برجر",
                    comment = "أحسن برجر ذقته في وهران بلا منازع! الخبز طري جدا واللحم كريسبي من الأطراف. أنصح بتجربة صوص الهلابينو الخاص بهم.",
                    visitTime = "أمس",
                    helpfulCount = 9
                ),
                FollowerReview(
                    restaurantId = 3,
                    followerName = "حسام القسنطيني",
                    rating = 10.0f,
                    agreement = "متفق تماماً مع الطبيب",
                    dishOrdered = "شخشوخة ظفر بلحم العلوش",
                    comment = "أنا ولد المدينة ونشهد أن مطبخهم يطبخ بنكهة أمهاتنا وجداتنا. المرق ثقيل ومتبل صح. تشخيص طبيب الماكلة 100% دقيق!",
                    visitTime = "منذ أسبوع",
                    helpfulCount = 22
                ),
                FollowerReview(
                    restaurantId = 5,
                    followerName = "ياسين العاصمي",
                    rating = 9.0f,
                    agreement = "متفق تماماً مع الطبيب",
                    dishOrdered = "كرانتيكا بالهريسة وباردة سيلكتو",
                    comment = "البنة الشعبية بأبهى صورها وسعر في متناول الجميع. شكراً يا طبيب الماكلة لأنك تنصف المطاعم الشعبية البسيطة.",
                    visitTime = "اليوم",
                    helpfulCount = 18
                )
            )

            reviewDao.insertAll(reviews)
        }
    }
}
