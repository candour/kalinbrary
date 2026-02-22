# Kalinbrary
### Your Personal secure Story Library

Kalinbrary is a native Android application designed to help you create, manage, and preserve your personal stories. Whether it's a travel journal, a collection of short stories, or a simple diary, Kalinbrary provides a secure and intuitive platform to combine text and imagery into a beautiful digital library.

## 🚀 Features

- **Story Management**: Create, edit, and delete stories with ease.
- **Rich Content**: Compose stories using a flexible mix of text and image elements.
- **Secure by Design**: Your stories are protected using industry-standard encryption.
- **Portability**: Export your entire library to a ZIP archive for backup or import it onto another device.
- **Local First**: Images are stored locally on your device to ensure privacy and offline access.

## 🛠 Tech Stack

- **Kotlin**: Modern, expressive language for Android development.
- **Jetpack Navigation**: Robust in-app navigation with Safe Args.
- **Jetpack Security**: Secure data persistence using `EncryptedSharedPreferences`.
- **GSON**: Flexible JSON serialization for story data.
- **Picasso**: Efficient image loading and caching.
- **Material Design**: Modern and consistent user interface.

## 🏗 Architecture & Security

Kalinbrary prioritizes the privacy and security of your data.

- **Storage**: Stories are serialized to JSON and stored in `EncryptedSharedPreferences` (AES256_GCM). This ensures that your story content remains protected even if the device's storage is accessed.
- **Data Model**: Stories are composed of a series of `StoryElement` objects, allowing for a flexible, linear narrative structure.
- **Backup & Portability**: The Import/Export feature packages your stories and their associated local images into a ZIP file. During import, a "Zip Slip" vulnerability check is performed to ensure the security of your device's file system.

## 📱 How to Use

1. **Adding a Story**: Tap the Floating Action Button (+) on the main screen. Enter a title, choose a cover image (via URL or local path), and start adding text or image blocks.
2. **Viewing a Story**: Tap on any story in your library list to read it.
3. **Editing/Deleting**: While viewing a story, use the overflow menu (three dots) in the top right corner to edit or delete the story.
4. **Import/Export**: Use the icons in the main screen's top toolbar to backup your library to a ZIP file or restore from a previously created backup.

## 📄 License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0). See the [LICENSE](LICENSE) file for details.
