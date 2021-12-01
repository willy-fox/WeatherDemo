package com.example.weatherdemo.okhttp

import com.example.weatherdemo.BuildConfig

class HttpHelp private constructor(){
    val mLiteService:HttpService
    init {
        mLiteService=HttpUtils.getHttpServerApi()
    }
    companion object{
         val mHttpHelp: HttpHelp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ HttpHelp() }
    }
}