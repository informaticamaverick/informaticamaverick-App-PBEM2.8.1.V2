# Walkthrough: Rediseño de Editor de Ubicación Elite (SUV v2026)

Se ha transformado el editor de direcciones para cumplir con los estándares de seguridad y normalización de datos Maverick, asegurando que cada ubicación esté verificada y lista para el motor de búsqueda.

## Cambios Realizados

### 💎 Seguridad SUV (Sistema de Ubicación Verificada)
- **Normalización Forzosa**: Se implementó un flujo que exige al usuario presionar **"CALCULAR DIRECCIÓN"** tras cualquier edición manual.
- **Integración con Core**: El botón de cálculo utiliza `GeoUtils.getAddressFromText` para validar la existencia real de la dirección y autocompletar campos con nombres normalizados.
- **Distintivo GPS**: Al usar el sensor del dispositivo, aparece el mensaje de confianza **"Dirección verificada por GPS"**.

### 🆘 Rescate de Ubicación (Problemas con el GPS)
- **Resolución por Link**: Se implementó la función **"¿Tienes problemas con la Ubicación?"**. Al activarla, se abre un diálogo que permite:
    - Pegar un enlace compartido de **Google Maps** (soporta links cortos `maps.app.goo.gl` y largos).
    - Ingresar **coordenadas manuales** (lat, lng).
- **Inteligencia Geográfica**: El motor de Core resuelve el enlace, extrae las coordenadas y realiza un geocoding inverso para completar el formulario automáticamente con datos 100% válidos.

### 🎨 Rediseño Estético y Funcional
- **Nueva Cabecera**: Título "Mi Ubicación" y botón de cierre alineados a los bordes.
- **Datos Técnicos Elite**: Campos de solo lectura para **País, Latitud, Longitud y Código Geo (Geohash)**.
- **Preview Integrada**: El archivo `FormularioDireccionMav.kt` ahora cuenta con su propia `@Preview` para agilizar el diseño futuro.

## Verificación de Flujos

1.  **Edición Manual**: Si el usuario cambia la calle, el botón "Guardar" se bloquea automáticamente.
2.  **Rescate**: El usuario pega un link de Maps -> El sistema resuelve -> Se autocompletan todos los campos técnicos -> El botón "Guardar" se habilita.
3.  **SUV Directo**: El uso de GPS realiza todo el proceso de una vez, marcando la dirección como verificada por sensor.

> [!IMPORTANT]
> Este cambio garantiza que no existan "direcciones fantasma" en el ecosistema, protegiendo la integridad de la base de datos y la fiabilidad de las búsquedas.
