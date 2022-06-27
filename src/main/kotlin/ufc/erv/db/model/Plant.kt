package ufc.erv.db.model

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.nio.file.Paths

interface Plant : Entity<Plant> {
    companion object : Entity.Factory<Plant>()
    val id: Int
    var popularName: String
    var scientificName: String?
    var description: String?
    var owner: User
    var imageFileExtension: String
}

fun getPlantImagePath(owner: String, id: String) = Paths.get(getUserPath(owner), "/$id").toString()

object PlantTable : Table<Plant>("plants") {
    val id = int("id").primaryKey().bindTo { it.id }
    val popularName = varchar("popular_name").bindTo { it.popularName }
    val scientificName = varchar("scientific_name").bindTo { it.scientificName }
    val description = varchar("description").bindTo { it.description }
    val ownerId = int("owner_id").references(UserTable) { it.owner }
    val imageFileExtension = varchar("image_file_extension").bindTo { it.imageFileExtension }
}

