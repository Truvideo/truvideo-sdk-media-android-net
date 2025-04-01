package com.truvideo.media

import android.content.Context
import androidx.startup.AppInitializer
import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.TruvideoSdkMediaInitializer
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaMetadata
import com.truvideo.sdk.media.model.TruvideoSdkMediaTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File


class DotnetTruvideoMedia {
    companion object {
        var mainCallback: MediaCallback? = null

        @JvmStatic
        fun uploadMedia(
            context: Context,
            arrayList: List<String>,
            mediaCallback: MediaCallback
        ) {
            AppInitializer.getInstance(context)
                .initializeComponent(TruvideoSdkMediaInitializer::class.java)
            //Log.d("IMPORTANT_TAG", "initAppMediaInitializer: STARTED")
            mainCallback = mediaCallback
            arrayList.forEach {
                upload(context, it)
            }
        }

        fun upload(context: Context?, filepath: String) {
            // Create a file upload request builder
            val builder = TruvideoSdkMedia.FileUploadRequestBuilder(filepath)
            // --------------------------
            // TAGS
            // --------------------------

            // Option 1: use the file upload request builder directly
            builder.addTag("key", "value")
            builder.addTag("color", "red")
            builder.addTag("order-number", "123")

            // Option 2: use the tag builder
            val tags = TruvideoSdkMediaTags
                .builder()
                .set("key", "value")
                .set("color", "red")
                .set("order-number", "123")
                .build()
            builder.setTags(tags)

            // --------------------------
            // METADATA
            // --------------------------

            // Option 1: use the file upload request builder directly
            builder.addMetadata("key", "value")
            builder.addMetadata("list", listOf("value1", "value2"))
            builder.addMetadata(
                "nested", TruvideoSdkMediaMetadata.builder()
                    .set("key", "value")
                    .set("list", listOf("value1", "value2"))
                    .build()
            )

            // Options2: use the metadata builder
            val metadata = TruvideoSdkMediaMetadata
                .builder()
                .set("key", "value")
                .set("list", listOf("value1", "value2"))
                .set(
                    "nested", TruvideoSdkMediaMetadata.builder()
                        .set("key", "value")
                        .set("list", listOf("value1", "value2"))
                        .build()
                )
                .build()
            builder.setMetadata(metadata)
            GlobalScope.launch(Dispatchers.Main) {
                // Build the request
                val request = try {
                    builder.build()
                } catch (exception: Exception) {
                    // handle creating request error
                    throw Exception("Error creating the file upload request")
                }

                // Upload the file
                try {
                    request.upload(object : TruvideoSdkMediaFileUploadCallback {
                        override fun onComplete(
                            id: String,
                            response: TruvideoSdkMediaFileUploadRequest
                        ) {
                            // File uploaded successfully
                           // Log.d("IMPORTANT_TAG", "File uploaded successfully:")
                            //mainCallback?.onSuccess("File uploaded successfully")
                            mainCallback?.onSuccess(response.toJson())

                        }

                        override fun onProgressChanged(id: String, progress: Float) {
                            // Handle uploading progress
                           // Log.d("IMPORTANT_TAG", "File uploading progress:")
                        }

                        override fun onError(id: String, ex: TruvideoSdkException) {
                            // Handle file uploading error
                            //Log.d("IMPORTANT_TAG", "File uploading error:")
                        }
                    })
                } catch (exception: Exception) {
                    // Handle start to upload error
                    throw Exception("Error starting to upload the file")
                }

            }
        }

        @JvmStatic
        fun getResultPath(context: Context, name: String, mediaCallback: MediaCallback) {
            // get result path with dynamic name

            var outputPath = File("${context.filesDir}/${name}").path
            mediaCallback.onSuccess(outputPath)
        }

        @JvmStatic
        // Function to get all image paths in a directory
        fun getAllImagePathsInDirectoryPng(directoryPath: String): ArrayList<String> {
            val directory = File(directoryPath)
            val imagePaths = ArrayList<String>()
            // Check if the directory exists and is a directory
            if (directory.exists() && directory.isDirectory) {
                // Recursively traverse the directory
                directory.walkTopDown().forEach {
                    // Filter out image files (you can add more extensions if needed)
                    if (it.isFile && it.extension.lowercase() in listOf("png", "jpeg", "jpg")) {
                        imagePaths.add(it.absolutePath)
                    }
                }
            }
            return imagePaths
        }
    }
}