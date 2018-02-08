package com.eje_c.vsaplayer

/**
 * ミリ秒を時間の文字列に変換する。
 */
fun Long.millisToTimeString(): String {

    val second = this / 1000
    val minutes = second / 60

    return if (minutes < 60) {
        String.format("%d:%02d", minutes, second % 60)
    } else {
        val hours = minutes / 60
        String.format("%d:%02d:%02d", hours, minutes % 60, second % 60)
    }
}