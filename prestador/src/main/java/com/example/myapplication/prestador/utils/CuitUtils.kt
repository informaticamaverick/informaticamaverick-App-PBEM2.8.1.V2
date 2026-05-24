package com.example.myapplication.prestador.utils

/**
 * Formatea un CUIT/CUIL como XX-XXXXXXXX-X mientras el usuario escribe.
 * Solo trabaja con los dígitos, ignorando guiones existentes.
 */
fun formatearCuit(input: String): String {
    val digits = input.filter { it.isDigit() }.take(11)
    return buildString {
        digits.forEachIndexed { i, c ->
            if (i == 2 || i == 10) append('-')
            append(c)
        }
    }
}

/**
 * Valida un CUIT/CUIL argentino usando el algoritmo oficial de dígito verificador.
 * Acepta formatos: "20345678901", "20-34567890-1", etc.
 * Retorna true si el CUIT es válido.
 */
fun esCuitValido(cuit: String): Boolean {
    val digits = cuit.filter { it.isDigit() }
    if (digits.length != 11) return false

    val coef = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
    val sum = coef.indices.sumOf { i -> digits[i].digitToInt() * coef[i] }
    val remainder = sum % 11
    val verificador = when (val check = 11 - remainder) {
        11 -> 0
        10 -> return false // CUIT inválido si check == 10
        else -> check
    }
    return digits[10].digitToInt() == verificador
}

/**
 * Mensaje de error descriptivo para mostrar en el campo CUIT.
 * Retorna null si es válido.
 */
fun errorCuitMensaje(cuit: String): String? {
    val digits = cuit.filter { it.isDigit() }
    return when {
        digits.isEmpty() -> null // no validar si está vacío
        digits.length < 11 -> "El CUIT debe tener 11 dígitos"
        !esCuitValido(cuit) -> "CUIT inválido (dígito verificador incorrecto)"
        else -> null
    }
}
