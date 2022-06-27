package ufc.erv.response

/** Dados da planta de um usuário, otimindo o dono */
@kotlinx.serialization.Serializable
data class UserPlantResponse(
    val id: Int,
    val popularName: String,
    val scientificName: String?,
    val description: String?,
)
