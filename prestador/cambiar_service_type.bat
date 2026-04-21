@echo off
echo.
echo ================================================
echo CAMBIAR TIPO DE SERVICIO DEL PRESTADOR
echo ================================================
echo.
echo 1. TECHNICAL   - Servicios Tecnicos
echo 2. PROFESSIONAL - Profesional con Agenda
echo 3. RENTAL      - Alquiler de Espacios
echo.
set /p OPCION="Selecciona una opcion (1-3): "

if "%OPCION%"=="1" (
    set SERVICE_TYPE=TECHNICAL
    echo Cambiando a TECHNICAL...
)
if "%OPCION%"=="2" (
    set SERVICE_TYPE=PROFESSIONAL
    echo Cambiando a PROFESSIONAL...
)
if "%OPCION%"=="3" (
    set SERVICE_TYPE=RENTAL
    echo Cambiando a RENTAL...
)

echo.
echo Ejecutando comando SQL...
adb shell "run-as com.example.myapplication.prestador sqlite3 /data/data/com.example.myapplication.prestador/databases/prestador_database_v21 \"UPDATE providers SET serviceType='%SERVICE_TYPE%' WHERE id IN (SELECT id FROM providers LIMIT 1);\""

echo.
echo ✅ Tipo de servicio cambiado a: %SERVICE_TYPE%
echo.
echo Reinicia la app para ver los cambios.
echo.
pause
