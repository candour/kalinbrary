package com.messark.kalinbrary.data

import android.content.Context

object StoryRepository {

    private lateinit var storageManager: StorageManager
    private val stories = mutableListOf<Story>()

    fun init(context: Context) {
        storageManager = StorageManager(context)
        val loadedStories = storageManager.loadStories()
        if (loadedStories.isNotEmpty()) {
            stories.addAll(loadedStories)
        } else {
            // Add a default story if storage is empty
            stories.add(
                Story(
                    title = "The Little Match Girl",
                    coverImageUrl = "https://images.unsplash.com/photo-1519996529648-284bc6738369?q=80&w=1000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8bWF0Y2hlc3xlbnwwfHwwfHx8MA%3D%3D",
                    content = listOf(
                        TextElement("It was terribly cold and nearly dark on the last evening of the old year, and the snow was falling fast. In the cold and the darkness, a poor little girl with bare head and naked feet was roaming through the streets. She had been wearing slippers, it is true, when she left home, but they were not of much use. They were very large slippers, and her mother had used them till then, so they were not small. The little maiden lost them as she slipped across the road, where two carriages were rattling by at a terrible rate. One of the slippers she could not find again, and a boy grabbed the other and ran off with it. He thought he could use it as a cradle, some day when he had children of his own. So the little girl walked about in the streets on her naked feet, which were red and blue with the cold. In her old apron she carried a great many matches, and she had a packet of them in her hand. No one had bought any of them the whole day, and no one had given her a single penny."),
                        ImageElement("https://images.unsplash.com/photo-1519996529648-284bc6738369?q=80&w=1000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8bWF0Y2hlc3xlbnwwfHwwfHx8MA%3D%3D"),
                        TextElement("Shivering with cold and hunger, she crept along, a picture of misery, poor little girl! The snowflakes fell on her long, fair hair, which hung in pretty curls over her neck. In a corner, between two houses, one of which projected beyond the other, she sank down and huddled herself together. She had drawn her little feet under her, but she was colder than ever, and she dared not go home, for she had sold no matches, and could not bring a single penny of money. Her father would certainly beat her; besides, it was cold at home, for they had only the roof to cover them, through which the wind howled, although the largest cracks were stopped up with straw and rags. Her little hands were almost frozen with the cold. Ah! a match might afford her a world of comfort, if she only dared take a single one from the packet, draw it against the wall, and warm her fingers. She drew one out. R-r-ratch! how it sputtered and burned! It was a warm, bright flame, like a little candle, as she held her hands over it; it was a wonderful little light! It seemed to the little girl that she was sitting by a large iron stove, with polished brass feet and a brass ornament. How the fire burned! and how comfortable it was! The little girl had already stretched out her feet to warm them, when, lo! the flame of the match went out, the stove vanished, and she had only the remains of the half-burned match in her hand."),
                    )
                )
            )
            storageManager.saveStories(stories)
        }
    }

    fun getStories(): List<Story> {
        return stories
    }

    fun addStory(story: Story) {
        stories.add(story)
        storageManager.saveStories(stories)
    }

    fun deleteStory(story: Story) {
        stories.remove(story)
        storageManager.saveStories(stories)
    }

    fun clearAndAddStories(newStories: List<Story>) {
        stories.clear()
        stories.addAll(newStories)
        storageManager.saveStories(stories)
    }
}