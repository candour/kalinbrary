package com.messark.kalinbrary.data

import android.content.Context
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StorageManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("Kalinbrary", Context.MODE_PRIVATE)
    val gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(StoryElement::class.java, StoryElementAdapter())
        gson = gsonBuilder.create()
    }

    fun saveStories(stories: List<Story>) {
        val json = gson.toJson(stories)
        sharedPreferences.edit().putString("stories", json).apply()
    }

    fun loadStories(): MutableList<Story> {
        val json = sharedPreferences.getString("stories", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Story>>() {}.type
            try {
                gson.fromJson(json, type) ?: mutableListOf()
            } catch (e: JsonParseException) {
                e.printStackTrace()
                // Clear corrupted data
                sharedPreferences.edit().remove("stories").apply()
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }
}

class StoryElementAdapter : JsonSerializer<StoryElement>, JsonDeserializer<StoryElement> {
    override fun serialize(
        src: StoryElement?,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        when (src) {
            is TextElement -> {
                jsonObject.addProperty("type", "text")
                jsonObject.addProperty("text", src.text)
            }
            is ImageElement -> {
                jsonObject.addProperty("type", "image")
                jsonObject.addProperty("imageUrl", src.imageUrl)
            }
            null -> return JsonNull.INSTANCE
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): StoryElement {
        val jsonObject = json.asJsonObject
        val typeElement = jsonObject.get("type")

        if (typeElement != null) {
            // New format with "type" field
            return when (val type = typeElement.asString) {
                "text" -> {
                    val text = jsonObject.get("text").asString
                    TextElement(text)
                }
                "image" -> {
                    val imageUrl = jsonObject.get("imageUrl").asString
                    ImageElement(imageUrl)
                }
                else -> throw JsonParseException("Unknown element type: $type")
            }
        } else {
            // Handle old format by guessing the type based on fields
            return if (jsonObject.has("text")) {
                val text = jsonObject.get("text").asString
                TextElement(text)
            } else if (jsonObject.has("imageUrl")) {
                val imageUrl = jsonObject.get("imageUrl").asString
                ImageElement(imageUrl)
            } else {
                throw JsonParseException("Cannot determine type of StoryElement from old format")
            }
        }
    }
}