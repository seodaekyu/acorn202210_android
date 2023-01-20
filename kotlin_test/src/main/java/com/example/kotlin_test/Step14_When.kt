package com.example.kotlin_test

fun main() {

    var selected = "gun"

    when(selected){
        "gun" -> println("총으로 공격해요!")
        "sword" -> println("검으로 공격해요!")
        else -> println("주먹(기본 무기)으로 공격해요!")
    }
}