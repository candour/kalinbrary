package com.messark.kalinbrary.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StorageManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "KalinbrarySecure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(StoryElement::class.java, StoryElementAdapter())
        gson = gsonBuilder.create()
    }

    fun saveStories(stories: List<Story>) {
        val editor = sharedPreferences.edit()

        // Get previous IDs to know what to delete
        val oldIdsJson = sharedPreferences.getString("story_ids", null)
        val oldIds: List<String> = if (oldIdsJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            try {
                gson.fromJson(oldIdsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val currentIds = stories.map { it.id }

        // Save each story as an individual entry
        stories.forEach { story ->
            editor.putString("story_${story.id}", gson.toJson(story))
        }

        // Delete stories no longer present
        oldIds.forEach { id ->
            if (id !in currentIds) {
                editor.remove("story_$id")
            }
        }

        // Save IDs list to maintain order
        editor.putString("story_ids", gson.toJson(currentIds))

        editor.apply()
    }

    fun saveStory(story: Story) {
        sharedPreferences.edit().putString("story_${story.id}", gson.toJson(story)).apply()
    }

    fun deleteStory(id: String) {
        sharedPreferences.edit().remove("story_$id").apply()
    }

    fun saveStoryIds(ids: List<String>) {
        sharedPreferences.edit().putString("story_ids", gson.toJson(ids)).apply()
    }

    fun loadStories(): MutableList<Story> {
        // Migration from old unencrypted SharedPreferences if exists
        val oldPrefs = context.getSharedPreferences("Kalinbrary", Context.MODE_PRIVATE)
        val oldJson = oldPrefs.getString("stories", null)
        if (oldJson != null) {
            val type = object : TypeToken<MutableList<Story>>() {}.type
            try {
                val stories: MutableList<Story> = gson.fromJson(oldJson, type) ?: mutableListOf()
                // Migration: Ensure all stories have an ID
                val migratedStories = stories.map { story ->
                    @Suppress("SENSELESS_COMPARISON")
                    if (story.id == null) {
                        story.copy(id = java.util.UUID.randomUUID().toString())
                    } else {
                        story
                    }
                }.toMutableList()

                saveStories(migratedStories)
                oldPrefs.edit().remove("stories").apply()
                return migratedStories
            } catch (e: JsonParseException) {
                e.printStackTrace()
                oldPrefs.edit().remove("stories").apply()
            }
        }

        val idsJson = sharedPreferences.getString("story_ids", null)
        if (idsJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            try {
                val ids: List<String> = gson.fromJson(idsJson, type) ?: emptyList()
                return ids.mapNotNull { id ->
                    val storyJson = sharedPreferences.getString("story_$id", null)
                    if (storyJson != null) {
                        try {
                            gson.fromJson(storyJson, Story::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }
                }.toMutableList()
            } catch (e: JsonParseException) {
                e.printStackTrace()
                sharedPreferences.edit().remove("story_ids").apply()
            }
        }

        return mutableListOf()
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
