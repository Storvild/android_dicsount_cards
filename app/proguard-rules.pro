# Project specific ProGuard rules
-keep class com.example.discountcards.data.local.entity.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
