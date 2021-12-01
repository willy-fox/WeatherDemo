package com.example.weatherdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.weatherdemo.databinding.ActivityMainBinding
import com.example.weatherdemo.okhttp.HttpHelp

class MainActivity : AppCompatActivity() {
    lateinit var mBinding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        loadingData()
    }
    fun loadingData(){
        HttpHelp.mHttpHelp
    }
}