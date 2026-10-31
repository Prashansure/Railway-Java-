package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Screen for checking one PNR and viewing the logged-in user's bookings. */
public final class PNRStatusScreen extends JPanel {

    private final SmartRailwaySystem app;
    private final JTextField pnr = Ui.field(15);
    private final JPanel result = Ui.card();
    private final DefaultTableModel tableModel = Ui.model(
            "PNR", "Train", "From to To", "Date", "Class", "Seats", "Fare", "Status");

    public PNRStatusScreen(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);
        add(Ui.header("PNR Status Check", () -> app.show("menu"), Ui.ORANGE),
                BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Ui.BG);
        body.setBorder(new javax.swing.border.EmptyBorder(20, 24, 16, 24));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel searchPanel = Ui.card();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        JLabel heading = Ui.label("Check PNR Status", 17, Ui.TEXT, true);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        searchPanel.add(heading);

        JPanel searchRow = Ui.panel(Ui.SURFACE);
        searchRow.add(Ui.label("PNR Number:", 12, Ui.MUTED, false));
        searchRow.add(pnr);
        searchRow.add(Ui.button("CHECK STATUS", Ui.BLUE, this::check));
        searchPanel.add(searchRow);
        body.add(searchPanel);

        body.add(result);

        JPanel bookingsHeading = Ui.panel(Ui.BG);
        bookingsHeading.setLayout(new BorderLayout());
        bookingsHeading.add(Ui.label("My Bookings", 17, Ui.TEXT, true), BorderLayout.WEST);
        bookingsHeading.add(Ui.button("Refresh", Ui.SURFACE2, this::load), BorderLayout.EAST);
        bookingsHeading.setBorder(new javax.swing.border.EmptyBorder(14, 0, 5, 0));
        body.add(bookingsHeading);

        JTable bookingTable = Ui.table(tableModel);
        Ui.setColumnWidths(bookingTable, 95, 190, 130, 95, 60, 55, 85, 95);
        bookingTable.setPreferredScrollableViewportSize(new Dimension(850, 8 * 27 + 25));

        JScrollPane ticketsScroll = Ui.scroll(bookingTable);
        ticketsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        body.add(ticketsScroll);
        add(Ui.pageScroll(body), BorderLayout.CENTER);

        pnr.addActionListener(event -> check());
    }

    /** Looks up the entered PNR and displays its details in the result card. */
    private void check() {
        String value = pnr.getText().trim();
        if (value.isEmpty()) {
            showError("Enter a PNR number.");
            return;
        }

        Object[] ticket = app.booking.getPnrStatus(value);
        result.removeAll();
        result.setBorder(BorderFactory.createLineBorder(Ui.SURFACE2, 1));
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

        if (ticket == null) {
            result.add(Ui.label("PNR not found.", 13, Ui.RED, true));
        } else {
            boolean confirmed = "CONFIRMED".equals(ticket[12]);
            result.setBorder(BorderFactory.createLineBorder(
                    confirmed ? Ui.GREEN : Ui.RED, 2));

            result.add(Ui.label("PNR: " + ticket[0], 18, Ui.TEXT, true));
            result.add(Ui.label(
                    "Status: " + ticket[12], 14, confirmed ? Ui.GREEN : Ui.RED, true));

            String[][] details = {
                    {"Passenger", ticket[2] + " (Age: " + ticket[3] + ")"},
                    {"Train", ticket[5] + " (" + ticket[4] + ")"},
                    {"Route", ticket[6] + " to " + ticket[7]},
                    {"Date", ticket[8].toString()},
                    {"Class", ticket[9].toString()},
                    {"Seats", ticket[10].toString()},
                    {"Total Fare", "INR " + ticket[11]},
                    {"Booked at", ticket[13].toString()}
            };

            for (String[] detail : details) {
                result.add(Ui.label(detail[0] + ":  " + detail[1], 12, Ui.TEXT, false));
            }
        }

        result.revalidate();
        result.repaint();
    }

    /** Reloads the current user's bookings into the table. */
    private void load() {
        tableModel.setRowCount(0);

        for (Object[] ticket : app.booking.getUserTickets(
                (String) app.currentUser.get("username"))) {
            tableModel.addRow(new Object[]{
                    ticket[0],
                    ticket[5] + " (" + ticket[4] + ")",
                    ticket[6] + " to " + ticket[7],
                    ticket[8],
                    ticket[9],
                    ticket[10],
                    "INR " + ticket[11],
                    ticket[12]
            });
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void onShow() {
        load();
    }
}
