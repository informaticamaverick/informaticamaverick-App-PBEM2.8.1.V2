# Configuración de Firebase para Sistema Topik (Licitaciones)

Para que las notificaciones masivas funcionen correctamente entre la App del Usuario y la App del Prestador, no necesitas configurar manualmente cada Topic en la consola de Firebase, pero sí debes asegurar que las reglas y permisos sean los adecuados.

## 1. Cloud Messaging (FCM)
El sistema utiliza el ruteo por **Topics**. La estructura es:
`tender_{CP}_{Categoría}` (ejemplo: `tender_t4000_plomero`).

- **App Prestador**: Se suscribe automáticamente a estos topics al iniciar la app basándose en sus servicios y zona.
- **App Usuario**: Publica en estos topics mediante una llamada al servidor o función en la nube (Cloud Function).

> [!IMPORTANT]
> Debes habilitar la **FCM API (V1)** en la consola de Google Cloud asociada a tu proyecto de Firebase.

## 2. Firestore Rules
Para la **Ley #2 (Costo Zero)** y sincronización bidireccional, asegúrate de que las reglas permitan:
- Usuarios: Crear en `LicitacionesAbiertas` y leer sus propios presupuestos en `presupuestos`.
- Prestadores: Leer en `LicitacionesAbiertas` (filtrado por categoría) y crear en `presupuestos`.

```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /LicitacionesAbiertas/{tenderId} {
      allow read: if true; // Opcionalmente filtrar por token de prestador
      allow write: if request.auth != null;
    }
    match /presupuestos/{budgetId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 3. Cloud Functions (Opcional pero Recomendado)
Si deseas que la notificación incluya datos enriquecidos sin que la App del Usuario tenga que manejar la lógica de red pesada:
- Crea una función `onTenderCreated` que escuche cambios en Firestore y dispare el mensaje al Topic correspondiente.
