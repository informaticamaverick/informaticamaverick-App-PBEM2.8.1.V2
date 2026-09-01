# Walkthrough - Protocolo de los 5 Pilares de Identidad

He cristalizado la arquitectura soberana de identidades de PBEM en un nuevo documento maestro, asegurando que la jerarquía entre la Cuenta, el Prestador y la Empresa sea inmutable y clara para el desarrollo futuro.

## 🏛️ El Manifiesto de Identidad

He creado el archivo **`PROTOCOLO_5PILARES_IDENTIDAD.md`** en el Core, el cual detalla:

### 1. Definición Táctica de Pilares
El documento explica la responsabilidad única de cada pilar:
- **Pilar #1 (Cuenta)**: El Root de suscripción y soberanía.
- **Pilar #2 (Prestador)**: La identidad profesional humana.
- **Pilar #3 (Empresa)**: El contenedor de marca legal.
- **Pilar #4 (Sucursal)**: El punto operativo geográfico (fundamental para el buscador).
- **Pilar #5 (Usuario)**: El perfil ligero del cliente.

### 2. Manual de Mappers (Shallow vs Deep)
Se ha documentado el proceso de transformación:
- **Shallow**: Datos de alta velocidad (<1KB) para el descubrimiento masivo.
- **Deep**: Perfiles completos con biografía y galerías, cargados por demanda.

### 3. Reglas de Soberanía Atómica
El manual explica cómo el sistema decide qué mostrar (ej: Modo Empresa vs Personal) y cómo Room gestiona la unificación de estas identidades mediante la **DatabaseView** sin duplicar datos.

## Cumplimiento de Estándares
- **Idioma**: 100% Español Profesional.
- **Ubicación**: Centralizado en `:core` para visibilidad de ambas apps.
- **Trazabilidad**: Incluye referencias cruzadas a los mappers y entidades reales.

> [!TIP]
> Este documento sirve como la "Constitución" del sistema de identidades; cualquier cambio en la estructura de perfiles debe ser validado primero contra este protocolo.
