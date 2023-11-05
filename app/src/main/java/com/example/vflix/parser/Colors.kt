package com.example.vflix.parser

var ARRAY_OF_GRADIENT_COLORS = arrayOf(
    arrayOf(0xFF0c1e0e, 0xFF789a78, 0xFF000000),
    arrayOf(0xFF0c0c1e, 0xFF78789a, 0xFF000000),
    arrayOf(0xFF0c1e0c, 0xFF789a78, 0xFF000000),
    arrayOf(0xFF1e0c0c, 0xFF9a7878, 0xFF000000),
    arrayOf(0xFF1e0c1e, 0xFF9a789a, 0xFF000000),
    arrayOf(0xFF1e1e0c, 0xFF9a9a78, 0xFF000000),
    arrayOf(0xFF0c1e1e, 0xFF789a9a, 0xFF000000),
)

fun GetRandGradient(): Array<Long> {
    return ARRAY_OF_GRADIENT_COLORS.random()
}