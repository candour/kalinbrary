package com.messark.kalinbrary.data

sealed class StoryElement

data class TextElement(val text: String) : StoryElement()

data class ImageElement(val imageUrl: String) : StoryElement()

data class Story(
    val title: String,
    val coverImageUrl: String? = null,
    val content: List<StoryElement>
)