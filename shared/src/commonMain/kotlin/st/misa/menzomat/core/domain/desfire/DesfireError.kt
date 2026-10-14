package st.misa.menzomat.core.domain.desfire

import st.misa.menzomat.core.domain.util.hex

/** A DESFire status code other than OK, or a response that is not DESFire at all. */
sealed class DesfireError(message: String) : Exception(message) {
    class PermissionDenied : DesfireError("Permission denied (0x9D)")
    class FileNotFound : DesfireError("File not found (0xF0)")
    class ApplicationNotFound : DesfireError("Application not found (0xA0)")
    class AuthenticationRequired : DesfireError("Authentication required (0xAE)")
    class Status(val code: Int) : DesfireError("DESFire status 0x${code.hex(2)}")
    class Malformed(detail: String) : DesfireError("Not a DESFire response: $detail")

    companion object {
        fun fromStatus(code: Int): DesfireError = when (code) {
            0x9D -> PermissionDenied()
            0xF0 -> FileNotFound()
            0xA0 -> ApplicationNotFound()
            0xAE -> AuthenticationRequired()
            else -> Status(code)
        }
    }
}
