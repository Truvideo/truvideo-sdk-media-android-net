package com.truvideo.media

object MediaFileTypeConverter {

   @JvmStatic
       fun mediaFileType(orientation: MediaFileType?): TruVideoSdkMediaFileType {
       return when (orientation) {
           MediaFileType.IMAGE -> TruVideoSdkMediaFileType.IMAGE
           MediaFileType.VIDEO -> TruVideoSdkMediaFileType.VIDEO
           MediaFileType.AUDIO -> TruVideoSdkMediaFileType.AUDIO
           MediaFileType.DOCUMENT -> TruVideoSdkMediaFileType.DOCUMENT
           MediaFileType.ALL -> TruVideoSdkMediaFileType.ALL
           else -> TruVideoSdkMediaFileType.IMAGE // default
       }
   }
}