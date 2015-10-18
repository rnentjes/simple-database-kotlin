package nl.astraeus.database

import nl.astraeus.database.annotations.Id
import nl.astraeus.database.jdbc.ConnectionPool
import nl.astraeus.database.jdbc.ConnectionProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.test.assertTrue

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 17:00
 */

class User(@Id var id: Long = 0, var name: String, var email: String) {
    constructor(): this(0, "", "")

    constructor(name: String, email: String): this(0, name, email)
}

class UserDao(): Dao<User>(User::class.java)

class TestQueries {
    @Before fun setUp() {
        DdlMapping.get().setExecuteDDLUpdates(true)

        ConnectionPool.get().setConnectionProvider(object : ConnectionProvider {
            override fun getConnection(): Connection {
                try {
                    Class.forName("org.h2.Driver")

                    val connection = DriverManager.getConnection("jdbc:h2:mem:TestQueries", "sa", "")
                    connection.autoCommit = false

                    return connection
                } catch (e: ClassNotFoundException) {
                    throw IllegalStateException(e)
                } catch (e: SQLException) {
                    throw IllegalStateException(e)
                }
            }
        })
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testWhere() {
        var dao = UserDao()

        transaction {
            dao.insert(User("Rien", "info@nentjes.com"))
            dao.insert(User("Piet", "piet@nentjes.com"))

            var found = dao.where("name = ?", "Rien")

            assertTrue {
                found.size() == 1
            }

            for(user in found) {
                println("Found: ${user.name}")
            }
        }
    }

}

