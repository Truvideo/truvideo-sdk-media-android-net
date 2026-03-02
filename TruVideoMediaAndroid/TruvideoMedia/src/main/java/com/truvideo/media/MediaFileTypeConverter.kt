package com.truvideo.media

object MediaFileTypeConverter {

   @JvmStatic
       fun mediaFileType(orientation: MediaFileType?): TruVideoSdkMediaFileType {
       return when (orientation) {
           MediaFileType.Picture -> TruVideoSdkMediaFileType.Picture
           MediaFileType.Video -> TruVideoSdkMediaFileType.Video
           MediaFileType.All -> TruVideoSdkMediaFileType.All
           MediaFileType.Audio -> TruVideoSdkMediaFileType.Audio
           MediaFileType.Document -> TruVideoSdkMediaFileType.Document
           else -> TruVideoSdkMediaFileType.All // default
       }
   }
}