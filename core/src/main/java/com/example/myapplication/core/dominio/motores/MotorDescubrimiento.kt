
/*package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.utilidades.normalizeFull
import com.example.myapplication.core.utilidades.normalizeForTopic
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE DESCUBRIMIENTO MAVERICK (Atómico v2026.ELITE) ---
 * [PROPÓSITO]: Único generador legal de llaves (Huellas) y tópicos del ecosistema.
 * [LEY #9]: Estándar Mav en Español. Unificación total Firestore/FCM.
 */
@Singleton
class MotorDescubrimiento @Inject constructor() {

    // --- SECTOR 1: CONSTANTES ATÓMICAS (Ley #9) ---
    val PRE_ZONA = "Z"      // Zona (C.P.)
    val PRE_PRESTADOR = "P" // Prestador / Búsqueda
    val PRE_OFERTA = "O"    // Oferta / Promoción
    val PRE_CONCURSO = "C"  // Concurso / Licitación

    /**
     * [ELITE] Normalización definitiva para huellas y tópicos.
     * Garantiza 100% de coincidencia entre Base de Datos y Notificaciones.
     */
    fun estandarizarLlave(texto: String): String {
        val std = texto.normalizeForTopic()
        Log.d("MOTOR_MAV", "🔍 [LLAVE_STD] '$texto' -> '$std'")
        return std
    }

    /**
     * [ELITE] Generador universal de huellas maestras.
     * Formato: PREFIJO_CP_CATEGORIA
     */
    fun generarHuellaMaestra(prefijo: String, cp: String, categoria: String? = null): String {
        val cpLimpio = normalizarCP(cp)
        if (cpLimpio.isEmpty()) return ""

        val base = "${prefijo}_$cpLimpio"
        val huella = if (categoria.isNullOrBlank()) base else "${base}_${estandarizarLlave(categoria)}"
        Log.d("MOTOR_MAV", "🛰️ [HUELLA_MAESTRA] $huella ($prefijo) | CP: $cpLimpio | Cat: $categoria")
        return huella
    }

    /**
     * [ELITE] Genera la jerarquía completa de etiquetas para descubrimiento masivo.
     * [ORDEN]: Zona -> Afinidad (SuperCat) -> Especialidad (Cat)
     */
    fun generarHuellasJerarquicasMav(
        cp: String, 
        superCategoria: String? = null, 
        categoria: String? = null,
        prefijo: String = PRE_OFERTA
    ): List<String> {
        val tags = mutableListOf<String>()
        val cpLimpio = normalizarCP(cp)
        if (cpLimpio.isBlank()) return emptyList()

        // 1. Capa de Zona (Z_1234)
        tags.add(generarHuellaMaestra(PRE_ZONA, cpLimpio))

        // 2. Capa de Especialidad Directa (PRE_CP_Cat)
        // [ELITE v2026.FINAL]: Prioridad a la búsqueda plana por rubro.
        if (!categoria.isNullOrBlank()) {
            tags.add(generarHuellaMaestra(prefijo, cpLimpio, categoria))
        }

        // 3. Capa de Afinidad (PRE_CP_SuperCat)
        if (!superCategoria.isNullOrBlank()) {
            tags.add(generarHuellaMaestra(prefijo, cpLimpio, superCategoria))

            // 4. Capa de Precisión Jerárquica (PRE_CP_SuperCat_Cat)
            if (!categoria.isNullOrBlank()) {
                val tagPrecision = "${prefijo}_${cpLimpio}_${estandarizarLlave(superCategoria)}_${estandarizarLlave(categoria)}"
                tags.add(tagPrecision)
            }
        }
        
        val tagsFinales = tags.distinct()
        Log.d("MOTOR_MAV", "🛰️ [HUELLAS_JERARQUICAS] Prefijo: $prefijo | Tags: $tagsFinales")
        return tagsFinales
    }

    /**
     * [ELITE] Genera el producto cartesiano de Zonas x Categorías.
     * Útil para prestadores con múltiples especialidades y ubicaciones.
     */
    fun generarMatrizDeBusqueda(
        codigosPostales: List<String>,
        categorias: List<String>,
        prefijo: String = PRE_PRESTADOR
    ): List<String> {
        val tags = mutableListOf<String>()
        val cpsLimpios = codigosPostales.map { normalizarCP(it) }.filter { it.isNotBlank() }.distinct()
        val catsLimpias = categorias.map { estandarizarLlave(it) }.filter { it.isNotBlank() }.distinct()

        cpsLimpios.forEach { cp ->
            // Etiquetas de Zona general
            tags.add(generarHuellaMaestra(PRE_ZONA, cp))

            catsLimpias.forEach { cat ->
                // Etiquetas de Especialidad en Zona: P_4000_plomeria
                tags.add("${prefijo}_${cp}_${cat}")
            }
        }
        return tags.distinct()
    }

    // --- SECTOR 2: GENERADORES TÁCTICOS (PILARES) ---

    fun generarTopicoZona(cp: String): String = generarHuellaMaestra(PRE_ZONA, cp)

    fun generarTopicoConcurso(cp: String, categoria: String): String =
        generarHuellaMaestra(PRE_CONCURSO, cp, categoria)

    fun generarTopicoPromocion(cp: String, superCategoria: String, categoria: String): String =
        "${PRE_OFERTA}_${normalizarCP(cp)}_${estandarizarLlave(superCategoria)}_${estandarizarLlave(categoria)}"

    // --- SECTOR 3: MÉTODOS DE COMPATIBILIDAD (OBSOLETOS - COMENTADOS PARA EVITAR USO) ---

    fun normalizarCP(codigoPostal: String): String {
        return com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones.limpiarCodigoPostal(codigoPostal)
    }

    /*
    fun generarLlaveBusqueda(codigoPostal: String, categoria: String): String {
        val cpLimpio = normalizarCP(codigoPostal)
        val catNormalizada = categoria.normalizeFull()
        return if (cpLimpio.isEmpty() || catNormalizada.isEmpty()) ""
               else "${cpLimpio}_${catNormalizada}"
    }

    fun generarHuellasJerarquicas(codigoPostal: String, superCategoria: String, categoria: String): List<String> {
        val cp = normalizarCP(codigoPostal)
        val sCat = superCategoria.normalizeFull()
        val cat = categoria.normalizeFull()

        val tags = mutableListOf<String>()

        if (cp.isNotBlank()) {
            tags.add(cp)
            if (sCat.isNotBlank()) {
                tags.add("${cp}_${sCat}")
                if (cat.isNotBlank()) {
                    tags.add("${cp}_${sCat}_${cat}")
                }
            }
        }
        return tags.distinct()
    }

    fun generarTagsDeBusqueda(cp: String, categoria: String, lat: Double = 0.0, lng: Double = 0.0): List<String> {
        val tags = mutableListOf<String>()

        val zipTag = generarLlaveBusqueda(cp, categoria)
        if (zipTag.isNotBlank()) tags.add(zipTag)

        val geoTag = generarTagGeohash(lat, lng, categoria)
        if (geoTag.isNotBlank()) tags.add(geoTag)

        return tags.distinct()
    }

    fun generarTopicosJerarquicos(codigoPostal: String, superCategoria: String, categoria: String): List<String> {
        val cp = normalizarCP(codigoPostal)
        val sCat = superCategoria.normalizeForTopic()
        val cat = categoria.normalizeForTopic()

        val topics = mutableListOf<String>()
        if (cp.isNotBlank()) {
            topics.add("zona_$cp")
            if (sCat.isNotBlank()) {
                topics.add("afinidad_${cp}_${sCat}")
                if (cat.isNotBlank()) {
                    topics.add("promo_${cp}_${sCat}_${cat}")
                }
            }
        }
        return topics.distinct()
    }
    */

    fun generarTagGeohash(lat: Double, lng: Double, categoria: String): String {
        if (lat == 0.0 || lng == 0.0) return ""
        // [ELITE]: Precisión 5 para cobertura de radio de búsqueda (~4.8km)
        val hash = com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica.generarGeohash(lat, lng, 5)
        val catNormalizada = estandarizarLlave(categoria)
        val tag = if (catNormalizada.isEmpty()) "" else "geo_${hash}_${catNormalizada}"
        if (tag.isNotBlank()) {
            Log.d("MOTOR_MAV", "🗺️ [TAG_GEOHASH] Gen: $tag | Cat: $categoria")
        }
        return tag
    }

    /*
    fun generarTopicoPromocionLegacy(codigoPostal: String, superCategoria: String, categoria: String): List<String> {
        val cp = normalizarCP(codigoPostal)
        val sCat = superCategoria.normalizeForTopic()
        val cat = categoria.normalizeForTopic()
        return if (cp.isEmpty() || sCat.isEmpty() || cat.isEmpty()) emptyList()
               else listOf("promo_${cp}_${sCat}_${cat}")
    }

    fun generarTopicoAfinidad(codigoPostal: String, superCategoria: String): String {
        val cp = normalizarCP(codigoPostal)
        val sCat = superCategoria.normalizeForTopic()
        return if (cp.isEmpty() || sCat.isEmpty() || "" == sCat) ""
               else "afinidad_${cp}_${sCat}"
    }
    */

    fun generarTagAtributo(llaveBase: String, atributo: String): String {
        return if (llaveBase.isEmpty()) "" else "${llaveBase}_${atributo.lowercase()}"
    }
}


*/