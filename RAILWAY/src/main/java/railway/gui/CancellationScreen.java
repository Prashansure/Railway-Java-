package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Allows a user to preview and cancel one of their confirmed tickets. */
public final class CancellationScreen extends JPanel {

    private final SmartRailwaySystem app;
    private final JTextField pnr = Ui.field(15);
    private final JPanel preview = Ui.card();
    private final DefaultTableModel tableModel = Ui.model(
            "PNR", "Train", "From to To", "Date", "Class", "Seats", "Fare", "Action");
    private final JTable activeTable = Ui.table(tableModel);

    public CancellationScreen(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);
        add(Ui.header("Cancel Ticket", () -> app.show("menu"), Ui.ORANGE),
                BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Ui.BG);
        body.setBorder(new javax.swing.border.EmptyBorder(20, 24, 16, 24));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel searchCard = Ui.card();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        JLabel heading = Ui.label("Cancel by PNR", 17, Ui.TEXT, true);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        searchCard.add(heading);

        JPanel searchRow = Ui.panel(Ui.SURFACE);
        searchRow.add(Ui.label("PNR Number:", 12, Ui.MUTED, false));
        searchRow.add(pnr);
        searchRow.add(Ui.button("CHECK AND CANCEL", Ui.RED, this::check));
        searchCard.add(searchRow);
        searchCard.add(Ui.label(
                "Cancellation charges may apply according to railway policy.",
                11, Ui.MUTED, false));
        body.add(searchCard);

        body.add(preview);

        JPanel tableHeading = Ui.panel(Ui.BG);
        tableHeading.setLayout(new BorderLayout());
        tableHeading.add(Ui.label("Your Active Tickets", 17, Ui.TEXT, true),
                BorderLayout.WEST);
        tableHeading.add(Ui.button("Refresh", Ui.SURFACE2, this::load), BorderLayout.EAST);
        tableHeading.setBorder(new javax.swing.border.EmptyBorder(13, 0, 5, 0));
        body.add(tableHeading);

        Ui.setColumnWidths(activeTable, 95, 150, 125, 90, 55, 50, 75, 70);
        activeTable.setPreferredScrollableViewportSize(new Dimension(780, 8 * 27 + 25));

        JScrollPane activeScroll = Ui.scroll(activeTable);
        activeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        body.add(activeScroll);
        add(Ui.pageScroll(body), BorderLayout.CENTER);

        pnr.addActionListener(event -> check());
        activeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                int row = activeTable.rowAtPoint(event.getPoint());
                int column = activeTable.columnAtPoint(event.getPoint());
                if (row >= 0 && column == 7) {
                    pnr.setText(tableModel.getValueAt(row, 0).toString());
                    check();
                }
            }
        });
    }

    /** Loads the selected ticket and shows a cancellation preview. */
    private void check() {
        String value = pnr.getText().trim();
        if (value.isEmpty()) {
            showError("Enter a PNR number.");
            return;
        }

        Object[] ticket = app.booking.getPnrStatus(value);
        preview.removeAll();
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));

        if (ticket == null) {
            preview.add(Ui.label("PNR not found.", 13, Ui.RED, true));
        } else if ("CANCELLED".equals(ticket[12])) {
            preview.add(Ui.label("This ticket is already cancelled.", 13, Ui.RED, true));
        } else {
            preview.add(Ui.label("Ticket Details", 17, Ui.TEXT, true));

            String[][] details = {
                    {"PNR", ticket[0].toString()},
                    {"Passenger", ticket[2] + " (Age " + ticket[3] + ")"},
                    {"Train", ticket[5] + " (" + ticket[4] + ")"},
                    {"Route", ticket[6] + " to " + ticket[7]},
                    {"Date", ticket[8].toString()},
                    {"Class", ticket[9].toString()},
                    {"Seats", ticket[10].toString()},
                    {"Fare", "INR " + ticket[11]}
            };

            for (String[] detail : details) {
                preview.add(Ui.label(
                        detail[0] + ":  " + detail[1], 12, Ui.TEXT, false));
            }

            long refund = ((Number) ticket[11]).longValue() * 75 / 100;
            preview.add(Ui.label(
                    "Estimated Refund: INR " + refund + " (75% of INR " + ticket[11] + ")",
                    13, Ui.GREEN, true));
            preview.add(Ui.button(
                    "CONFIRM CANCELLATION", Ui.RED,
                    () -> cancel(ticket[0].toString())));
        }

        preview.revalidate();
        preview.repaint();
    }

    private void cancel(String value) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel PNR " + value + "?\nThis cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        var result = app.booking.cancelTicket(
                value, (String) app.currentUser.get("username"));
        if (result.ok()) {
            JOptionPane.showMessageDialog(
                    this, result.value(), "Ticket Cancelled", JOptionPane.INFORMATION_MESSAGE);
            preview.removeAll();
            pnr.setText("");
            load();
        } else {
            showError(result.value());
        }
    }

    private void load() {
        tableModel.setRowCount(0);

        for (Object[] ticket : app.booking.getUserTickets(
                (String) app.currentUser.get("username"))) {
            if ("CONFIRMED".equals(ticket[12])) {
                tableModel.addRow(new Object[]{
                        ticket[0],
                        ticket[5],
                        ticket[6] + " to " + ticket[7],
                        ticket[8],
                        ticket[9],
                        ticket[10],
                        "INR " + ticket[11],
                        "Cancel"
                });
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void onShow() {
        load();
    }
}
