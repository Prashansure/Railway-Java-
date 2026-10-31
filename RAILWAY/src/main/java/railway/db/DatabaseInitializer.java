package railway.db;

import java.sql.Connection;
import java.sql.Statement;

/** Creates the tables required by the application when it starts. */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
        // Utility class; do not create an instance.
    }

    public static void initialize(DBConnection db) {
        createUsersTable(db);
        createTrainsTables(db);
        createTicketsTable(db);
    }

    private static void createUsersTable(DBConnection db) {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE NOT NULL,"
                + "password TEXT NOT NULL,"
                + "role TEXT DEFAULT 'user')";

        try (Connection connection = db.connect("users");
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute("INSERT OR IGNORE INTO users "
                    + "(username, password, role) "
                    + "VALUES ('admin', 'admin123', 'admin')");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize the users database.", exception);
        }
    }

    private static void createTrainsTables(DBConnection db) {
        String trainsSql = "CREATE TABLE IF NOT EXISTS trains ("
                + "train_code INTEGER PRIMARY KEY,"
                + "name TEXT, source TEXT, destination TEXT,"
                + "departure TEXT, arrival TEXT, route TEXT, duration INTEGER,"
                + "fare_1A INTEGER DEFAULT 0, fare_2A INTEGER DEFAULT 0,"
                + "fare_3A INTEGER DEFAULT 0, fare_SL INTEGER DEFAULT 0,"
                + "fare_CC INTEGER DEFAULT 0, fare_2S INTEGER DEFAULT 0,"
                + "fare_EC INTEGER DEFAULT 0)";

        String seatsSql = "CREATE TABLE IF NOT EXISTS seat_availability ("
                + "train_code INTEGER, class TEXT, available INTEGER,"
                + "PRIMARY KEY(train_code, class))";

        try (Connection connection = db.connect("trains");
             Statement statement = connection.createStatement()) {
            statement.execute(trainsSql);
            statement.execute(seatsSql);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize the trains database.", exception);
        }
    }

    private static void createTicketsTable(DBConnection db) {
        String sql = "CREATE TABLE IF NOT EXISTS tickets ("
                + "pnr TEXT PRIMARY KEY, username TEXT, passenger TEXT, age INTEGER,"
                + "train_code INTEGER, train_name TEXT, source TEXT, destination TEXT,"
                + "travel_date TEXT, class TEXT, seats INTEGER, fare INTEGER,"
                + "status TEXT DEFAULT 'CONFIRMED', booked_at TEXT)";

        try (Connection connection = db.connect("tickets");
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize the tickets database.", exception);
        }
    }
}
