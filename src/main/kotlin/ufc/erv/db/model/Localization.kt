package ufc.erv.db.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface State : Entity<State> {
    companion object : Entity.Factory<State>()
    val id: Int
    var name: String
}
object StateTable : Table<State>("states") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}

interface City : Entity<City> {
    companion object : Entity.Factory<City>()
    val id: Int
    var name: String
    var state: State
}
object CityTable : Table<City>("cities") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val stateId = int("state_id").references(StateTable) { it.state }
}
