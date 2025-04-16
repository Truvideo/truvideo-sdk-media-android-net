package com.truvideo.media

enum class MediaFileUploadStatus {
    IDLE,

    UPLOADING,

    SYNCHRONIZING,

    ERROR,

    COMPLETED,

    PAUSED,

    CANCELED;
}