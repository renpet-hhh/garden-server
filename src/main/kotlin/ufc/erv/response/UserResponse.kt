package ufc.erv.response

/** Dados do usuário de um usuário, omitindo id */
@kotlinx.serialization.Serializable
data class UserProfileResponse(
    val name: String,
    val email: String,
    val state: String,
    val city: String,
)