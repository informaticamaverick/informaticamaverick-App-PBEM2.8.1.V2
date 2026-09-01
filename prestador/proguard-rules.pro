# --- REGLAS ELITE: APP NARANJA (PRESTADOR) (v2026.REL) ---

# 1. Preservar ViewModels específicos del Prestador
-keep class com.example.myapplication.prestador.viewmodel.** { *; }

# 2. Entidades locales del Prestador
-keep class com.example.myapplication.prestador.datos.local.entidades.** { *; }

# 3. Google Play Billing (Membresía Elite)
-keep class com.android.billingclient.** { *; }

# 4. Google Play Services & Firebase
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# 5. Eliminar Logs en modo Release (Ley de Higiene Maverick)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# 6. Coil & Multimedia
-keep class coil.** { *; }
-keep class androidx.exifinterface.** { *; }
