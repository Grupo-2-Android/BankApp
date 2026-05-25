package com.example.bankapp.presentation.utils

fun formatCardNumber(number: String): String {
    return number.chunked(4).joinToString(" ")
}

fun hideCardNumber(number: String): String {
    return "**** **** **** ${number.takeLast(4)}"
}