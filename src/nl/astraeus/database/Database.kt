package nl.astraeus.database

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
