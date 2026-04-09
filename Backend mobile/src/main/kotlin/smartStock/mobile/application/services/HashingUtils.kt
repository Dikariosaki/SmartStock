package smartStock.mobile.application.services

import java.security.MessageDigest

object HashingUtils {
    fun md5Hex(input: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
