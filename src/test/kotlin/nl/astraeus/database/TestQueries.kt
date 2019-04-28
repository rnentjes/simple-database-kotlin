package nl.astraeus.database

import nl.astraeus.database.annotations.Column
import nl.astraeus.database.annotations.Id
import nl.astraeus.database.annotations.Table
import nl.astraeus.database.jdbc.ConnectionPool
import nl.astraeus.database.jdbc.ConnectionProvider
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

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
        return transaction<List<User>> {
            UserDao.where("company = ?", id)
        }
    }
}

@Table(name = "usr")
class User(
        var company: Company,
        var name: String,
        var email: String) {
    @Id var id: Long = 0

    protected constructor(): this(Company(""), "", "")
}

// needs manual index to prevent double entries
@Table
class ManyToMany(var company: Company, @Column(name = "usr") var user: User) {
    @Id var id: Long = 0

    protected constructor(): this(Company(""), User(Company(""), "", ""))
}

object CompanyDao: SimpleDao<Company>(Company::class.java)

object UserDao: SimpleDao<User>(User::class.java)

object MTMDao: SimpleDao<ManyToMany>(ManyToMany::class.java) {

    fun users(comp: Company): List<User> {
        return transaction<List<User>> {
            UserDao.from("join manytomany where manytomany.user = usr.id and manytomany.company = ?", comp.id)
            val dao = UserDao()

            dao.from("join manytomany where manytomany.user = usr.id and manytomany.company = ?", comp.id)
        }
    }

    fun companies(user: User): List<Company> {
        return transaction<List<Company>> {
            CompanyDao.from("join manytomany where manytomany.company = company.id and manytomany.user = ?", user.id)
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

class MyConnectionProvider : ConnectionProvider() {

    override fun getConnection() = createConnection()

    override fun getDefinition(): DdlMapping.DatabaseDefinition {
        return DdlMapping.DatabaseDefinition.H2
    }
}

class TestQueries {
    @Before fun setUp() {
        val db = SimpleDatabase.define(ConnectionPool(MyConnectionProvider()))

        db.setExecuteDDLUpdates(true);
    }

    @Test fun testWhere() {

        transaction {
            val company = Company("company")

            val rien = User(company, "Rien", "info@somewhere.com")
            val piet = User(company, "Piet", "piet@somewhere.com")

            UserDao.insert(info)
            UserDao.upsert(piet)

            info.name = "Iiiinfo"
            UserDao.update(info)

            piet.email = "pietje@somewhere.com"
            UserDao.upsert(piet)

            MTMDao.insert(ManyToMany(company, info))
            MTMDao.insert(ManyToMany(company, piet))
            MTMDao.insert(ManyToMany(Company("Other company"), info))
        }

        transaction {
            val user = UserDao.find("name = ?", "Iiiinfo")

            if (user != null) {
                user.company.name = "Better Company!"

                CompanyDao.update(user.company)

                val companies = MTMDao.companies(user)

                for (company in companies) {
                    println("Company from ${user.name} -> ${company.name}")
                }
            }
        }

        transaction {
            val found = UserDao.where("name = ?", "Iiiinfo")

            assertTrue(found.size == 1)

            assertTrue(UserDao.all().size == 2)

            assertTrue(UserDao.count("name = ?", "Piet") == 1)

            for(company in CompanyDao.all()) {
                println("Company: #${company.id} - ${company.name}")

                for(user in company.users()) {
                    println("Company user: #${user.id} - ${user.name} - ${user.email} - ${user.company.name}")
                }
            }

            for(user in UserDao.all()) {
                println("Found: #${user.id} - ${user.name} - ${user.email} - ${user.company.name}")
            }

            var rs = query(query = "SELECT * FROM company")

            while(rs.next()) {
                print("Company: ")
                print(rs.getLong(1))
                print(" - ")
                println(rs.getString(2))
            }

            rs = query(query = "SELECT * FROM usr")

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

            rs = query(query = "SELECT * FROM manytomany")

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
