package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Administrator view for inspecting tickets, users, and train data. */
public final class AdminPanel extends JPanel {

    private final SmartRailwaySystem app;
    private final DefaultTableModel ticketModel = Ui.model(
            "PNR", "User", "Passenger", "Train", "From", "To",
            "Date", "Class", "Seats", "Fare", "Status");
    private final DefaultTableModel userModel = Ui.model("ID", "Username", "Role");
    private final DefaultTableModel trainModel = Ui.model(
            "Code", "Name", "From", "To", "Dep", "Arr", "Hours",
            "1A", "2A", "3A", "SL", "CC", "2S", "EC");

    public AdminPanel(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JPanel navigation = Ui.header(
                "Admin Panel", () -> app.show("menu"), Color.decode("#7c3aed"));
        navigation.add(Ui.button("Refresh", Color.decode("#5b21b6"), this::load),
                BorderLayout.EAST);
        add(navigation, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("All Tickets", createTicketTable());
        tabs.addTab("Users", createUserTable());
        tabs.addTab("Trains Database", createTrainTable());
        add(tabs, BorderLayout.CENTER);
    }

    private JScrollPane createTicketTable() {
        JTable table = Ui.table(ticketModel);
        Ui.setColumnWidths(table, 100, 75, 105, 155, 105, 105, 80, 45, 50, 85, 85);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return Ui.scroll(table);
    }

    private JScrollPane createUserTable() {
        JTable table = Ui.table(userModel);
        Ui.setColumnWidths(table, 110, 260, 150);
        return Ui.scroll(table);
    }

    private JScrollPane createTrainTable() {
        JTable table = Ui.table(trainModel);
        Ui.setColumnWidths(table, 65, 165, 110, 110, 55, 55, 45,
                45, 45, 45, 45, 45, 45, 45);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return Ui.scroll(table);
    }

    /** Reloads all three administrator tables from the database. */
    private void load() {
        loadTickets();
        loadUsers();
        loadTrains();
    }

    private void loadTickets() {
        ticketModel.setRowCount(0);
        for (Object[] ticket : app.booking.getAllTickets()) {
            ticketModel.addRow(new Object[]{
                    ticket[0], ticket[1], ticket[2], ticket[5], ticket[6],
                    ticket[7], ticket[8], ticket[9], ticket[10],
                    "INR " + ticket[11], ticket[12]
            });
        }
    }

    private void loadUsers() {
        userModel.setRowCount(0);
        for (Object[] user : app.auth.getAllUsers()) {
            userModel.addRow(user);
        }
    }

    private void loadTrains() {
        trainModel.setRowCount(0);
        String sql = "SELECT * FROM trains ORDER BY train_code";

        try (Connection connection = app.db.connect("trains");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                Object[] row = new Object[14];
                row[0] = result.getInt(1);
                row[1] = result.getString(2);
                row[2] = result.getString(3);
                row[3] = result.getString(4);
                row[4] = result.getString(5);
                row[5] = result.getString(6);
                row[6] = result.getInt(8);

                for (int index = 0; index < 7; index++) {
                    int fare = result.getInt(9 + index);
                    row[7 + index] = fare > 0 ? String.valueOf(fare) : "-";
                }
                trainModel.addRow(row);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load train data.", exception);
        }
    }

    public void onShow() {
        load();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        load();
    }
}
