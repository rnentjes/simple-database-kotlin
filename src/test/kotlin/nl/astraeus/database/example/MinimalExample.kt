package nl.astraeus.database.example

import nl.astraeus.database.SimpleDao
import nl.astraeus.database.SimpleDatabase
import nl.astraeus.database.annotations.*
import nl.astraeus.database.jdbc.ConnectionProvider
import nl.astraeus.database.transaction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * User: rnentjes
 * Date: 11-12-16
 * Time: 16:43
 */
@Table(name="persons")
@Cache(maxSize = 6)
class Person(
  @Id val id: Long = 0,

  @Length(value = 200)
  @Default("'new name'")
  var name: String,

  @Default("21")
  var age: Int,

  @Length(precision = 10, scale = 2)
  var balance: Double,

  var address: String
) {
    // no-arg constructor required
    constructor(): this(0, "", 0, 0.0, "")

    constructor(name: String, age: Int, address: String): this(0, name, age, 0.0, address)
}

fun main(args: Array<String>) {
    // define the default database, all it needs it a way to get a connection
    val db = SimpleDatabase.define(object : ConnectionProvider() {
        @Throws(SQLException::class, ClassNotFoundException::class)
        override fun getConnection(): Connection {
            Class.forName("org.h2.Driver")

            val connection = DriverManager.getConnection("jdbc:h2:mem:Example;DB_CLOSE_DELAY=-1", "sa", "")
            connection.autoCommit = false

            return connection
        }
    })

    // automatically create database tables and columns if needed
    db.setExecuteDDLUpdates(true)

    // use default dao (extends it if you need more)
    val personDao = SimpleDao(Person::class.java)

    // execute multiple dao actions in transaction
    personDao.execute({ dao ->
        dao.insert(Person("John", 40, "Road"))
        dao.insert(Person("Jan", 32, "Straat"))
        dao.insert(Person("Ronald", 31, "Wherever"))
        dao.insert(Person("Piet", 26, "Weg"))
        dao.insert(Person("Klaas", 10, "Pad"))
    })

    // find persons, read actions don't need a transaction
    var persons = personDao.where("name like ?", "J%")

    for (person in persons) {
        System.out.println("Person: " + person.name)
    }

    // start transaction because of the update
    db.begin()

    transaction {
        val person = personDao.find("name = ? and age = ?", "John", 40)

        person.name = "Johnny"

        personDao.update(person)
    }

    persons = personDao.where("name like ?", "J%")

    for (person in persons) {
        System.out.println("Person: " + person.name)
    }
}
