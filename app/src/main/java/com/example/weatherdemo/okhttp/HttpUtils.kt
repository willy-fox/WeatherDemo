package com.example.weatherdemo.okhttp

import com.example.weatherdemo.BuildConfig
import com.example.weatherdemo.MyApplication
import com.example.weatherdemo.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class HttpUtils {
    /**
     * 连接超时时间
     */
    var CONNECT_TIMEOUT: Long = 10

    /**
     * 写入超时时间
     */
    var WRITE_TIME: Long = 30

    /**
     * 读出超时时间
     */
    var READ_TIME: Long = 30

    /**
     * 是否开启https
     */
    var IS_OPEN_HTTPS = false
    companion object{
        private var httpUtils:HttpUtils?=null
        @Synchronized
        fun getHttpServerApi():HttpService{
            return createLiteServiceApi(BuildConfig.HostName)
        }
        @Synchronized
        fun createLiteServiceApi(hostName:String):HttpService{
            httpUtils= HttpUtils()
            var okHttpClient:OkHttpClient?= httpUtils?.createOkClient(hostName.contains("https"))

        }

    }
    //创建client对象
    public fun createOkClient(isOpenHttps:Boolean=true, isNeedLogger:Boolean=true,isGzip:Boolean=true):OkHttpClient{
        var sslParams:HttpsUtils.Companion.SSLParams?=null
        if(isOpenHttps){
            val certificates=MyApplication.getInstance()?.resources?.openRawResource(R.raw.server)
            val pkcs12File=MyApplication.getInstance()?.resources?.assets?.open("client.p12")
            val bksFile=pkcs12ToBks(pkcs12File,HttpsUtils.HTTPS_P12_PASSWORD)
            sslParams=HttpsUtils.getSslSocketFactory(arrayOf(certificates),bksFile,HttpsUtils.HTTPS_P12_PASSWORD)
        }
        var build=OkHttpClient.Builder().apply {
            addInterceptor{ chain ->
                val originalRequest=chain.request()
                val build1=originalRequest.newBuilder().apply {
                    if(isGzip){
                        addHeader("accept-encoding", "gzip")
                    }
                    MyApplication.getInstance()?.getToken()?.let {
                        addHeader("TOKEN",it)
                    }
                    addHeader("version","1.0.0")
                    addHeader("device","0")
                    addHeader("deviceId","");
                    addHeader("sendTime",System.currentTimeMillis().toString())
                }
                chain.proceed(build1.build())
            }
            if(BuildConfig.DEBUG&&isNeedLogger) {
                val logginInterceptor = HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                }
                addInterceptor(logginInterceptor)
            }
            connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS)
            readTimeout(READ_TIME,TimeUnit.SECONDS)
            writeTimeout(WRITE_TIME,TimeUnit.SECONDS)
            if(isOpenHttps&&sslParams!=null&&sslParams.sslSocketFactory!=null&&sslParams.trustManager!=null){
                sslSocketFactory(sslParams.sslSocketFactory!!,sslParams.trustManager!!)
            }
        }
            return build.build();
        }

    protected fun pkcs12ToBks(pkcs12Stream: InputStream?, pkcs12Password: String): InputStream? {
        val password = pkcs12Password.toCharArray()
        try {
            val pkcs12 = KeyStore.getInstance("PKCS12")
            pkcs12.load(pkcs12Stream, password)
            val aliases = pkcs12.aliases()
            val alias: String
            alias = if (aliases.hasMoreElements()) {
                aliases.nextElement()
            } else {
                throw Exception("pkcs12 file not contain a alias")
            }
            val certificate = pkcs12.getCertificate(alias)
            val key = pkcs12.getKey(alias, password)
            val bks = KeyStore.getInstance("BKS").apply {
                load(null, password)
                setKeyEntry(alias, key, password, arrayOf(certificate))
            }
            val out = ByteArrayOutputStream()
            bks.store(out, password)
            return ByteArrayInputStream(out.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
