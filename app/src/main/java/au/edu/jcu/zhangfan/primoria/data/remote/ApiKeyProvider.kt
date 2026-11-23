package au.edu.jcu.zhangfan.primoria.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import au.edu.jcu.zhangfan.primoria.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

enum class KeySource { USER, BUILD, NONE }

interface ApiKeyProvider {
    val keySource: StateFlow<KeySource>
    fun currentKey(): String?
    fun saveUserKey(key: String)
    fun clearUserKey()
}

/**
 * Resolves the DeepSeek key: a key the learner entered in Settings wins, otherwise the
 * developer key from the git-ignored `.env` file is used.
 *
 * A user key is encrypted with an AES-GCM key that never leaves the Android Keystore and
 * is written to `noBackupFilesDir`, so it is not readable as plain text or copied to backups.
 */
@Singleton
class KeystoreApiKeyProvider @Inject constructor(
    @ApplicationContext context: Context,
) : ApiKeyProvider {

    private val file = File(context.noBackupFilesDir, "deepseek_key.bin")
    private var cachedUserKey: String? = readUserKey()

    private val _keySource = MutableStateFlow(resolveSource())
    override val keySource: StateFlow<KeySource> = _keySource.asStateFlow()

    override fun currentKey(): String? =
        cachedUserKey ?: BuildConfig.DEEPSEEK_API_KEY.takeIf { it.isNotBlank() }

    override fun saveUserKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val encrypted = cipher.iv + cipher.doFinal(trimmed.toByteArray())
        file.writeText(Base64.encodeToString(encrypted, Base64.NO_WRAP))
        cachedUserKey = trimmed
        _keySource.value = resolveSource()
    }

    override fun clearUserKey() {
        file.delete()
        cachedUserKey = null
        _keySource.value = resolveSource()
    }

    private fun resolveSource(): KeySource = when {
        cachedUserKey != null -> KeySource.USER
        BuildConfig.DEEPSEEK_API_KEY.isNotBlank() -> KeySource.BUILD
        else -> KeySource.NONE
    }

    private fun readUserKey(): String? = runCatching {
        if (!file.exists()) return null
        val bytes = Base64.decode(file.readText(), Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        }
        String(cipher.doFinal(bytes.copyOfRange(IV_SIZE, bytes.size)))
    }.getOrNull()

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "primoria_deepseek_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
    }
}
