package nl.astraeus.database

import java.sql.ResultSet

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:46
 */

fun execute(dbName: String = "default", query: String, vararg args: Any) {
    val db = SimpleDatabase.get(dbName)

    db.execute(query, *args)
}

fun query(dbName: String = "default", query: String, vararg args: Any): ResultSet {
    val db = SimpleDatabase.get(dbName)

    return db.executeQuery(query, *args)
}

fun update(dbName: String = "default", query: String, vararg args: Any): Int {
    val db = SimpleDatabase.get(dbName)

    return db.executeUpdate(query, *args)
}
