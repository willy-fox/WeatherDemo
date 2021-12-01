package com.example.weatherdemo.okhttp

import java.io.IOException
import java.io.InputStream
import java.lang.AssertionError
import java.lang.Exception
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

class HttpsUtils {
    companion object{
        val HTTPS_P12_PASSWORD=""
        public class SSLParams(){
            public var sslSocketFactory: SSLSocketFactory?=null
            public var trustManager: X509TrustManager?=null
        }
        fun getSslSocketFactory(
            certificates: Array<InputStream?>,
            bksFile: InputStream?,
            password: String?
        ): SSLParams {
            val sslParams = SSLParams()
            return try {
                val trustManagers: Array<TrustManager>? = prepareTrustManager(*certificates)
                val keyManagers: Array<KeyManager>? = prepareKeyManager(bksFile, password)
                val sslContext = SSLContext.getInstance("TLS")
                var trustManager: X509TrustManager? = null
                trustManager = if (trustManagers != null) {
                    MyTrustManager(chooseTrustManager(trustManagers))
                } else {
                    UnSafeTrustManager()
                }
                sslContext.init(keyManagers, arrayOf<TrustManager?>(trustManager), null)
                sslParams.sslSocketFactory = sslContext.socketFactory
                sslParams.trustManager = trustManager
                sslParams
            } catch (e: NoSuchAlgorithmException) {
                throw AssertionError(e)
            } catch (e: KeyManagementException) {
                throw AssertionError(e)
            } catch (e: KeyStoreException) {
                throw AssertionError(e)
            }
        }
        private class UnSafeTrustManager : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        private fun prepareTrustManager(vararg certificates: InputStream?): Array<TrustManager>? {
            if (certificates == null || certificates.size <= 0) return null
            try {
                val certificateFactory =
                    CertificateFactory.getInstance("X.509")
                val keyStore =
                    KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null)
                var index = 0
                for (certificate in certificates) {
                    val certificateAlias = Integer.toString(index++)
                    keyStore.setCertificateEntry(
                        certificateAlias,
                        certificateFactory.generateCertificate(certificate)
                    )
                    try {
                        certificate?.close()
                    } catch (e: IOException) {
                    }
                }
                var trustManagerFactory: TrustManagerFactory? = null
                trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)
                return trustManagerFactory.trustManagers
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun prepareKeyManager(
            bksFile: InputStream?,
            password: String?
        ): Array<KeyManager>? {
            try {
                if (bksFile == null || password == null) return null
                val clientKeyStore = KeyStore.getInstance("BKS")
                clientKeyStore.load(bksFile, password.toCharArray())
                val keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                keyManagerFactory.init(clientKeyStore, password.toCharArray())
                return keyManagerFactory.keyManagers
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: UnrecoverableKeyException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
        private fun chooseTrustManager(trustManagers: Array<TrustManager>?): X509TrustManager? {
            trustManagers?.let {
                for (trustManager in it) {
                    if (trustManager is X509TrustManager) {
                        return trustManager
                    }
                }
            }
            return null
        }


        private class MyTrustManager(localTrustManager: X509TrustManager?) :
            X509TrustManager {
            private val defaultTrustManager: X509TrustManager?
            private val localTrustManager: X509TrustManager?

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    defaultTrustManager!!.checkServerTrusted(chain, authType)
                } catch (ce: CertificateException) {
                    localTrustManager?.checkServerTrusted(chain, authType)
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }

            init {
                val var4 =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                var4.init(null as KeyStore?)
                defaultTrustManager = chooseTrustManager(var4.trustManagers)
                this.localTrustManager = localTrustManager
            }
        }
    }


}