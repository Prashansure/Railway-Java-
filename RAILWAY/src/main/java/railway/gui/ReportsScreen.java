package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/** Displays booking totals and a train-by-train report for the administrator. */
public final class ReportsScreen extends JPanel {

    private final SmartRailwaySystem app;
    private final JLabel[] summaryValues = new JLabel[4];
    private final DefaultTableModel tableModel = Ui.model(
            "Train Code", "Train Name", "Bookings", "Passengers", "Revenue (INR)");

    public ReportsScreen(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JPanel navigation = Ui.header(
                "Reports and Analytics", () -> app.show("menu"), Ui.ORANGE);
        navigation.add(Ui.button("Refresh", Ui.SURFACE2, this::load), BorderLayout.EAST);
        add(navigation, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Ui.BG);
        body.setBorder(new javax.swing.border.EmptyBorder(16, 20, 16, 20));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel summaryCards = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryCards.setBackground(Ui.BG);
        String[] names = {"Confirmed", "Cancelled", "Passengers", "Revenue"};
        Color[] colours = {Ui.GREEN, Ui.RED, Ui.BLUE, Ui.ACCENT};

        for (int index = 0; index < names.length; index++) {
            JPanel card = Ui.card();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.add(Ui.label(names[index], 12, Ui.MUTED, false));
            summaryValues[index] = Ui.label("-", 24, colours[index], true);
            card.add(summaryValues[index]);
            summaryCards.add(card);
        }

        body.add(summaryCards);
        body.add(Box.createVerticalStrut(18));
        body.add(Ui.label("Train-wise Report", 17, Ui.TEXT, true));

        JTable reportTable = Ui.table(tableModel);
        Ui.setColumnWidths(reportTable, 90, 270, 100, 120, 140);
        reportTable.setPreferredScrollableViewportSize(new Dimension(800, 12 * 27 + 25));

        JScrollPane reportScroll = Ui.scroll(reportTable);
        reportScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        body.add(reportScroll);
        add(Ui.pageScroll(body), BorderLayout.CENTER);
    }

    /** Reads fresh report data from the database and updates the labels/table. */
    private void load() {
        Map<String, Long> summary = app.reports.getSummary();
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);

        summaryValues[0].setText(String.valueOf(summary.get("confirmed")));
        summaryValues[1].setText(String.valueOf(summary.get("cancelled")));
        summaryValues[2].setText(String.valueOf(summary.get("passengers")));
        summaryValues[3].setText("INR " + numberFormat.format(summary.get("revenue")));

        tableModel.setRowCount(0);
        for (Object[] row : app.reports.getTrainReport()) {
            tableModel.addRow(new Object[]{
                    row[1],
                    row[0],
                    row[2],
                    row[3],
                    "INR " + numberFormat.format(row[4])
            });
        }
    }

    public void onShow() {
        load();
    }
}
