package nl.astraeus.database

/**
 * User: rnentjes
 * Date: 18-10-15
 * Time: 16:40
 */

fun transaction(name: String = "default", task: () -> Unit) {
    val db = SimpleDatabase.get(name)

    if (db.transactionActive()) {
        return task()
    } else {
        try {
            db.begin()

            task()

            db.commit()
        } finally {
            if (db.transactionActive()) {
                db.rollback()
            }
        }
    }
}

fun <T> transaction(name: String = "default", task: () -> T): T {
    val db = SimpleDatabase.get(name)

    if (db.transactionActive()) {
        return task()
    } else {
        try {
            db.begin()

            val result = task()

            db.commit()

            return result
        } finally {
            if (db.transactionActive()) {
                db.rollback()
            }
        }
    }
}
