package nl.astraeus.database

import java.sql.ResultSet

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:46
 */

abstract class Dao<T>(val cls: Class<T>) {

    open fun find(id: Long): T {
        return Persister.find(cls, id)
    }

    open fun insert(obj: T) {
        Persister.insert(obj)
    }

    open fun update(obj: T) {
        Persister.update(obj);
    }
    open fun delete(obj: T) {
        Persister.delete(obj)
    }

    open fun execute(query: String, args: Array<Any>) {
        Persister.execute(query, args);
    }

    open fun query(query: String, args: Array<Any>): ResultSet {
        return Persister.executeQuery(query, args);
    }

    open fun update(query: String, args: Array<Any>): Int {
        return Persister.executeUpdate(query, args);
    }

    open fun where(query: String, vararg args: String): List<T> {
        return Persister.selectWhere(cls, query, *args)
    }

}