package ufc.erv.db

import com.typesafe.config.ConfigFactory
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import ufc.erv.db.model.*
import ufc.erv.response.UserPlantResponse

val Database.users get() = this.sequenceOf(UserTable)
val Database.plants get() = this.sequenceOf(PlantTable)
val Database.cities get() = this.sequenceOf(CityTable)

fun Query.any(predicate: (QueryRowSet) -> Boolean): Boolean {
    for (row in this) {
        if (predicate(row)) return true
    }
    return false
}
fun Query.first(): QueryRowSet? {
    for (row in this) return row
    return null
}

object DB {
    private lateinit var db: Database
    fun init() {
        val config = ConfigFactory.load()
        // val driver = config.getString("mysql.driver")
        val jdbcUrl = config.getString("mysql.jdbcUrl")
        db = Database.connect(
            jdbcUrl,
            user = config.getString("mysql.auth.user"),
            password = config.getString("mysql.auth.password")
        )
    }


    fun getUserById(id: Int): User? {
        return db.users.find { it.id eq id }
    }
    fun getUserByName(username: String): User? {
        return db.users.find { it.name eq username }
    }
    fun getAllPlantsFromUser(username: String): List<UserPlantResponse> {
        return db.from(PlantTable)
            .innerJoin(UserTable, on = UserTable.id eq PlantTable.ownerId)
            .innerJoin(CityTable, on = CityTable.id eq UserTable.cityId)
            .innerJoin(StateTable, on = StateTable.id eq CityTable.stateId)
            .select()
            .where(UserTable.name eq username)
            .map {
                UserPlantResponse(
                    it[PlantTable.id]!!,
                    it[PlantTable.popularName]!!,
                    it[PlantTable.scientificName],
                    it[PlantTable.description]
                )
            }
    }
    fun getImagePath(plantId: Int): String? {
        val plant = db.plants.find { it.id eq plantId }
        return plant?.imageFileExtension?.let { "${getPlantImagePath(plant.owner.name, plant.id.toString())}.$it" }
    }
    private fun getAllLocalizations(): Query {
        return db.from(CityTable).select(StateTable.name, CityTable.name)
    }
    private fun alreadyHasLocalization(state: String, city: String): Boolean {
        for (row in getAllLocalizations()) {
            if (state == row[StateTable.name] && city == row[CityTable.name]) {
                return true
            }
        }
        return false
    }
    private fun findState(state: String): Int? {
        val s = db.from(StateTable).select().where {
            StateTable.name eq state
        }.first()
        return if (s != null) s[StateTable.id] else null
    }
    private fun findCity(state: String, city: String): Int? {
        val c = db.from(CityTable).select().where {
            (CityTable.name eq city) and (StateTable.name eq state)
        }.first()
        return if (c != null) c[CityTable.id] else null
    }
    private fun registerLocalizationIfNotAlreadyThere(state: String, city: String): Int {
        val stateId = findState(state) ?: registerState(state)
        return findCity(state, city) ?: registerCity(stateId, city)
    }
    private fun registerState(state: String): Int {
        return db.insertAndGenerateKey(StateTable) {
            set(it.name, state)
        } as Int
    }
    private fun registerCity(stateId: Int, city: String): Int {
        return db.insertAndGenerateKey<CityTable>(CityTable) {
            set(it.name, city)
            set(it.stateId, stateId)
        } as Int
    }

    fun registerPlant(
        ownerId: Int,
        popularName: String,
        scientificName: String?,
        description: String?,
        imageFileExtension: String
    ): Int {
        return db.insertAndGenerateKey(PlantTable) {
            set(it.popularName, popularName)
            set(it.scientificName, scientificName)
            set(it.description, description)
            set(it.ownerId, ownerId)
            set(it.imageFileExtension, imageFileExtension)
        } as Int
    }


}