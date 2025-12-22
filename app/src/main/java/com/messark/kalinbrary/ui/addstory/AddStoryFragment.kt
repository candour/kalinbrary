package com.messark.kalinbrary.ui.addstory

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.messark.kalinbrary.R
import com.messark.kalinbrary.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AddStoryFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var coverImageUrlEditText: EditText
    private lateinit var contentEditText: EditText
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val localPath = saveImageFromUri(it)
                if (localPath != null) {
                    val currentText = contentEditText.text.toString()
                    val newText = "$currentText\n[IMAGE:$localPath]\n"
                    contentEditText.setText(newText)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_story, container, false)

        titleEditText = view.findViewById(R.id.edit_text_title)
        coverImageUrlEditText = view.findViewById(R.id.edit_text_cover_image_url)
        contentEditText = view.findViewById(R.id.edit_text_content)

        view.findViewById<Button>(R.id.button_add_image).setOnClickListener {
            val currentText = contentEditText.text.toString()
            val newText = "$currentText\n[IMAGE:PASTE_IMAGE_URL_HERE]\n"
            contentEditText.setText(newText)
        }

        view.findViewById<Button>(R.id.button_add_photo_from_phone).setOnClickListener {
            pickImage.launch("image/*")
        }

        view.findViewById<Button>(R.id.button_save_story).setOnClickListener {
            lifecycleScope.launch {
                saveStory()
                findNavController().navigate(R.id.action_AddStoryFragment_to_FirstFragment)
            }
        }

        return view
    }

    private suspend fun saveStory() {
        val title = titleEditText.text.toString()
        val coverImageUrl = coverImageUrlEditText.text.toString()
        val content = contentEditText.text.toString()

        val localCoverImagePath = if (coverImageUrl.isNotBlank()) {
            saveImageLocally(coverImageUrl)
        } else {
            null
        }

        val storyElements = mutableListOf<StoryElement>()
        val contentParts = content.split("\n")

        for (part in contentParts) {
            if (part.startsWith("[IMAGE:") && part.endsWith("]")) {
                val imageUrl = part.substring(7, part.length - 1)
                if (imageUrl.isNotBlank()) {
                    val localPath = if (imageUrl.startsWith("http")) {
                        saveImageLocally(imageUrl)
                    } else {
                        imageUrl
                    }
                    if (localPath != null) {
                        storyElements.add(ImageElement(localPath))
                    }
                }
            } else if (part.isNotBlank()) {
                storyElements.add(TextElement(part))
            }
        }

        if (title.isNotBlank() && (storyElements.isNotEmpty() || localCoverImagePath != null)) {
            val newStory = Story(title, localCoverImagePath, storyElements)
            StoryRepository.addStory(newStory)
        }
    }

    private suspend fun saveImageLocally(imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, fileName)
                URL(imageUrl).openStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun saveImageFromUri(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, fileName)
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}