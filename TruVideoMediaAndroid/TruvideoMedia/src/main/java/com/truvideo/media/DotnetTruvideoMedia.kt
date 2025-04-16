package com.truvideo.media


import android.content.Context
import androidx.startup.AppInitializer
import com.google.gson.Gson
import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.TruvideoSdkMediaInitializer
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.model.TruvideoSdkMediaTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import truvideo.sdk.common.exceptions.TruvideoSdkException


class DotnetTruvideoMedia {
    // Method to set the listener from .NET MAUI
    fun setDataListener(listener: DataListener) {
        DotnetTruvideoMedia.listener = listener
    }

    companion object {
        var mainCallback: MediaCallback? = null
        var listener: DataListener? = null

        @JvmStatic
        fun version(callback: MediaCallback) {
            val version = TruvideoSdkMedia.version
            callback.onSuccess("" + version)
        }

        @JvmStatic
        fun initAppMediaInitializer(context: Context, callback: MediaCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                AppInitializer.getInstance(context)
                    .initializeComponent(TruvideoSdkMediaInitializer::class.java)
                callback.onSuccess("Media Initializer")
            }
        }

        @JvmStatic
        fun uploadMedia(
            context: Context,
            tag: String,
            metadata: String,
            arrayList: List<String>,
            mediaCallback: MediaCallback
        ) {
            AppInitializer.getInstance(context).initializeComponent(TruvideoSdkMediaInitializer::class.java)
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
        fun search(
            context: Context,
            tag: String,
            mediaFileTypeValue: MediaFileType,
            pageNumber: Int,
            size: Int,
            mediaCallback: MediaCallback
        ) {
            GlobalScope.launch(Dispatchers.Main) {
                val tagJson = JSONObject(tag)
                val map = mutableMapOf<String, String>()
                val keys = tagJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = tagJson.getString(key)
                }
                val tags = TruvideoSdkMediaTags(map)
                val mediaFileType = MediaFileTypeConverter.mediaFileType(mediaFileTypeValue)
                val mediaType =
                    if (mediaFileType == TruVideoSdkMediaFileType.Picture) {
                        TruvideoSdkMediaFileType.Picture
                    } else if (mediaFileType == TruVideoSdkMediaFileType.Video) {
                        TruvideoSdkMediaFileType.Video
                    }  else {
                        TruvideoSdkMediaFileType.All
                    }

                val result = TruvideoSdkMedia.search(tags, mediaType, pageNumber, size)
                val gson = Gson()
                val jsonResult = gson.toJson(result)
                mediaCallback.onSuccess(jsonResult)
            }
        }


        @JvmStatic
        fun streamAllRequests(context: Context, callback: MediaCallback) {
            val allRequest = TruvideoSdkMedia.streamAllFileUploadRequests()
            val gson = Gson()
            val jsonResult = gson.toJson(allRequest)
            callback.onSuccess(jsonResult)
        }


        @JvmStatic
        fun getAllRequests(statusValue: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val status = if (statusValue.equals("IDLE", true)) {
                    TruvideoSdkMediaFileUploadStatus.IDLE
                } else if (statusValue.equals("ERROR", true)) {
                    TruvideoSdkMediaFileUploadStatus.ERROR
                }
                else if (statusValue.equals("UPLOADING", true)) {
                    TruvideoSdkMediaFileUploadStatus.UPLOADING
                }
                else if (statusValue.equals("PAUSED", true)) {
                    TruvideoSdkMediaFileUploadStatus.PAUSED
                }
                else if (statusValue.equals("SYNCHRONIZING", true)) {
                    TruvideoSdkMediaFileUploadStatus.SYNCHRONIZING
                }
                else if (statusValue.equals("COMPLETED", true)) {
                    TruvideoSdkMediaFileUploadStatus.COMPLETED
                }
                else if (statusValue.equals("CANCELED", true)) {
                    TruvideoSdkMediaFileUploadStatus.CANCELED
                } else {
                    TruvideoSdkMediaFileUploadStatus.CANCELED
                }
                val allRequest = TruvideoSdkMedia.getAllFileUploadRequests(status)
                //val gson = Gson()
                //val jsonResult = gson.toJson(allRequest)
                callback.onSuccess(""+allRequest)
            }
        }

        @JvmStatic
        fun status(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                val status = request!!.status
                callback.onSuccess("" + status)
            }
        }

        @JvmStatic
        fun cancel(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                request!!.cancel()
                callback.onSuccess("Request Cancel")
            }
        }

        @JvmStatic
        fun delete(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                request!!.delete()
                callback.onSuccess("Request delete")
            }
        }

        @JvmStatic
        fun pause(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                request!!.pause()
                callback.onSuccess("Request pause")
            }
        }

    }
}