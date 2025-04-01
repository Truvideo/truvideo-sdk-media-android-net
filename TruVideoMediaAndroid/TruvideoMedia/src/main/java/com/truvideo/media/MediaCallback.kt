package com.truvideo.media

interface MediaCallback {
    fun onSuccess(result: String?)
    fun onFailure(error: String?)
}