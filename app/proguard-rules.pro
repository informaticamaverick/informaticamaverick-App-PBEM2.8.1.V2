# --- REGLAS ELITE: APP AZUL (v2026.REL) ---

# 1. Preservar Modelos de UI y ViewModels (Evita fallos en SavedState y Recomposición)
-keep class com.example.myapplication.ui.componentes.be.modelos.** { *; }
-keep class com.example.myapplication.viewmodel.** { *; }

# 2. Entidades y Vistas SQL específicas de la App Azul
-keep class com.example.myapplication.datos.local.entidades.** { *; }

# 3. Google Play Services & Firebase
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# 4. Eliminar Logs en modo Release (Ley de Higiene Maverick)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# 5. Coil & Accompanist
-keep class coil.** { *; }
-keep class com.google.accompanist.** { *; }

# 6. Generative AI (Gemini)
-keep class com.google.ai.client.generativeai.** { *; }
