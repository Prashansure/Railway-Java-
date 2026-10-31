package railway;

import railway.db.DatabaseInitializer;
import railway.db.DBConnection;
import railway.gui.AdminPanel;
import railway.gui.CancellationScreen;
import railway.gui.LoginScreen;
import railway.gui.MainMenu;
import railway.gui.PNRStatusScreen;
import railway.gui.ReportsScreen;
import railway.gui.TrainBookingScreen;
import railway.services.AuthService;
import railway.services.BookingService;
import railway.services.ReportService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Application entry point and shared application state.
 *
 * The application uses one main window. Each screen is placed in the same
 * CardLayout, and showScreen() switches between those screens.
 */
public final class SmartRailwaySystem {

    public final JFrame root = new JFrame("Smart Railway Reservation System");
    public final DBConnection db = new DBConnection();
    public final AuthService auth = new AuthService(db);
    public final BookingService booking = new BookingService(db);
    public final ReportService reports = new ReportService(db);

    /** The user who is currently logged in, or null before login. */
    public Map<String, Object> currentUser;

    private final CardLayout cards = new CardLayout();
    private final JPanel pageHolder = new JPanel(cards);

    private MainMenu mainMenu;
    private PNRStatusScreen pnrStatus;
    private CancellationScreen cancellation;
    private ReportsScreen reportsScreen;
    private AdminPanel adminPanel;

    public SmartRailwaySystem() {
        // Create the database tables and the default admin account first.
        DatabaseInitializer.initialize(db);

        root.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        root.setSize(1000, 720);
        root.setMinimumSize(new Dimension(900, 650));
        root.setLocationRelativeTo(null);

        // Build each screen once and reuse it while the application is open.
        pageHolder.add(new LoginScreen(this), "login");

        mainMenu = new MainMenu(this);
        pageHolder.add(mainMenu, "menu");

        pageHolder.add(new TrainBookingScreen(this), "booking");

        pnrStatus = new PNRStatusScreen(this);
        pageHolder.add(pnrStatus, "pnr");

        cancellation = new CancellationScreen(this);
        pageHolder.add(cancellation, "cancel");

        reportsScreen = new ReportsScreen(this);
        pageHolder.add(reportsScreen, "reports");

        adminPanel = new AdminPanel(this);
        pageHolder.add(adminPanel, "admin");

        root.setContentPane(pageHolder);
        showScreen("login");
    }

    /** Switches the visible screen and refreshes data-dependent screens. */
    public void showScreen(String screenName) {
        if ("menu".equals(screenName)) {
            mainMenu.refresh();
        } else if ("pnr".equals(screenName)) {
            pnrStatus.onShow();
        } else if ("cancel".equals(screenName)) {
            cancellation.onShow();
        } else if ("reports".equals(screenName)) {
            reportsScreen.onShow();
        } else if ("admin".equals(screenName)) {
            adminPanel.onShow();
        }

        cards.show(pageHolder, screenName);
    }

    /** Kept as a small convenience for the existing screen classes. */
    public void show(String screenName) {
        showScreen(screenName);
    }

    public static void main(String[] args) {
        // Swing components should be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new SmartRailwaySystem().root.setVisible(true));
    }
}
