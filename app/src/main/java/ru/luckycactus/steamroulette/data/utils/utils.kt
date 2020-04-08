package ru.luckycactus.steamroulette.data.utils

import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import ru.luckycactus.steamroulette.data.net.Tls12SocketFactory
import ru.luckycactus.steamroulette.presentation.utils.onApiAtLeast
import ru.luckycactus.steamroulette.presentation.utils.onApiLower
import javax.net.ssl.SSLContext

fun enableTls12OnOldApis(client: OkHttpClient.Builder): OkHttpClient.Builder? {
    onApiAtLeast(16) {
        onApiLower(22) {
            try {
                val sc: SSLContext = SSLContext.getInstance("TLSv1.2")
                sc.init(null, null, null)
                client.sslSocketFactory(
                    Tls12SocketFactory(
                        sc.socketFactory
                    )
                )
                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build()
                val specs: MutableList<ConnectionSpec> = ArrayList()
                specs.add(cs)
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)
                client.connectionSpecs(specs)
            } catch (exc: java.lang.Exception) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
            }
        }
    }
    return client
}