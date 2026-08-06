# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve Room database entities and DAOs
-keep class com.example.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-dontwarn androidx.room.paging.**

# Preserve annotations and type metadata for serialization/Room/Moshi
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Compose Runtime Owner
-keepclassmembers class * extends androidx.compose.ui.node.Owner { *; }

# Keep ViewModel classes
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

