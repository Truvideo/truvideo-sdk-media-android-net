package com.truvideo.media


import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.truvideo.sdk.media.TruvideoSdkMedia
//import com.truvideo.sdk.media.TruvideoSdkMediaInitializer
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
//import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
//import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
//import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
//import com.truvideo.sdk.media.model.TruvideoSdkMediaTags
import com.truvideo.sdk.media.model.external.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.model.external.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.external.TruvideoSdkMediaFileUploadRequestStatus
import com.truvideo.sdk.media.model.external.TruvideoSdkMediaMetadata
import com.truvideo.sdk.media.model.external.TruvideoSdkMediaTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
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
                // AppInitializer.getInstance(context)
                //  .initializeComponent(TruvideoSdkMediaInitializer::class.java)
                callback.onSuccess("Media Initializer")
            }
        }

        // ✅ Upload multiple files
        @JvmStatic
        fun uploadMedia(
            context: Context,
            tag: String,
            metadata: String,
            arrayList: List<String>,
            mediaCallback: MediaCallback
        ) {
            // AppInitializer.getInstance(context)
            //  .initializeComponent(TruvideoSdkMediaInitializer::class.java)
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
                            mainCallback?.onSuccess(response.toString())
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
                    mainCallback?.onFailure(
                        "Error starting to upload the file")
                }

            }
        }

        // ✅ Build media request
        @JvmStatic
        fun mediaBuilder(filePath: String, tag: String, metaData: String, callback: MediaCallback) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    callback.onFailure(
                        "File not found")
                }
                CoroutineScope(Dispatchers.Main).launch {
                    builder(filePath, tag, metaData, callback)
                }
            } catch (e: Exception) {
                mainCallback?.onFailure("Builder failed:")
            }
        }

        private fun builder(
            filePath: String,
            tag: String,
            metaData: String,
            callback: MediaCallback
        ) {
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
                callback.onFailure(
                    "Builder failed: ${e.message}"
                )
            }
        }

        // ✅ Convert request to JSON
        /* private fun returnRequest(request: TruvideoSdkMediaFileUploadRequest): String {
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
         }*/

        private fun returnRequest(request: TruvideoSdkMediaFileUploadRequest): String {
            return JSONObject().apply {
                put("id", request.id)
                put("filePath", request.filePath)
                put("fileType", request.fileType)
                put("tags", request.tags)
                put("metadata", request.metadata)
                put("durationMilliseconds", request.durationMilliseconds)
                put("remoteId", request.mediaId)
                put("remoteURL", request.mediaUrl)
                put("transcriptionURL", request.transcriptionUrl)
                put("transcriptionLength", "")
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
                /*fun mediaUpload(id: String, callback: MediaCallback) {
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
                }*/

        fun mediaUpload(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                    val file = File(request!!.filePath)
                    if (!file.exists()) {
                        callback.onFailure(
                            "File not found"
                        )
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
                                callback.onSuccess(response.toString())
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
                                callback.onFailure(
                                    mainResponse.toString()
                                )
                            }
                        })
                    } catch (exception: Exception) {
                        // Handle start to upload error
                        throw Exception("Error starting to upload the file")
                    }

                }

            } catch (e: Exception) {
                callback.onFailure(
                    "Upload failed: ${e.message}"
                )
            }
        }

        // ✅ Search (with/without pagination)
        @JvmStatic
        fun search(
            context: Context,
            tag: String,
            mediaFileTypeValue: MediaFileType,
            isLibrary: Boolean,
            size: Int,
            mediaCallback: MediaCallback
        ) {
            /*try {
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
            }*/

            try {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        val tagJson = JSONObject(tag)
                        val map = mutableMapOf<String, String>()
                        val keys = tagJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            map[key] = tagJson.getString(key)
                        }

                        // Convert map to List of Entries (fix ClassCastException)
                        val tagEntries = map.map { TruvideoSdkMediaTags.Entry(it.key, it.value) }
                        val tags = TruvideoSdkMediaTags(tagEntries)
                        val mediaFileType = MediaFileTypeConverter.mediaFileType(mediaFileTypeValue)
                        val mediaType: TruvideoSdkMediaFileType? =
                            when (mediaFileType) {

                                TruVideoSdkMediaFileType.IMAGE ->
                                    TruvideoSdkMediaFileType.IMAGE

                                TruVideoSdkMediaFileType.VIDEO ->
                                    TruvideoSdkMediaFileType.VIDEO

                                TruVideoSdkMediaFileType.DOCUMENT ->
                                    TruvideoSdkMediaFileType.DOCUMENT

                                TruVideoSdkMediaFileType.AUDIO ->
                                    TruvideoSdkMediaFileType.AUDIO

                                else -> null
                            }
                        try {
                            val result = TruvideoSdkMedia.search(tags, mediaType, isLibrary, size)
                            val gson = Gson()
                            val jsonResult = gson.toJson(result)
                            mediaCallback.onSuccess(jsonResult)

                        } catch (e: Exception) {
                            mediaCallback.onFailure(
                                "Search failed1: ${e.message}"
                            )
                        }

//
                    } catch (e: Exception) {
                        mediaCallback.onFailure(
                            "Search failed1: ${e.message}"
                        )
                    }
                }


            } catch (e: Exception) {
                mediaCallback.onFailure(
                    "Search failed: ${e.message}"
                )
            }
        }

        @JvmStatic
        fun search(
            context: Context,
            tag: String,
            mediaFileTypeValue: MediaFileType,
            isLibrary: Boolean,
            pageNumber: Int,
            size: Int,
            mediaCallback: MediaCallback
        ) {
            try {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        // Parse JSON string to map
                        val tagJson = JSONObject(tag)
                        val map = mutableMapOf<String, String>()
                        val keys = tagJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            map[key] = tagJson.getString(key)
                        }

                        // Convert map to List of Entries (fix ClassCastException)
                        val tagEntries = map.map { TruvideoSdkMediaTags.Entry(it.key, it.value) }
                        val tags = TruvideoSdkMediaTags(tagEntries)

                        // Convert media type
                        val mediaFileType = MediaFileTypeConverter.mediaFileType(mediaFileTypeValue)
                        val mediaType: TruvideoSdkMediaFileType? =
                            when (mediaFileType) {

                                TruVideoSdkMediaFileType.IMAGE ->
                                    TruvideoSdkMediaFileType.IMAGE

                                TruVideoSdkMediaFileType.VIDEO ->
                                    TruvideoSdkMediaFileType.VIDEO

                                TruVideoSdkMediaFileType.DOCUMENT ->
                                    TruvideoSdkMediaFileType.DOCUMENT

                                TruVideoSdkMediaFileType.AUDIO ->
                                    TruvideoSdkMediaFileType.AUDIO

                                else -> null
                            }
                        try {

                            // Perform search
                            val result = withContext(Dispatchers.IO) {
                             TruvideoSdkMedia.search(
                                tags,
                                mediaType,
                                isLibrary,
                                pageNumber,
                                size
                            )
                            }
                            val gson = Gson()
                            val jsonResult = gson.toJson(result)
                            mediaCallback.onSuccess(jsonResult)
                        } catch (e: Exception) {
                            mediaCallback.onFailure(
                                "Search failed1: ${e.message}"
                            )
                        }
                    } catch (e: Exception) {
                        mediaCallback.onFailure(
                            "Search failed1: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                mediaCallback.onFailure(
                    "Search failed: ${e.message}"
                )
            }
        }



        @JvmStatic
        fun searchByID(
            context: Context,
            id: String,
            mediaCallback: MediaCallback
        ) {
            try {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        try {

                            // Perform search
                            val result = withContext(Dispatchers.IO) {
                                TruvideoSdkMedia.searchById(id)
                            }
                            val gson = Gson()
                            val jsonResult = gson.toJson(result)
                            mediaCallback.onSuccess(jsonResult)
                        } catch (e: Exception) {
                            mediaCallback.onFailure(
                                "Search failed1: ${e.message}"
                            )
                        }
                    } catch (e: Exception) {
                        mediaCallback.onFailure(
                            "Search failed1: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                mediaCallback.onFailure(
                    "Search failed: ${e.message}"
                )
            }
        }

        /*@JvmStatic
        fun search(context: Context, tag: String, mediaFileTypeValue: MediaFileType, isLibrary: Boolean, pageNumber: Int, size: Int, mediaCallback: MediaCallback) {

           *//* try {
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
            }*//*

            try {
                CoroutineScope(Dispatchers.Main).launch {
                    val tagJson = JSONObject(tag)
                    val map = mutableMapOf<String, String>()
                    val keys = tagJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        map[key] = tagJson.getString(key)
                    }
                    val tags = TruvideoSdkMediaTags(map as List<TruvideoSdkMediaTags.Entry>)
                    val mediaFileType = MediaFileTypeConverter.mediaFileType(mediaFileTypeValue)
                    val mediaType: TruvideoSdkMediaFileType? =
                        when (mediaFileType) {

                            TruVideoSdkMediaFileType.Picture ->
                                TruvideoSdkMediaFileType.IMAGE

                            TruVideoSdkMediaFileType.Video ->
                                TruvideoSdkMediaFileType.VIDEO

                            TruVideoSdkMediaFileType.Document ->
                                TruvideoSdkMediaFileType.DOCUMENT

                            TruVideoSdkMediaFileType.Audio ->
                                TruvideoSdkMediaFileType.AUDIO

                            else ->
                                TruvideoSdkMediaFileType.IMAGE
                        }
                    val result =
                        TruvideoSdkMedia.search(tags, mediaType, isLibrary, pageNumber, size)
                    val gson = Gson()
                    val jsonResult = gson.toJson(result)
                    mediaCallback.onSuccess(jsonResult)
                }
            } catch (e: Exception) {
                mediaCallback.onFailure("Search failed")
            }
        }
*/
        // ✅ Get request by ID
        @JvmStatic
        fun getFileUploadRequestById(id: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getFileUploadRequestById(id)
                        if (request == null) {
                            callback.onSuccess("{}")
                        } else {
                            val mainResponse = returnRequest(request)
                            callback.onSuccess(mainResponse)
                        }
                    } catch (e: Exception) {
                        callback.onFailure("Get request failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                callback.onFailure("Get request failed: ${e.message}")
            }
        }

        // ✅ Get all requests
        @JvmStatic
        fun getAllRequests(statusValue: String, callback: MediaCallback) {
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try{

                    /* if (statusValue == "") {
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
                     }*/

                    if (statusValue == "") {
                        val request = TruvideoSdkMedia.getAllFileUploadRequests()
                        //callback.onSuccess("" + request)
                        val gson = Gson()
                        val jsonResult = gson.toJson(request)
                        callback.onSuccess(jsonResult)
                    } else {
                        val mainStatus: TruvideoSdkMediaFileUploadRequestStatus? =
                            when (statusValue) {
                                "UPLOADING" -> TruvideoSdkMediaFileUploadRequestStatus.UPLOADING
                                "IDLE" -> TruvideoSdkMediaFileUploadRequestStatus.IDLE
                                "ERROR" -> TruvideoSdkMediaFileUploadRequestStatus.ERROR
                                "PAUSED" -> TruvideoSdkMediaFileUploadRequestStatus.PAUSED
                                "COMPLETED" -> TruvideoSdkMediaFileUploadRequestStatus.COMPLETED
                                "CANCELED" -> TruvideoSdkMediaFileUploadRequestStatus.CANCELED
                                "SYNCHRONIZING" -> TruvideoSdkMediaFileUploadRequestStatus.SYNCHRONIZING
                                else -> null
                            }
                        val request = TruvideoSdkMedia.getAllFileUploadRequests(mainStatus)
                      //  Log.e("Rahul",""+request);

                        val gson = Gson()
                        val jsonResult = gson.toJson(request)
                        callback.onSuccess(jsonResult)
                        callback.onSuccess("" + request)
                        /*val gson = GsonBuilder()
                            .serializeNulls()
                            .create()

                        val jsonResult = gson.toJson(request)
                        callback.onSuccess(jsonResult)*/

                    }
                    } catch (e: Exception) {
                        callback.onFailure(
                            "GetAllRequests failed: ${e.message}"
                        )
                    }

                }

            } catch (e: Exception) {
                callback.onFailure(
                    "GetAllRequests failed: ${e.message}"
                )
            }
        }


       /* fun getAllStatusMergedFlat(callback: MediaCallback) {

            CoroutineScope(Dispatchers.Main).launch {

                try {

                    val allStatuses = listOf(
                        "IDLE",
                        "UPLOADING",
                        "SYNCHRONIZING",
                        "ERROR",
                        "COMPLETED",
                        "PAUSED",
                        "CANCELED"
                    )

                    val finalList = mutableListOf<Any>()

                    for (status in allStatuses) {

                        val data = withContext(Dispatchers.IO) {

                            val mainStatus = when (status) {
                                "UPLOADING" -> TruvideoSdkMediaFileUploadRequestStatus.UPLOADING
                                "IDLE" -> TruvideoSdkMediaFileUploadRequestStatus.IDLE
                                "ERROR" -> TruvideoSdkMediaFileUploadRequestStatus.ERROR
                                "PAUSED" -> TruvideoSdkMediaFileUploadRequestStatus.PAUSED
                                "COMPLETED" -> TruvideoSdkMediaFileUploadRequestStatus.COMPLETED
                                "CANCELED" -> TruvideoSdkMediaFileUploadRequestStatus.CANCELED
                                "SYNCHRONIZING" -> TruvideoSdkMediaFileUploadRequestStatus.SYNCHRONIZING
                                else -> null
                            }

                            if (mainStatus == null) {
                                emptyList()
                            } else {
                                TruvideoSdkMedia.getAllFileUploadRequests(mainStatus)
                            }
                        }

                        finalList.addAll(data)
                    }

                    // convert to SINGLE flat JSON array
                    val jsonResult = GsonBuilder()
                        .create()
                        .toJson(finalList)

                    callback.onSuccess(jsonResult)

                } catch (e: Exception) {
                    callback.onFailure("Error: ${e.message}")
                }
            }
        }
*/

        /*fun fetchAllStatusRequests(callback: MediaCallback) {

            CoroutineScope(Dispatchers.Main).launch {

                val allStatuses = listOf(
                    "IDLE",
                    "UPLOADING",
                    "SYNCHRONIZING",
                    "ERROR",
                    "COMPLETED",
                    "PAUSED",
                    "CANCELED"
                )

                try {

                    val finalResult = mutableMapOf<String, String>()

                    for (status in allStatuses) {

                        val response = withContext(Dispatchers.IO) {

                            val mainStatus = when (status) {
                                "UPLOADING" -> TruvideoSdkMediaFileUploadRequestStatus.UPLOADING
                                "IDLE" -> TruvideoSdkMediaFileUploadRequestStatus.IDLE
                                "ERROR" -> TruvideoSdkMediaFileUploadRequestStatus.ERROR
                                "PAUSED" -> TruvideoSdkMediaFileUploadRequestStatus.PAUSED
                                "COMPLETED" -> TruvideoSdkMediaFileUploadRequestStatus.COMPLETED
                                "CANCELED" -> TruvideoSdkMediaFileUploadRequestStatus.CANCELED
                                "SYNCHRONIZING" -> TruvideoSdkMediaFileUploadRequestStatus.SYNCHRONIZING
                                else -> null
                            }

                            val request = if (mainStatus == null) {
                                TruvideoSdkMedia.getAllFileUploadRequests()
                            } else {
                                TruvideoSdkMedia.getAllFileUploadRequests(mainStatus)
                            }

                            Gson().toJson(request)
                        }

                        finalResult[status] = response

                       // Log.d("STATUS_CHECK", "Status: $status -> $response")
                    }

                    // Final combined response
                    val finalJson = Gson().toJson(finalResult)

                    callback.onSuccess(finalJson)

                } catch (e: Exception) {
                    callback.onFailure("Fetch failed: ${e.message}")
                }
            }
        }*/

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
                callback.onFailure(
                    "Status failed: ${e.message}"
                )
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
                callback.onFailure(
                    "Cancel failed: ${e.message}"
                )
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
                callback.onFailure(
                    "Delete failed: ${e.message}"
                )
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
                        callback.onFailure(
                            "Pause failed"
                        )
                    }
                }

            } catch (e: Exception) {
                callback.onFailure(
                    "Pause failed"
                )
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
                        callback.onFailure(
                            "Resume failed"
                        )
                    }
                }
            } catch (e: Exception) {
                callback.onFailure(
                    "Resume failed"
                )
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
                callback.onFailure(
                    "Stream failed: ${e.message}"
                )
            }
        }

        // ================= STREAM BY ID =================

        @JvmStatic
        fun streamRequestById(id: String, callback: MediaCallback) {

            CoroutineScope(Dispatchers.Main).launch {
                try {

                    TruvideoSdkMedia
                        .streamFileUploadRequestById(id)
                        .collect { request ->

                            if (request == null) {
                                callback.onSuccess("{}")
                            } else {
                                callback.onSuccess(returnRequest(request))
                            }
                        }

                } catch (e: Exception) {

                    callback.onFailure(
                        e.message ?: "StreamById failed"
                    )
                }
            }
        }

// ================= SEARCH BY ID =================

        @JvmStatic
        fun searchById(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = TruvideoSdkMedia.searchById(id)
                    callback.onSuccess(Gson().toJson(result))
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "SearchById failed"
                    )
                }
            }
        }

        // ================= STREAM ALL FILE UPLOAD REQUESTS =================

        @JvmStatic
        fun streamAllFileUploadRequests(callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    TruvideoSdkMedia
                        .streamAllFileUploadRequests()
                        .collect { list ->
                            callback.onSuccess(Gson().toJson(list))
                        }
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "Stream all file upload requests failed"
                    )
                }
            }
        }

        // ================= STREAM FILE UPLOAD REQUEST BY ID =================

        @JvmStatic
        fun streamFileUploadRequestById(id: String, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    TruvideoSdkMedia
                        .streamFileUploadRequestById(id)
                        .collect { item ->
                            callback.onSuccess(Gson().toJson(item))
                        }
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "StreamFileUploadRequestById failed"
                    )
                }
            }
        }

        // ================= GET ALL FILE UPLOAD REQUESTS =================

        @JvmStatic
        fun getAllFileUploadRequests(callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = TruvideoSdkMedia.getAllFileUploadRequests()
                    callback.onSuccess(Gson().toJson(result))
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "GetAllFileUploadRequests failed"
                    )
                }
            }
        }

        // ================= GET ALL UPLOAD REQUESTS =================

        @JvmStatic
        fun getAllUploadRequests(callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = TruvideoSdkMedia.getAllUploadRequests()
                    callback.onSuccess(Gson().toJson(result))
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "GetAllUploadRequests failed"
                    )
                }
            }
        }

        // ================= GET UPLOAD REQUEST BY ID =================

        @JvmStatic
        fun getUploadRequestById(id: Long, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = TruvideoSdkMedia.getUploadRequestById(id)
                    callback.onSuccess(Gson().toJson(result))
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "GetUploadRequestById failed"
                    )
                }
            }
        }

        // ================= STREAM ALL UPLOAD REQUESTS =================

        @JvmStatic
        fun streamAllUploadRequests(callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    TruvideoSdkMedia
                        .streamAllUploadRequests()
                        .collect { list ->
                            callback.onSuccess(Gson().toJson(list))
                        }
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "StreamAllUploadRequests failed"
                    )
                }
            }
        }

        // ================= STREAM UPLOAD REQUEST BY ID =================

        @JvmStatic
        fun streamUploadRequestById(id: Long, callback: MediaCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    TruvideoSdkMedia
                        .streamUploadRequestById(id)
                        .collect { item ->
                            callback.onSuccess(Gson().toJson(item))
                        }
                } catch (e: Exception) {
                    callback.onFailure(
                        e.message ?: "StreamUploadRequestById failed"
                    )
                }
            }
        }


        @JvmStatic
        fun uploadStreamUploadRequest(
            id: String,
            title: String?,
            tags: String?,
            metadata: String?,
            includeInReport: Boolean,
            isLibrary: Boolean,
            callback: MediaCallback
        ) {
            val longId = id.toLongOrNull()
            if (longId == null) {
                callback.onFailure("INVALID_ID")
                return
            }

            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getUploadRequestById(longId)
                        if (request == null) {
                           // callback.onFailure("NOT_FOUND", "Stream upload request not found for id: $id")
                            return@launch
                        }

                        val tagsObj = buildTagsFromJson(tags ?: "{}")
                        val metadataObj = buildMetadataFromJson(metadata ?: "{}")

                        request.upload(
                            title = title ?: "",
                            tags = tagsObj,
                            metadata = metadataObj,
                            includeInReport = includeInReport,
                            isLibrary = isLibrary
                        )

                        callback.onSuccess("Stream upload started")
                    } catch (e: TruvideoSdkException) {
                       // callback.onFailure("TruvideoSdkException", e.message ?: "Unknown SDK exception")
                    } catch (e: Exception) {
                        //callback.onFailure("Exception", e.message ?: "Unknown error occurred")
                    }
                }
            } catch (e: Exception) {
                callback.onFailure( e.message ?: "Failed to launch coroutine")
            }
        }



        private fun buildTagsFromJson(jsonStr: String): TruvideoSdkMediaTags {

            val entries = mutableListOf<TruvideoSdkMediaTags.Entry>()

            try {

                val json = JSONObject(jsonStr)

                json.keys().forEach { key ->

                    entries.add(TruvideoSdkMediaTags.Entry(key, json.getString(key)))

                }

            } catch (_: JSONException) { }

            return TruvideoSdkMediaTags(entries)

        }

        private fun buildMetadataFromJson(jsonStr: String): TruvideoSdkMediaMetadata {

            val builder = TruvideoSdkMediaMetadata.builder()

            try {

                val json = JSONObject(jsonStr)

                json.keys().forEach { key ->

                    builder.set(key, json.getString(key))

                }

            } catch (_: JSONException) { }

            return builder.build()

        }


        @JvmStatic
         fun pauseStreamUploadRequest(id: String, callback: MediaCallback) {
            val longId = id?.toLongOrNull()
            if (longId == null) {
                mainCallback?.onFailure("INVALID_ID")
                return
            }
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getUploadRequestById(longId)
                        request!!.pause()
                        callback.onSuccess("Request pause")

                    } catch (e: Exception) {
                        callback.onFailure(
                            "Pause failed"
                        )
                    }
                }

            } catch (e: Exception) {
                mainCallback?.onFailure(
                    "Pause failed"
                )
            }
            }


        @JvmStatic
        fun retryStreamUploadRequest(id: String, callback: MediaCallback) {
            val longId = id?.toLongOrNull()
            if (longId == null) {
                mainCallback?.onFailure("INVALID_ID")
                return
            }
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getUploadRequestById(longId)
                        request!!.retry()
                        callback.onSuccess("Request retry")

                    } catch (e: Exception) {
                        mainCallback?.onFailure(
                            "Retry failed"
                        )
                    }
                }

            } catch (e: Exception) {
                mainCallback?.onFailure(
                    "Retry failed"
                )
            }
        }


        @JvmStatic
        fun resumeStreamUploadRequest(id: String, callback: MediaCallback) {
            val longId = id?.toLongOrNull()
            if (longId == null) {
                mainCallback?.onFailure("INVALID_ID")
                return
            }
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getUploadRequestById(longId)
                        request!!.resume()
                        callback.onSuccess("Request Resume")

                    } catch (e: Exception) {
                        mainCallback?.onFailure(
                            "Resume failed"
                        )
                    }
                }

            } catch (e: Exception) {
                mainCallback?.onFailure(
                    "Resume failed"
                )
            }
        }

        @JvmStatic
        fun deleteStreamUploadRequest(id: String, callback: MediaCallback) {
            val longId = id?.toLongOrNull()
            if (longId == null) {
                mainCallback?.onFailure("INVALID_ID")
                return
            }
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val request = TruvideoSdkMedia.getUploadRequestById(longId)
                        request!!.delete()
                        callback.onSuccess("Request delete")

                    } catch (e: Exception) {
                        mainCallback?.onFailure(
                            "Delete failed"
                        )
                    }
                }

            } catch (e: Exception) {
                mainCallback?.onFailure(
                    "Delete failed"
                )
            }
        }

    }




}
