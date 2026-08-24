package com.truvideo.media

data class UploadRequestDto(
    val id: String?,
    val filePath: String?,
    val durationMilliseconds: Long?,
    val errorMessage: String?,
    val mediaId: String?,
    val mediaUrl: String?,
    val uploadProgress: Int?,
    val transcriptionUrl: String?,
    val deleteOnComplete: Boolean?,
    val includeInReport: Boolean?,
    val isLibrary: Boolean?,
    val fileType: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UploadRequestResponse(
    val id: Long,
    val tag: String?,
    val status: String,
    val type: String,
    val progress: Float,
    val mediaId: String?,
    val thumbnailPath: String?,
    val contentType: String?,
    val fileExtension: String?,
    val duration: Long,
    val title: String?,
    val errorMessage: String?,
    val includeInReport: Boolean,
    val isLibrary: Boolean,
    val markForCompletion: Boolean,
    val tags: List<String>,
    val metadata: Map<String, String>,
    val initializationMetrics: OperationMetricsResponse?,
    val completionMetrics: OperationMetricsResponse?,
    val fileUploadMetrics: OperationMetricsResponse?,
    val metrics: OperationMetricsResponse?,
    val parts: List<UploadRequestPartResponse>,
    val createdAt: String?,
    val updatedAt: String?
)

data class OperationMetricsResponse(
    val startedAt: String?,
    val endedAt: String?,
    val completed: Boolean
)

data class UploadRequestPartResponse(
    val index: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val metrics: OperationMetricsResponse?
)

data class UploadStramRequestResponse(
    val id: String,
    val tag: String,
    val status: String,
    val type: String,
    val contentType: String,
    val fileExtension: String,
    val duration: Long,
    val errorMessage: String,
    val progress: Float,
    val thumbnailPath: String?,
    val mediaId: String,
    val title: String,
    val includeInReport: Boolean,
    val isLibrary: Boolean,
    val markForCompletion: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)