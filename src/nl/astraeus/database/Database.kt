package nl.astraeus.database

import nl.astraeus.database.jdbc.ConnectionPool
import nl.astraeus.database.jdbc.ConnectionProvider
import java.sql.Connection

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:40
 */

fun begin() {
    if (Persister.transactionActive()) {
        throw IllegalStateException("Already connection active in begin!")
    }

    Persister.begin()
}

fun commit() {
    if (!Persister.transactionActive()) {
        throw IllegalStateException("No connection active in commit!")
    }

    Persister.commit();
}

fun rollback() {
    if (!Persister.transactionActive()) {
        throw IllegalStateException("No connection active in rollback!")
    }

    Persister.rollback();
}

fun connection() = Persister.getConnection()

fun transactionActive() = Persister.transactionActive()

fun transaction(task: () -> Unit) {
    if (transactionActive()) {
        return task()
    } else {
        try {
            begin()

            task()

            commit()
        } finally {
            if (transactionActive()) {
                rollback()
            }
        }
    }
}

fun <T> transaction(task: () -> T): T {
    if (transactionActive()) {
        return task()
    } else {
        try {
            begin()

            var result = task()

            commit()

            return result
        } finally {
            if (transactionActive()) {
                rollback()
            }
        }
    }
}

fun setConnectionProvider(conn: () -> Connection ) {
    ConnectionPool.get().setConnectionProvider(object : ConnectionProvider {
        override fun getConnection(): Connection = conn()
    })
}
