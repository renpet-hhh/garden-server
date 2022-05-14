package ufc.erv.data

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: String,
    var popularName: String,
    var scientificName: String = "",
    var description: String = "",
    var localization: String = "",
)
