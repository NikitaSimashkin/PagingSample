package ru.kram.pagingsample.ui.catlist.model

data class InfoBlockData(
    val text1Left: String?,
    val text1Right: String?,
    val text2Left: String?,
    val text2Right: String?,
    val text3Left: String?,
    val text3Right: String?,
) {
    companion object {
        val EMPTY = InfoBlockData(
            text1Left = null,
            text1Right = null,
            text2Left = null,
            text2Right = null,
            text3Left = null,
            text3Right = null,
        )
    }
}