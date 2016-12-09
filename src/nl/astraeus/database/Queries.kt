package nl.astraeus.database

import java.sql.ResultSet

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:46
 */

fun execute(query: String, vararg args: Any) = Persister.execute(query, *args);

fun query(query: String, vararg args: Any): ResultSet = Persister.executeQuery(query, *args)

fun executeUpdate(query: String, vararg args: Any): Int = Persister.executeUpdate(query, *args)

abstract class Dao<T>(val cls: Class<T>) {

    init {
        Persister.init(cls)
    }

    open fun find(id: Long): T? = Persister.find(cls, id)
    open fun find(query: String, vararg args: Any): T? = Persister.findWhere(cls, query, *args)

    open fun insert(obj: T) = Persister.insert(obj)
    open fun update(obj: T) = Persister.update(obj)
    open fun delete(obj: T) = Persister.delete(obj)
    open fun upsert(obj: T) = Persister.store(obj)

    open fun where(query: String, vararg args: Any): List<T> = Persister.selectWhere(cls, query, *args)

    open fun from(query: String, vararg args: Any): List<T> = Persister.selectFrom(cls, query, *args)

    open fun count(query: String, vararg args: Any): Int = Persister.selectCount(cls, query, *args)

    open fun all(): List<T> = Persister.selectAll(cls)

    open fun clearCache() = Persister.invalidateCache(cls)

}
