package nl.astraeus.database

import java.sql.ResultSet

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:46
 */

abstract class Dao<T>(val cls: Class<T>) {

    init {
        Persister.init(cls)
    }

    open fun find(id: Long): T = Persister.find(cls, id)

    open fun insert(obj: T) = Persister.insert(obj)
    open fun update(obj: T) = Persister.update(obj)
    open fun delete(obj: T) = Persister.delete(obj)
    open fun upsert(obj: T) = Persister.store(obj)

    open fun execute(query: String, vararg args: String) = Persister.execute(query, *args);

    open fun query(query: String, vararg args: String): ResultSet = Persister.executeQuery(query, *args)

    open fun update(query: String, vararg args: String): Int = Persister.executeUpdate(query, *args)

    open fun where(query: String, vararg args: String): List<T> = Persister.selectWhere(cls, query, *args)

    open fun from(query: String, vararg args: String): List<T> = Persister.selectFrom(cls, query, *args)

    open fun count(query: String, vararg args: String): Int = Persister.selectCount(cls, query, *args)

    open fun all(): List<T> = Persister.selectAll(cls)

}
