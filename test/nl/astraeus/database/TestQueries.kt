package nl.astraeus.database

import nl.astraeus.database.annotations.Id
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.sql.DriverManager
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

        setConnectionProvider {
            Class.forName("org.h2.Driver")

            val connection = DriverManager.getConnection("jdbc:h2:mem:TestQueries", "sa", "")
            connection.autoCommit = false

            connection
        }
    }

    @After fun tearDown() {
        // tear down the test case
    }

    @Test fun testWhere() {
        var dao = UserDao()

        transaction {
            var rien = User("Rien", "info@somewhere.com")
            var piet = User("Piet", "piet@somewhere.com")

            dao.insert(rien)
            dao.upsert(piet)

            rien.name = "Rrrrien"
            dao.update(rien)

            piet.email = "pietje@somewhere.com"
            dao.upsert(piet)

            var found = dao.where("name = ?", "Rrrrien")

            assertTrue {
                found.size() == 1
            }

            assertTrue {
                dao.all().size() == 2
            }

            assertTrue {
                dao.count("name = ?", "Piet") == 1
            }

            for(user in dao.all()) {
                println("Found: #${user.id} - ${user.name} - ${user.email}")
            }
        }
    }

}

