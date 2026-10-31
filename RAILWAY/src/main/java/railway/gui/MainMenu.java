package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Main navigation screen shown after a successful login. */
public final class MainMenu extends JPanel {

    private final SmartRailwaySystem app;
    private final JLabel userLabel = Ui.label("", 12, Color.WHITE, false);
    private final JButton adminButton;

    public MainMenu(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JPanel navigation = new JPanel(new BorderLayout());
        navigation.setBackground(Ui.ACCENT);
        navigation.setBorder(new javax.swing.border.EmptyBorder(12, 20, 12, 12));
        navigation.add(
                Ui.label("Smart Railway Reservation System", 18, Color.WHITE, true),
                BorderLayout.WEST);

        JPanel accountActions = Ui.panel(Ui.ACCENT);
        accountActions.add(Ui.button("Logout", Color.decode("#c2540a"), this::logout));
        accountActions.add(userLabel);
        navigation.add(accountActions, BorderLayout.EAST);
        add(navigation, BorderLayout.NORTH);

        JPanel body = Ui.panel(Ui.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(Box.createVerticalStrut(42));

        JLabel heading = Ui.label("What would you like to do?", 23, Ui.TEXT, true);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        body.add(heading);

        JLabel subheading = Ui.label("Choose an option below", 13, Ui.MUTED, false);
        subheading.setAlignmentX(CENTER_ALIGNMENT);
        body.add(subheading);
        body.add(Box.createVerticalStrut(20));

        String[][] actions = {
                {"SEARCH", "Search and Book Train", "#f97316", "booking"},
                {"PNR", "Check PNR Status", "#3b82f6", "pnr"},
                {"CANCEL", "Cancel Ticket", "#ef4444", "cancel"},
                {"REPORTS", "View Reports", "#22c55e", "reports"}
        };

        JPanel actionGrid = Ui.panel(Ui.BG);
        actionGrid.setLayout(new GridLayout(2, 2, 24, 20));

        for (String[] action : actions) {
            actionGrid.add(createActionCard(action[0], action[1], action[2], action[3]));
        }

        actionGrid.setMaximumSize(new Dimension(590, 320));
        actionGrid.setAlignmentX(CENTER_ALIGNMENT);
        body.add(actionGrid);

        adminButton = Ui.button("Admin Panel", Ui.SURFACE2, () -> app.show("admin"));
        adminButton.setAlignmentX(CENTER_ALIGNMENT);
        body.add(Box.createVerticalStrut(12));
        body.add(adminButton);

        add(body, BorderLayout.CENTER);
    }

    private JPanel createActionCard(String shortName, String title,
                                    String colour, String screenName) {
        JPanel card = Ui.card();
        card.setBorder(new LineBorder(Color.decode(colour), 2));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel shortLabel = Ui.label(shortName, 22, Ui.TEXT, true);
        shortLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(shortLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel titleLabel = Ui.label(title, 15, Ui.TEXT, true);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));

        JButton openButton = Ui.button("Open", Color.decode(colour), () -> app.show(screenName));
        openButton.setAlignmentX(CENTER_ALIGNMENT);
        card.add(openButton);
        return card;
    }

    private void logout() {
        app.currentUser = null;
        app.show("login");
    }

    /** Refreshes the account label and admin-only button before this page appears. */
    public void refresh() {
        if (app.currentUser == null) {
            return;
        }

        userLabel.setText(
                "User: " + app.currentUser.get("username")
                        + " (" + app.currentUser.get("role") + ")");
        adminButton.setVisible("admin".equals(app.currentUser.get("role")));
    }
}
