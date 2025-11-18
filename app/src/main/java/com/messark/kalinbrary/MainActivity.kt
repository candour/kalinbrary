package com.messark.kalinbrary

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.gson.JsonParseException
import com.messark.kalinbrary.data.ImageElement
import com.messark.kalinbrary.data.Story
import com.messark.kalinbrary.data.StoryRepository
import com.messark.kalinbrary.data.StorageManager
import com.messark.kalinbrary.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val EXPORT_STORIES_REQUEST_CODE = 1
private const val IMPORT_STORIES_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var storageManager: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        storageManager = StorageManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportStories()
                true
            }
            R.id.action_import -> {
                importStories()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportStories() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, "stories.zip")
        }
        startActivityForResult(intent, EXPORT_STORIES_REQUEST_CODE)
    }

    private fun importStories() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
        startActivityForResult(intent, IMPORT_STORIES_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                EXPORT_STORIES_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        lifecycleScope.launch {
                            Toast.makeText(this@MainActivity, "Exporting...", Toast.LENGTH_SHORT).show()
                            val errorMessage = writeZipFile(uri)
                            val message = errorMessage ?: "Export successful"
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                IMPORT_STORIES_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        lifecycleScope.launch {
                            Toast.makeText(this@MainActivity, "Importing...", Toast.LENGTH_SHORT).show()
                            val errorMessage = readZipFile(uri)
                            val message = errorMessage ?: "Import successful"
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun writeZipFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val stories = StoryRepository.getStories()
            val imageFiles = mutableSetOf<File>()
            val storiesForExport = stories.map { story ->
                val newContent = story.content.mapNotNull { element ->
                    if (element is ImageElement && element.imageUrl.startsWith("/")) {
                        val file = File(element.imageUrl)
                        if (file.exists()) {
                            imageFiles.add(file)
                            ImageElement(file.name)
                        } else {
                            null // Remove image element if local file is missing
                        }
                    } else {
                        element // Keep text elements and http images
                    }
                }

                var finalCoverUrl: String? = story.coverImageUrl
                if (story.coverImageUrl?.startsWith("/") == true) {
                    val coverImageFile = File(story.coverImageUrl)
                    if (coverImageFile.exists()) {
                        imageFiles.add(coverImageFile)
                        finalCoverUrl = coverImageFile.name
                    } else {
                        finalCoverUrl = null // Remove broken reference
                    }
                }

                Story(story.title, finalCoverUrl, newContent)
            }

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
                    zos.putNextEntry(ZipEntry("stories.json"))
                    zos.write(storageManager.gson.toJson(storiesForExport).toByteArray())
                    zos.closeEntry()

                    imageFiles.forEach { file ->
                        FileInputStream(file).use { fis ->
                            BufferedInputStream(fis).use { origin ->
                                val entry = ZipEntry(file.name)
                                zos.putNextEntry(entry)
                                origin.copyTo(zos, 1024)
                                zos.closeEntry()
                            }
                        }
                    }
                }
            }
            null
        } catch (e: IOException) {
            e.printStackTrace()
            "Export failed: Could not write to file."
        } catch (e: Exception) {
            e.printStackTrace()
            "Export failed: An unexpected error occurred."
        }
    }

    private suspend fun readZipFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            var storiesJson: String? = null
            contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val fileName = entry!!.name
                        if (fileName == "stories.json") {
                            val writer = StringWriter()
                            zis.reader(Charset.defaultCharset()).copyTo(writer)
                            storiesJson = writer.toString()
                        } else {
                            val file = File(filesDir, fileName)
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }
                }
            }

            if (storiesJson != null) {
                val type = object : com.google.gson.reflect.TypeToken<List<Story>>() {}.type
                val importedStories: List<Story> = storageManager.gson.fromJson(storiesJson, type)
                val finalStories = importedStories.map { story ->
                    val newContent = story.content.mapNotNull { element ->
                        if (element is ImageElement && !element.imageUrl.startsWith("http")) {
                            val localFile = File(filesDir, element.imageUrl)
                            if (localFile.exists()) {
                                ImageElement(localFile.absolutePath)
                            } else {
                                null
                            }
                        } else {
                            element
                        }
                    }
                    val newCoverUrl = story.coverImageUrl?.let { url ->
                        if (!url.startsWith("http")) {
                            val localFile = File(filesDir, url)
                            if (localFile.exists()) {
                                localFile.absolutePath
                            } else {
                                null
                            }
                        } else {
                            url
                        }
                    }
                    Story(story.title, newCoverUrl, newContent)
                }
                withContext(Dispatchers.Main) {
                    StoryRepository.clearAndAddStories(finalStories)
                }
            } else {
                return@withContext "Import failed: stories.json not found in the archive."
            }
            null
        } catch (e: JsonParseException) {
            e.printStackTrace()
            "Import failed: Corrupted or invalid story data format."
        } catch (e: IOException) {
            e.printStackTrace()
            "Import failed: Could not read the file."
        } catch (e: Exception) {
            e.printStackTrace()
            "Import failed: An unexpected error occurred."
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
