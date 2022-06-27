package ufc.erv.db.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.nio.file.Paths

interface User : Entity<User> {
    companion object : Entity.Factory<User>()
    val id: Int
    var name: String
    var email: String
    var city: City
    var digestToken: String
}

private val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
private val resourcesPath = Paths.get(projectDirAbsolutePath, "/src/main/resources").toString()
fun getUserPath(username: String) = Paths.get(resourcesPath, "/u/${username}/plant/image").toString()

object UserTable : Table<User>("users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val email = varchar("email").bindTo { it.email }
    val cityId = int("city_id").references(CityTable) { it.city }
    val digestToken = varchar("digest_token").bindTo { it.digestToken }
}

