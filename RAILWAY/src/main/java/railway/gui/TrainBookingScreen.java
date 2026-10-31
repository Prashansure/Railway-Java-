package railway.gui;

import railway.SmartRailwaySystem;
import railway.services.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Screen for searching trains and booking a seat for a passenger. */
public final class TrainBookingScreen extends JPanel {

    private final SmartRailwaySystem app;
    private final JTextField source = Ui.field(16);
    private final JTextField destination = Ui.field(16);
    private final JTextField travelDate = Ui.field(11);
    private final JTextField passengerName = Ui.field(18);
    private final JTextField passengerAge = Ui.field(5);
    private final JComboBox<String> travelClass = new JComboBox<>();
    private final JSpinner seats = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
    private final JLabel message = Ui.label("", 12, Ui.MUTED, false);
    private final JLabel fare = Ui.label("", 13, Ui.GREEN, true);
    private final JLabel bookingTitle = Ui.label("Select a train", 15, Ui.ACCENT, true);
    private final DefaultTableModel tableModel = Ui.model(
            "Code", "Train Name", "From", "To", "Departs", "Arrives", "Hours", "Classes");
    private final JTable trainTable = Ui.table(tableModel);
    private final JPanel bookingForm = Ui.card();
    private final List<Object[]> foundTrains = new ArrayList<>();
    private Object[] selectedTrain;

    public TrainBookingScreen(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);
        add(Ui.header("Search and Book Train", () -> app.show("menu"), Ui.ACCENT),
                BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Ui.BG);
        body.setBorder(new javax.swing.border.EmptyBorder(12, 16, 8, 16));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildSearchPanel());
        body.add(Box.createVerticalStrut(6));

        message.setAlignmentX(LEFT_ALIGNMENT);
        body.add(message);

        configureTrainTable();
        JScrollPane resultsScroll = Ui.scroll(trainTable);
        resultsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        resultsScroll.setAlignmentX(LEFT_ALIGNMENT);
        body.add(resultsScroll);
        body.add(Box.createVerticalStrut(6));

        buildBookingForm();
        body.add(bookingForm);

        add(Ui.pageScroll(body), BorderLayout.CENTER);
        travelClass.addActionListener(event -> updateFare());
        seats.addChangeListener(event -> updateFare());
    }

    private JPanel buildSearchPanel() {
        JPanel searchPanel = Ui.card();
        searchPanel.setLayout(new GridBagLayout());

        GridBagConstraints grid = new GridBagConstraints();
        grid.insets = new Insets(4, 7, 4, 7);
        grid.gridy = 0;
        grid.gridx = 0;
        searchPanel.add(Ui.label("Search Trains", 17, Ui.TEXT, true), grid);

        grid.gridy = 1;
        String[] labels = {"From Station", "To Station", "Travel Date"};
        JTextField[] fields = {source, destination, travelDate};
        travelDate.setText(LocalDate.now().plusDays(1).toString());

        for (int index = 0; index < labels.length; index++) {
            grid.gridx = index * 2;
            searchPanel.add(Ui.label(labels[index], 11, Ui.MUTED, false), grid);
            grid.gridx = index * 2 + 1;
            searchPanel.add(fields[index], grid);
        }

        grid.gridy = 2;
        grid.gridx = 0;
        grid.gridwidth = 6;
        searchPanel.add(Ui.button("SEARCH TRAINS", Ui.ACCENT, this::search), grid);

        grid.gridy = 3;
        JPanel quickRoutes = Ui.panel(Ui.SURFACE);
        quickRoutes.add(Ui.label("Quick routes:", 11, Ui.MUTED, false));

        String[][] routes = {
                {"VISAKHAPATNAM", "HYDERABAD"},
                {"HYDERABAD", "VISAKHAPATNAM"},
                {"VISAKHAPATNAM", "VIJAYAWADA"},
                {"CHENNAI", "KOLKATA"},
                {"MUMBAI", "BHUBANESHWAR"}
        };

        for (String[] route : routes) {
            String label = route[0].substring(0, 4) + " to " + route[1].substring(0, 4);
            quickRoutes.add(Ui.button(label, Ui.SURFACE2, () -> {
                source.setText(route[0]);
                destination.setText(route[1]);
                search();
            }));
        }

        searchPanel.add(quickRoutes, grid);
        searchPanel.setAlignmentX(LEFT_ALIGNMENT);
        return searchPanel;
    }

    private void configureTrainTable() {
        Ui.setColumnWidths(trainTable, 60, 180, 115, 115, 70, 70, 55, 140);
        trainTable.setPreferredScrollableViewportSize(new Dimension(850, 8 * 27 + 25));
        trainTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectTrain();
            }
        });
    }

    private void buildBookingForm() {
        bookingForm.setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        grid.insets = new Insets(5, 8, 5, 8);
        grid.gridy = 0;
        grid.gridx = 0;
        grid.gridwidth = 4;
        bookingForm.add(bookingTitle, grid);

        String[] labels = {"Passenger Name", "Age", "Class", "Seats"};
        JComponent[] fields = {passengerName, passengerAge, travelClass, seats};
        travelClass.setPrototypeDisplayValue("EC (Executive Chair) - INR 1234 [100 seats]");

        for (int index = 0; index < labels.length; index++) {
            grid.gridy = 1 + index / 2;
            grid.gridx = (index % 2) * 2;
            grid.gridwidth = 1;
            bookingForm.add(Ui.label(labels[index], 11, Ui.MUTED, false), grid);
            grid.gridx++;
            bookingForm.add(fields[index], grid);
        }

        grid.gridy = 3;
        grid.gridx = 0;
        grid.gridwidth = 4;
        bookingForm.add(fare, grid);
        grid.gridy = 4;
        bookingForm.add(Ui.button("CONFIRM BOOKING", Ui.ACCENT, this::book), grid);

        bookingForm.setVisible(false);
        bookingForm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        bookingForm.setAlignmentX(LEFT_ALIGNMENT);
    }

    /** Searches the database and places matching trains into the table. */
    private void search() {
        String from = source.getText().trim().toUpperCase();
        String to = destination.getText().trim().toUpperCase();

        if (from.isEmpty() || to.isEmpty()) {
            showError("Enter both the source and destination.");
            return;
        }

        foundTrains.clear();
        foundTrains.addAll(app.booking.searchTrains(from, to));
        tableModel.setRowCount(0);
        selectedTrain = null;
        bookingForm.setVisible(false);
        travelClass.removeAllItems();
        fare.setText("");

        for (Object[] train : foundTrains) {
            List<String> availableClasses = new ArrayList<>();
            for (int index = 0; index < BookingService.CLASSES.length; index++) {
                if (((Number) train[7 + index]).intValue() > 0) {
                    availableClasses.add(BookingService.CLASSES[index]);
                }
            }

            tableModel.addRow(new Object[]{
                    train[0], train[1], train[2], train[3], train[4], train[5],
                    train[6] + " hours", String.join(", ", availableClasses)
            });
        }

        message.setText(foundTrains.isEmpty()
                ? "No trains found from " + from + " to " + to + "."
                : foundTrains.size() + " train(s) found. Select a row to continue.");
    }

    /** Loads the available classes after the user selects a train row. */
    private void selectTrain() {
        int row = trainTable.getSelectedRow();
        if (row < 0 || row >= foundTrains.size()) {
            return;
        }

        selectedTrain = foundTrains.get(row);
        bookingTitle.setText(selectedTrain[0] + " - " + selectedTrain[1]
                + " (" + selectedTrain[2] + " to " + selectedTrain[3] + ")");

        travelClass.removeAllItems();
        for (Object[] availableClass : app.booking.getAvailableClasses(
                ((Number) selectedTrain[0]).intValue())) {
            String code = (String) availableClass[0];
            int classIndex = Arrays.asList(BookingService.CLASSES).indexOf(code);
            int classFare = ((Number) selectedTrain[7 + classIndex]).intValue();

            if (classFare > 0) {
                travelClass.addItem(code + " (" + BookingService.CLASS_NAMES[classIndex]
                        + ") - INR " + classFare + " [" + availableClass[1] + " seats]");
            }
        }

        bookingForm.setVisible(true);
        updateFare();
    }

    private String selectedClassCode() {
        Object selected = travelClass.getSelectedItem();
        return selected == null ? "" : selected.toString().split("\\s+")[0];
    }

    private void updateFare() {
        if (selectedTrain == null || selectedClassCode().isEmpty()) {
            return;
        }

        int classFare = app.booking.getFare(
                ((Number) selectedTrain[0]).intValue(), selectedClassCode());
        int numberOfSeats = (Integer) seats.getValue();
        fare.setText("Total Fare: INR " + (classFare * numberOfSeats)
                + " (" + numberOfSeats + " x INR " + classFare + ")");
    }

    /** Validates the form and sends the booking request to BookingService. */
    private void book() {
        if (selectedTrain == null) {
            return;
        }

        String passenger = passengerName.getText().trim();
        String ageText = passengerAge.getText().trim();
        String classCode = selectedClassCode();
        String date = travelDate.getText().trim();

        if (passenger.isEmpty() || ageText.isEmpty() || classCode.isEmpty()) {
            showError("Please complete all booking fields.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            int numberOfSeats = (Integer) seats.getValue();
            var result = app.booking.bookTicket(
                    (String) app.currentUser.get("username"),
                    passenger,
                    age,
                    ((Number) selectedTrain[0]).intValue(),
                    date,
                    classCode,
                    numberOfSeats);

            if (!result.ok()) {
                showError("Booking failed: " + result.value());
                return;
            }

            String details = "PNR: " + result.value()
                    + "\nPassenger: " + passenger
                    + "\nTrain: " + selectedTrain[1]
                    + "\nClass: " + classCode
                    + "\nSeats: " + numberOfSeats
                    + "\nDate: " + date
                    + "\n\nPlease save your PNR number.";
            JOptionPane.showMessageDialog(
                    this, details, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            bookingForm.setVisible(false);
            selectedTrain = null;
        } catch (NumberFormatException exception) {
            showError("Age must be a valid number.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
