# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Capacitor plugin classes
-keep public class * extends com.getcapacitor.Plugin

# Keep Android Auto classes
-keep class androidx.car.app.** { *; }
-keep interface androidx.car.app.** { *; }

# Keep our plugin classes
-keep class com.yourcompany.androidauto.** { *; }
