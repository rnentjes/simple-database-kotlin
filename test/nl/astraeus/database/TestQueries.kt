package nl.astraeus.database

import nl.astraeus.database.annotations.Column
import nl.astraeus.database.annotations.Id
import nl.astraeus.database.annotations.Table
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertTrue

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 17:00
 */

@Table
class Company(var name: String) {
    @Id var id: Long = 0

    protected constructor(): this("")

    fun users(): List<User> {
        var result: List<User>

        return transaction<List<User>> {
            var dao = UserDao()

            dao.where("company = ?", id)
        }
    }
}

@Table(name = "usr")
class User(var company: Company, var name: String, var email: String) {
    @Id var id: Long = 0

    protected constructor(): this(Company(""), "", "")
}

// needs manual index to prevent double entries
@Table
class ManyToMany(var company: Company, @Column(name = "usr") var user: User) {
    @Id var id: Long = 0

    protected constructor(): this(Company(""), User(Company(""), "", ""))
}

class CompanyDao(): Dao<Company>(Company::class.java)

class UserDao(): Dao<User>(User::class.java)

class MTMDao(): Dao<ManyToMany>(ManyToMany::class.java) {

    fun users(comp: Company): List<User> {
        return transaction<List<User>> {
            var dao = UserDao()

            dao.from("join manytomany where manytomany.user = usr.id and manytomany.company = ?", comp.id)
        }
    }

    fun companies(user: User): List<Company> {
        return transaction<List<Company>> {
            var dao = CompanyDao()

            dao.from("join manytomany where manytomany.company = company.id and manytomany.user = ?", user.id)
        }
    }
}

fun createConnection(): Connection {
    Class.forName("org.h2.Driver")

    val connection = DriverManager.getConnection("jdbc:h2:mem:TestQueries", "sa", "")
    connection.autoCommit = false

    // result
    return connection
}

class TestQueries {
    @Before fun setUp() {
        DdlMapping.get().setExecuteDDLUpdates(true)

        setConnectionProvider(::createConnection)

        setConnectionProvider {
            Class.forName("org.h2.Driver")

            val connection = DriverManager.getConnection("jdbc:h2:mem:TestQueries", "sa", "")
            connection.autoCommit = false

            // result
            connection
        }
    }

    @Test fun testWhere() {
        var companyDao = CompanyDao()
        var userDao = UserDao()
        var mtmDao = MTMDao()

        transaction {
            var company = Company("company")

            var rien = User(company, "Rien", "info@somewhere.com")
            var piet = User(company, "Piet", "piet@somewhere.com")

            userDao.insert(rien)
            userDao.upsert(piet)

            rien.name = "Rrrrien"
            userDao.update(rien)

            piet.email = "pietje@somewhere.com"
            userDao.upsert(piet)

            mtmDao.insert(ManyToMany(company, rien))
            mtmDao.insert(ManyToMany(company, piet))
            mtmDao.insert(ManyToMany(Company("Other company"), rien))
        }

        transaction {
            var user = userDao.find("name = ?", "Rrrrien")

            user.company.name = "Better Company!"

            companyDao.update(user.company)

            var companies = mtmDao.companies(user)

            for (company in companies) {
                println("Company from ${user.name} -> ${company.name}")
            }
        }

        transaction {
            var found = userDao.where("name = ?", "Rrrrien")

            assertTrue {
                found.size() == 1
            }

            assertTrue {
                userDao.all().size() == 2
            }

            assertTrue {
                userDao.count("name = ?", "Piet") == 1
            }

            for(company in companyDao.all()) {
                println("Company: #${company.id} - ${company.name}")

                for(user in company.users()) {
                    println("Company user: #${user.id} - ${user.name} - ${user.email} - ${user.company.name}")
                }
            }

            for(user in userDao.all()) {
                println("Found: #${user.id} - ${user.name} - ${user.email} - ${user.company.name}")
            }

            var rs = query("SELECT * FROM company")

            while(rs.next()) {
                print("Company: ")
                print(rs.getLong(1))
                print(" - ")
                println(rs.getString(2))
            }

            rs = query("SELECT * FROM usr")

            while(rs.next()) {
                print("User: ")
                print(rs.getLong(1))
                print(" - ")
                print(rs.getLong(2))
                print(" - ")
                print(rs.getString(3))
                print(" - ")
                println(rs.getString(4))
            }

            rs = query("SELECT * FROM manytomany")

            while(rs.next()) {
                print("MTM: ")
                print(rs.getLong(1))
                print(" - ")
                print(rs.getLong(2))
                print(" - ")
                println(rs.getLong(3))
            }
        }
    }

}
