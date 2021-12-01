package com.example.weatherdemo

import android.app.Application
import com.qweather.sdk.view.HeConfig

class MyApplication :Application() {
    companion object{
       private var myApplication:MyApplication?=null
        public fun getInstance()= myApplication

    }
    override fun onCreate() {
        super.onCreate()
        myApplication=this
        initWeatherConfig()
    }

    fun initWeatherConfig(){
        HeConfig.init(BuildConfig.HEPubId,BuildConfig.HEKey)
        if(BuildConfig.DEBUG)
            HeConfig.switchToDevService()//开发版
    }
    fun getToken():String{
        return ""
    }
}
