# --- REGLAS DE PERSISTENCIA MAVERICK (V2026.ELITE) ---

# Mantener Entidades de Room (Crucial para serialización JSON en Converters)
-keep class com.example.myapplication.core.datos.local.entidades.** { *; }

# Mantener Modelos de Dominio (Crucial para transporte de datos y GSON)
-keep class com.example.myapplication.core.dominio.modelos.** { *; }

# Mantener anotaciones de GSON y firmas genéricas (Vital para TypeToken)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.annotations.SerializedName { *; }

