package railway.services;

import railway.db.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Contains the business logic for searching, booking, and cancelling tickets. */
public final class BookingService {

    public static final String[] CLASSES = {"1A", "2A", "3A", "SL", "CC", "2S", "EC"};
    public static final String[] CLASS_NAMES = {
            "First AC", "Second AC", "Third AC", "Sleeper",
            "Chair Car", "Second Sitting", "Executive Chair"
    };

    private final DBConnection db;
    private final Random random = new Random();

    public BookingService(DBConnection db) {
        this.db = db;
    }

    /** Small result object used by booking and cancellation operations. */
    public record Result(boolean ok, String value) {
    }

    /** Finds trains that match the source and destination stations. */
    public List<Object[]> searchTrains(String source, String destination) {
        List<Object[]> trains = new ArrayList<>();
        String sql = "SELECT train_code, name, source, destination, departure, arrival, "
                + "duration, fare_1A, fare_2A, fare_3A, fare_SL, fare_CC, fare_2S, fare_EC "
                + "FROM trains WHERE UPPER(source) = UPPER(?) "
                + "AND UPPER(destination) = UPPER(?)";

        try (Connection connection = db.connect("trains");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, source);
            statement.setString(2, destination);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    trains.add(readRow(result, 14));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not search for trains.", exception);
        }

        return trains;
    }

    /** Returns the classes that still have seats available for a train. */
    public List<Object[]> getAvailableClasses(int trainCode) {
        List<Object[]> classes = new ArrayList<>();
        String sql = "SELECT class, available FROM seat_availability "
                + "WHERE train_code = ? AND available > 0";

        try (Connection connection = db.connect("trains");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trainCode);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    classes.add(new Object[]{result.getString(1), result.getInt(2)});
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load available classes.", exception);
        }

        return classes;
    }

    /** Looks up the fare column for one train class. */
    public int getFare(int trainCode, String travelClass) {
        if (!Arrays.asList(CLASSES).contains(travelClass)) {
            return 0;
        }

        // The class is checked against CLASSES before it is used as a column name.
        String fareColumn = "fare_" + travelClass;
        String sql = "SELECT " + fareColumn + " FROM trains WHERE train_code = ?";

        try (Connection connection = db.connect("trains");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trainCode);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the fare.", exception);
        }
    }

    /** Books seats, decreases availability, and stores the ticket in the tickets database. */
    public Result bookTicket(String username, String passenger, int age, int trainCode,
                             String travelDate, String travelClass, int seats) {
        try (Connection trainConnection = db.connect("trains")) {
            int availableSeats = findAvailableSeats(trainConnection, trainCode, travelClass);
            if (availableSeats < seats) {
                return new Result(false, "Not enough seats are available.");
            }

            Object[] train = findTrain(trainConnection, trainCode);
            if (train == null) {
                return new Result(false, "Train not found.");
            }

            decreaseSeats(trainConnection, trainCode, travelClass, seats);

            String pnr = generatePnr();
            String bookedAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int fare = getFare(trainCode, travelClass) * seats;

            String ticketSql = "INSERT INTO tickets (pnr, username, passenger, age, "
                    + "train_code, train_name, source, destination, travel_date, class, "
                    + "seats, fare, status, booked_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection ticketConnection = db.connect("tickets");
                 PreparedStatement statement = ticketConnection.prepareStatement(ticketSql)) {
                Object[] values = {
                        pnr, username, passenger, age, trainCode, train[0], train[1], train[2],
                        travelDate, travelClass, seats, fare, "CONFIRMED", bookedAt
                };
                for (int index = 0; index < values.length; index++) {
                    statement.setObject(index + 1, values[index]);
                }
                statement.executeUpdate();
            }

            return new Result(true, pnr);
        } catch (SQLException exception) {
            return new Result(false, "Could not complete the booking: " + exception.getMessage());
        }
    }

    /** Returns one ticket by PNR, or null if the PNR does not exist. */
    public Object[] getPnrStatus(String pnr) {
        String sql = "SELECT * FROM tickets WHERE pnr = ?";

        try (Connection connection = db.connect("tickets");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pnr);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readRow(result, 14) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the ticket.", exception);
        }
    }

    /** Cancels a user's ticket and returns its seats to the train. */
    public Result cancelTicket(String pnr, String username) {
        try (Connection ticketConnection = db.connect("tickets")) {
            String sql = "SELECT status, train_code, class, seats, username "
                    + "FROM tickets WHERE pnr = ?";

            String status;
            String owner;
            String travelClass;
            int trainCode;
            int seats;

            try (PreparedStatement statement = ticketConnection.prepareStatement(sql)) {
                statement.setString(1, pnr);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return new Result(false, "PNR not found.");
                    }
                    status = result.getString("status");
                    trainCode = result.getInt("train_code");
                    travelClass = result.getString("class");
                    seats = result.getInt("seats");
                    owner = result.getString("username");
                }
            }

            if (!Objects.equals(owner, username)) {
                return new Result(false, "You can only cancel your own tickets.");
            }
            if ("CANCELLED".equals(status)) {
                return new Result(false, "Ticket is already cancelled.");
            }

            try (PreparedStatement statement = ticketConnection.prepareStatement(
                    "UPDATE tickets SET status = 'CANCELLED' WHERE pnr = ?")) {
                statement.setString(1, pnr);
                statement.executeUpdate();
            }

            try (Connection trainConnection = db.connect("trains");
                 PreparedStatement statement = trainConnection.prepareStatement(
                         "UPDATE seat_availability SET available = available + ? "
                                 + "WHERE train_code = ? AND class = ?")) {
                statement.setInt(1, seats);
                statement.setInt(2, trainCode);
                statement.setString(3, travelClass);
                statement.executeUpdate();
            }

            return new Result(true, "Ticket cancelled successfully. The refund will be processed.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not cancel the ticket.", exception);
        }
    }

    public List<Object[]> getUserTickets(String username) {
        return getTickets("SELECT * FROM tickets WHERE username = ? ORDER BY booked_at DESC", username);
    }

    public List<Object[]> getAllTickets() {
        return getTickets("SELECT * FROM tickets ORDER BY booked_at DESC", null);
    }

    private int findAvailableSeats(Connection connection, int trainCode, String travelClass)
            throws SQLException {
        String sql = "SELECT available FROM seat_availability WHERE train_code = ? AND class = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trainCode);
            statement.setString(2, travelClass);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /** Returns train name, source, and destination for a train code. */
    private Object[] findTrain(Connection connection, int trainCode) throws SQLException {
        String sql = "SELECT name, source, destination FROM trains WHERE train_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trainCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new Object[]{
                        result.getString("name"),
                        result.getString("source"),
                        result.getString("destination")
                };
            }
        }
    }

    private void decreaseSeats(Connection connection, int trainCode,
                               String travelClass, int seats) throws SQLException {
        String sql = "UPDATE seat_availability SET available = available - ? "
                + "WHERE train_code = ? AND class = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seats);
            statement.setInt(2, trainCode);
            statement.setString(3, travelClass);
            statement.executeUpdate();
        }
    }

    private String generatePnr() {
        StringBuilder pnr = new StringBuilder(10);
        for (int index = 0; index < 10; index++) {
            pnr.append(random.nextInt(10));
        }
        return pnr.toString();
    }

    private List<Object[]> getTickets(String sql, String username) {
        List<Object[]> tickets = new ArrayList<>();

        try (Connection connection = db.connect("tickets");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (username != null) {
                statement.setString(1, username);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    tickets.add(readRow(result, 14));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load tickets.", exception);
        }

        return tickets;
    }

    private static Object[] readRow(ResultSet result, int columnCount) throws SQLException {
        Object[] row = new Object[columnCount];
        for (int index = 0; index < columnCount; index++) {
            row[index] = result.getObject(index + 1);
        }
        return row;
    }
}
