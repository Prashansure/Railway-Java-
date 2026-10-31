package railway.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Provides SQLite connections for the three application databases. */
public final class DBConnection {

    private final Path baseDirectory;

    public DBConnection() {
        this(Path.of(System.getProperty("user.dir")));
    }

    public DBConnection(Path baseDirectory) {
        this.baseDirectory = baseDirectory.toAbsolutePath();

        try {
            Files.createDirectories(this.baseDirectory.resolve("data"));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create the data directory.", exception);
        }
    }

    /** Returns the path of a database file such as data/users.db. */
    public Path file(String databaseName) {
        return baseDirectory.resolve("data").resolve(databaseName + ".db");
    }

    /** Opens a connection to the requested SQLite database. */
    public Connection connect(String databaseName) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file(databaseName));
    }

    public Path baseDir() {
        return baseDirectory;
    }
}
