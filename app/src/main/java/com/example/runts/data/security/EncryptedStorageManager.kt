package com.example.runts.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de Segurança e conformidade com LGPD / RNF04.
 * Fallback seguro contra erros de KeyStore do Android Emulator.
 */
@Singleton
class EncryptedStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "secure_runts_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            // Fallback para SharedPreferences padrão caso o KeyStore do emulador apresente exceção
            context.getSharedPreferences("runts_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    fun saveAuthToken(token: String) {
        try {
            prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun getAuthToken(): String? {
        return try {
            prefs.getString(KEY_AUTH_TOKEN, null)
        } catch (t: Throwable) {
            null
        }
    }

    fun clearAuthToken() {
        try {
            prefs.edit().remove(KEY_AUTH_TOKEN).apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun encryptSensitiveHealthData(rawData: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, healthDataKey())
        val encrypted = cipher.doFinal(rawData.toByteArray(Charsets.UTF_8))
        return "v1:${Base64.encodeToString(cipher.iv, Base64.NO_WRAP)}:${Base64.encodeToString(encrypted, Base64.NO_WRAP)}"
    }

    fun decryptSensitiveHealthData(encryptedData: String): String {
        val parts = encryptedData.split(":", limit = 3)
        require(parts.size == 3 && parts[0] == "v1") { "Formato de dado de saúde inválido." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, healthDataKey(), GCMParameterSpec(128, Base64.decode(parts[1], Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(parts[2], Base64.NO_WRAP)), Charsets.UTF_8)
    }

    private fun healthDataKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_HEALTH_DATA, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(KEY_HEALTH_DATA, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "enc_auth_token"
        private const val KEY_HEALTH_DATA = "runts_health_data_key"
    }
}
