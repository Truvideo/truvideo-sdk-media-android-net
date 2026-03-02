package com.truvideo.media


import android.content.Context
import android.os.Build
import android.util.Log
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
import java.io.File
import java.time.format.DateTimeFormatter

class DotnetTruvideoMedia {
    // Method to set the listener from .NET MAUI
    fun setDataListener(listener: DataListener) {
        DotnetTruvideoMedia.listener = listener
    }

    companion object {
        var mainCallback: MediaCallback? = null
        var listener: DataListener? = null

        // ✅ Version
        @JvmStatic
        fun version(callback: MediaCallback) {
            val version = TruvideoSdkMedia.version
            callback.onSuccess("" + version)
        }

        // ✅ Init Media
        @JvmStatic
        fun initAppMediaInitializer(context: Context, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                AppInitializer.getInstance(context)
                    .initializeComponent(TruvideoSdkMediaInitializer::class.java)
                callback.onSuccess("Media Initializer")
            }
        }

        // ✅ Upload multiple files
        @JvmStatic
        fun uploadMedia(context: Context, tag: String, metadata: String, arrayList: List<String>, mediaCallback: MediaCallback) {
            AppInitializer.getInstance(context)
                .initializeComponent(TruvideoSdkMediaInitializer::class.java)
            mainCallback = mediaCallback
            arrayList.forEach {
                upload(context, tag, metadata, it)
            }
        }

        // ✅ Upload single file
        private fun upload(context: Context?, tag: String, metadata: String, filepath: String) {
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

            CoroutineScope(Dispatchers.Main).launch {
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
                    //throw Exception("Error starting to upload the file")
                    mainCallback?.onFailure("Error starting to upload the file")
                }

            }
        }

        // ✅ Build media request
        @JvmStatic
        fun mediaBuilder(filePath: String, tag: String, metaData: String, callback: MediaCallback) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    callback.onFailure("File not found")
                }
                CoroutineScope(Dispatchers.Main).launch {
                    builder(filePath, tag, metaData, callback)
                }
            } catch (e: Exception) {
                mainCallback?.onFailure("Builder failed:")
            }
        }

        private fun builder(filePath: String, tag: String, metaData: String, callback: MediaCallback) {
            // Create a file upload request builder
            try {
                val builder = TruvideoSdkMedia.FileUploadRequestBuilder(filePath)
                val jsonTag = JSONObject(tag)
                val keys = jsonTag.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value =
                        jsonTag.getString(key) // Can be any type: String, Integer, Boolean, etc.
                    builder.addTag(key, value)
                }
                // Metadata
                val jsonMetadata = JSONObject(metaData)
                val metadataKeys = jsonMetadata.keys()
                while (metadataKeys.hasNext()) {
                    val key = metadataKeys.next()
                    val value =
                        jsonMetadata.getString(key) // Can be any type: String, Integer, Boolean, etc.
                    builder.addMetadata(key, value)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    // Build the request
                    val request = builder.build()
                    val mainResponse = returnRequest(request)
                    // Upload the file
                    callback.onSuccess(mainResponse)

                }

            } catch (e: Exception) {
                callback.onFailure("Builder failed: ${e.message}")
            }
        }

        // ✅ Convert request to JSON
        private fun returnRequest(request: TruvideoSdkMediaFileUploadRequest): String {
            return JSONObject().apply {
                put("id", request.id)
                put("filePath", request.filePath)
                put("fileType", request.type)
                put("tags", request.tags)
                put("metadata", request.metadata)
                put("durationMilliseconds", request.durationMilliseconds)
                put("remoteId", request.remoteId)
                put("remoteURL", request.remoteUrl)
                put("transcriptionURL", request.transcriptionUrl)
                put("transcriptionLength", request.transcriptionLength)
                put("status", request.status)
                put("progress", request.uploadProgress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    put(
                        "createdAt",
                        DateTimeFormatter.ISO_INSTANT.format(request.createdAt.toInstant())
                    )
                    put(
                        "updateAt",
                        DateTimeFormatter.ISO_INSTANT.format(request.updatedAt.toInstant())
                    )
                } else {
                    put("createdAt", request.createdAt)
                    put("updateAt", request.updatedAt)
                }

            }.toString()
        }


        // ✅ Upload using ID
        @JvmStatic
        fun mediaUpload(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    val file = File(request!!.filePath)
                    if (!file.exists()) {
                        callback.onFailure("File not found")
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
                                callback.onSuccess(response.toJson())
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
                                callback.onFailure(mainResponse.toString())
                            }
                        })
                    } catch (exception: Exception) {
                        // Handle start to upload error
                        //throw Exception("Error starting to upload the file")
                        callback.onFailure("Error starting to upload the file")
                    }

                }

            } catch (e: Exception) {
                callback.onFailure("Upload failed: ${e.message}")
            }
        }

        // ✅ Search (with/without pagination)
        @JvmStatic
        fun search(context: Context, tag: String, mediaFileTypeValue: MediaFileType, isLibrary: Boolean, size: Int, mediaCallback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
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
                        }
                        else if (mediaFileType == TruVideoSdkMediaFileType.Audio) {
                            TruvideoSdkMediaFileType.AUDIO
                        }
                        else if (mediaFileType == TruVideoSdkMediaFileType.Document) {
                            TruvideoSdkMediaFileType.PDF
                        }
                        else {
                            TruvideoSdkMediaFileType.All
                        }

                    val result = TruvideoSdkMedia.search(tags, mediaType, isLibrary, size)
                    val gson = Gson()
                    val jsonResult = gson.toJson(result)
                    mediaCallback.onSuccess(jsonResult)
                    } catch (e: Exception) {
                        mediaCallback.onFailure("Search failed:")
                    }
                }

            } catch (e: Exception) {
                mediaCallback.onFailure("Search failed:")
            }
        }

        @JvmStatic
        fun search(context: Context, tag: String, mediaFileTypeValue: MediaFileType, isLibrary: Boolean, pageNumber: Int, size: Int, mediaCallback: MediaCallback) {

            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
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
                        }
                        else if (mediaFileType == TruVideoSdkMediaFileType.Audio) {
                            TruvideoSdkMediaFileType.AUDIO
                        }
                        else if (mediaFileType == TruVideoSdkMediaFileType.Document) {
                            TruvideoSdkMediaFileType.PDF
                        }
                        else {
                            TruvideoSdkMediaFileType.All
                        }

                    val result =
                        TruvideoSdkMedia.search(tags, mediaType, isLibrary, pageNumber, size)
                    val gson = Gson()
                    val jsonResult = gson.toJson(result)
                    mediaCallback.onSuccess(jsonResult)
                    } catch (e: Exception) {
                        mediaCallback.onFailure("Search failed:")
                    }
                }
            } catch (e: Exception) {
                mediaCallback.onFailure("Search failed:")
            }
        }

        // ✅ Get request by ID
        @JvmStatic
         fun getFileUploadRequestById(id: String, callback: MediaCallback) {
            try{
                CoroutineScope(Dispatchers.Main).launch {
                    try{
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    if(request == null){
                        callback.onSuccess("{}")
                    }else{
                        val mainResponse = returnRequest(request)
                        callback.onSuccess(mainResponse)
                    }
                    }catch (e: Exception){
                        callback.onFailure("Get request failed: ${e.message}")
                    }
                }
            }catch (e: Exception){
                callback.onFailure("Get request failed: ${e.message}")
            }
        }

        // ✅ Get all requests
        @JvmStatic
        fun getAllRequests(statusValue: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {

                    if (statusValue == "") {
                        val request = TruvideoSdkMedia.getAllFileUploadRequests()
                        callback.onSuccess("" + request)
                    } else {
                        val mainStatus: TruvideoSdkMediaFileUploadStatus? = when (statusValue) {
                            "UPLOADING" -> TruvideoSdkMediaFileUploadStatus.UPLOADING
                            "IDLE" -> TruvideoSdkMediaFileUploadStatus.IDLE
                            "ERROR" -> TruvideoSdkMediaFileUploadStatus.ERROR
                            "PAUSED" -> TruvideoSdkMediaFileUploadStatus.PAUSED
                            "COMPLETED" -> TruvideoSdkMediaFileUploadStatus.COMPLETED
                            "CANCELED" -> TruvideoSdkMediaFileUploadStatus.CANCELED
                            "SYNCHRONIZING" -> TruvideoSdkMediaFileUploadStatus.SYNCHRONIZING
                            else -> null
                        }
                        val request = TruvideoSdkMedia.getAllFileUploadRequests(mainStatus)
                        callback.onSuccess("" + request)
                    }
    
                }

            } catch (e: Exception) {
                callback.onFailure("GetAllRequests failed: ${e.message}")
            }
        }

        // ✅ Status
        @JvmStatic
        fun status(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    val status = request!!.status
                    callback.onSuccess("" + status)
                }
            } catch (e: Exception) {
                callback.onFailure("Status failed: ${e.message}")
            }
        }

        // ✅ Cancel
        @JvmStatic
        fun cancel(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    request!!.cancel()
                    callback.onSuccess("Request Cancel")
                }
            } catch (e: Exception) {
                callback.onFailure("Cancel failed: ${e.message}")
            }
        }

        // ✅ Delete
        @JvmStatic
        fun delete(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    request!!.delete()
                    callback.onSuccess("Request delete")
                }
            } catch (e: Exception) {
                callback.onFailure("Delete failed: ${e.message}")
            }
        }

        // ✅ Pause
        @JvmStatic
        fun pause(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                 try {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    request!!.pause()
                    callback.onSuccess("Request pause")
                    
                    } catch (e: Exception) {
                                    callback.onFailure("Pause failed")
                     }
                }

            } catch (e: Exception) {
                callback.onFailure("Pause failed")
            }
        }
        
        // ✅ Resume
                @JvmStatic
                fun resume(id: String, callback: MediaCallback) {
                    try {
                        CoroutineScope(Dispatchers.Main).launch {
                         try {
                            val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                            request!!.resume()
                            callback.onSuccess("Request Resume")
                            } catch (e: Exception) {
                                                    callback.onFailure("Resume failed")
                                                }
                        }
                    } catch (e: Exception) {
                        callback.onFailure("Resume failed")
                    }
                }

        // ✅ Stream All
        @JvmStatic
        fun streamAllRequests(context: Context, callback: MediaCallback) {
            try {
                val allRequest = TruvideoSdkMedia.streamAllFileUploadRequests()
                val gson = Gson()
                val jsonResult = gson.toJson(allRequest)
                callback.onSuccess(jsonResult)

            } catch (e: Exception) {
                callback.onFailure("Stream failed: ${e.message}")
            }
        }

    }
}