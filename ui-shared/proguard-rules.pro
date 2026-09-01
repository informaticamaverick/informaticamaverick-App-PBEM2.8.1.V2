# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# --- REGLAS ELITE: UI-SHARED (v2026.REL) ---

# Preservar firmas genéricas y anotaciones para Compose
-keepattributes Signature
-keepattributes *Annotation*

# Preservar clases de UI compartidas
-keep class com.example.myapplication.uishared.** { *; }

# Coil
-keep class coil.** { *; }

