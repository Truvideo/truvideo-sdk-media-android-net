package com.truvideo.media


import android.content.Context
import androidx.startup.AppInitializer
import com.google.gson.Gson
import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.TruvideoSdkMediaInitializer
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaMetadata
import com.truvideo.sdk.media.model.TruvideoSdkMediaTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File


class DotnetTruvideoMedia {
    // Method to set the listener from .NET MAUI
    fun setDataListener(listener: DataListener) {
        DotnetTruvideoMedia.listener = listener
    }


    companion object {
        var mainCallback: MediaCallback? = null
        var listener: DataListener? = null

        @JvmStatic
        fun uploadMedia(
            context: Context,
            tag: String,
            metadata: String,
            arrayList: List<String>,
            mediaCallback: MediaCallback
        ) {
            AppInitializer.getInstance(context)
                .initializeComponent(TruvideoSdkMediaInitializer::class.java)
            mainCallback = mediaCallback
            arrayList.forEach {
                upload(context, tag, metadata, it)
            }
        }


        fun upload(context: Context?, tag: String, metadata: String, filepath: String) {
            // Create a file upload request builder
            val builder = TruvideoSdkMedia.FileUploadRequestBuilder(filepath)
            val tagJson = JSONObject(tag)

            tagJson.keys().forEach {
                val key = it
                val value = tagJson.getString(key)
                builder.addTag(key, value)
            }
            val metadataJson = JSONObject(metadata)

            metadataJson.keys().forEach {
                val key = it
                val value = metadataJson.getString(key)
                builder.addMetadata(key, value)
            }
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

                            //mainCallback?.onSuccess("File uploaded successfully")
                            //mainCallback?.onSuccess(response.toJson())
                            val mainResponse = mapOf<String, Any?>(
                                "event" to "onComplete",
                                "response" to "success"
                            )
                            listener?.onDataReceived("${mainResponse}")
                            mainCallback?.onSuccess(response.toJson())
                        }

                        override fun onProgressChanged(id: String, progress: Float) {
                            // Handle uploading progress
                            val mainResponse = mapOf<String, Any?>(
                                "event" to "onProgressChanged",
                                "response" to progress * 100
                            )
                            listener?.onDataReceived("$mainResponse")
                        }

                        override fun onError(id: String, ex: TruvideoSdkException) {
                            // Handle file uploading error
                            val mainResponse = mapOf<String, Any?>(
                                "event" to "onError",
                                "response" to ex
                            )
                            listener?.onDataReceived("$mainResponse")
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