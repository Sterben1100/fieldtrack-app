# Proguard rules for FieldTrack Pro ASW Foods
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.aswfoods.fieldtrack.data.remote.dto.** { *; }

