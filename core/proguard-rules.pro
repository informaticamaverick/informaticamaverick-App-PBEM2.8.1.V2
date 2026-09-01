# --- REGLAS GLOBALES MAVERICK (V2026.ELITE) ---

# Preservar firmas genéricas (Vital para Gson TypeToken y Retrofit)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Retrofit & OkHttp
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class com.example.myapplication.core.datos.remoto.api.** { *; }
-keepclassmembers class com.example.myapplication.core.datos.remoto.api.** { *; }

# Gson: Reglas específicas para evitar el error de TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room: Preservar Converters
-keep class com.example.myapplication.core.datos.local.Converters { *; }

# Firebase Firestore: Preservar clases de datos
-keepclassmembers class com.example.myapplication.core.datos.local.entidades.** { *; }
-keepclassmembers class com.example.myapplication.core.dominio.modelos.** { *; }
-keepattributes RuntimeVisibleAnnotations

# Coil (Carga de imágenes)
-keep class coil.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }

# Preservar el uso de @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
