package au.edu.jcu.zhangfan.primoria.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds the bearer token at request time so a key entered in Settings applies immediately. */
class AuthInterceptor @Inject constructor(
    private val keyProvider: ApiKeyProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = keyProvider.currentKey()
        val request = if (key.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder().header("Authorization", "Bearer $key").build()
        }
        return chain.proceed(request)
    }
}
