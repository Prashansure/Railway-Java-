package railway.services;

import railway.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Supplies the summary numbers and train-wise data shown on the reports page. */
public final class ReportService {

    private final DBConnection db;

    public ReportService(DBConnection db) {
        this.db = db;
    }

    /** Returns counts for confirmed tickets, cancellations, passengers, and revenue. */
    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new HashMap<>();

        String[] names = {"confirmed", "cancelled", "revenue", "passengers"};
        String[] queries = {
                "SELECT COUNT(*) FROM tickets WHERE status = 'CONFIRMED'",
                "SELECT COUNT(*) FROM tickets WHERE status = 'CANCELLED'",
                "SELECT COALESCE(SUM(fare), 0) FROM tickets WHERE status = 'CONFIRMED'",
                "SELECT COALESCE(SUM(seats), 0) FROM tickets WHERE status = 'CONFIRMED'"
        };

        try (Connection connection = db.connect("tickets");
             Statement statement = connection.createStatement()) {
            for (int index = 0; index < names.length; index++) {
                try (ResultSet result = statement.executeQuery(queries[index])) {
                    result.next();
                    summary.put(names[index], result.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the report summary.", exception);
        }

        return summary;
    }

    /** Returns booking, passenger, and revenue totals grouped by train. */
    public List<Object[]> getTrainReport() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT train_name, train_code, COUNT(*) AS bookings, "
                + "SUM(seats) AS passengers, SUM(fare) AS revenue "
                + "FROM tickets WHERE status = 'CONFIRMED' "
                + "GROUP BY train_code ORDER BY bookings DESC";

        try (Connection connection = db.connect("tickets");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                rows.add(new Object[]{
                        result.getString("train_name"),
                        result.getInt("train_code"),
                        result.getInt("bookings"),
                        result.getInt("passengers"),
                        result.getLong("revenue")
                });
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the train report.", exception);
        }

        return rows;
    }
}
