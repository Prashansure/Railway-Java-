package railway.gui;

import railway.SmartRailwaySystem;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/** Login and registration screen shown when the application starts. */
public final class LoginScreen extends JPanel {

    private final SmartRailwaySystem app;
    private final JTextField loginUser = Ui.field(24);
    private final JTextField captchaInput = Ui.field(16);
    private final JTextField registerUser = Ui.field(24);
    private final JPasswordField loginPassword = Ui.password(24);
    private final JPasswordField registerPassword = Ui.password(24);
    private final JPasswordField confirmPassword = Ui.password(24);
    private final JLabel captchaImage = new JLabel();
    private final JTabbedPane tabs = new JTabbedPane();
    private String captchaText = "";

    public LoginScreen(SmartRailwaySystem app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JPanel topLine = Ui.panel(Ui.ACCENT);
        topLine.setPreferredSize(new Dimension(1, 6));
        add(topLine, BorderLayout.NORTH);

        JPanel center = Ui.panel(Ui.BG);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalGlue());

        JLabel logo = Ui.label("RAILWAY", 28, Ui.ACCENT, true);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        center.add(logo);

        JLabel title = Ui.label("Smart Railway Reservation", 25, Ui.TEXT, true);
        title.setAlignmentX(CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(18));

        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        tabs.setPreferredSize(new Dimension(430, 410));
        tabs.setMaximumSize(new Dimension(430, 410));
        tabs.setAlignmentX(CENTER_ALIGNMENT);
        center.add(tabs);
        center.add(Box.createVerticalGlue());

        add(center, BorderLayout.CENTER);
        refreshCaptcha();
    }

    private JPanel buildLoginPanel() {
        JPanel panel = Ui.panel(Ui.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 0;
        grid.anchor = GridBagConstraints.WEST;
        grid.insets = new Insets(5, 4, 4, 4);

        panel.add(Ui.label("Username", 12, Ui.MUTED, false), grid);
        grid.gridy++;
        panel.add(loginUser, grid);
        grid.gridy++;
        panel.add(Ui.label("Password", 12, Ui.MUTED, false), grid);
        grid.gridy++;
        panel.add(loginPassword, grid);
        grid.gridy++;
        panel.add(Ui.label("Enter CAPTCHA", 12, Ui.MUTED, false), grid);
        grid.gridy++;

        JPanel captchaRow = Ui.panel(Ui.SURFACE);
        captchaRow.add(captchaImage);
        captchaRow.add(Ui.button("Refresh", Color.decode("#69778B"), this::refreshCaptcha));
        panel.add(captchaRow, grid);

        grid.gridy++;
        panel.add(captchaInput, grid);
        grid.gridy++;
        panel.add(Ui.button("LOGIN", Ui.ACCENT, this::login), grid);

        loginUser.addActionListener(event -> login());
        loginPassword.addActionListener(event -> login());
        captchaInput.addActionListener(event -> login());
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = Ui.panel(Ui.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = 0;
        grid.gridy = 0;
        grid.anchor = GridBagConstraints.WEST;
        grid.insets = new Insets(6, 4, 5, 4);

        panel.add(Ui.label("Username", 12, Ui.MUTED, false), grid);
        grid.gridy++;
        panel.add(registerUser, grid);
        grid.gridy++;
        panel.add(Ui.label("Password", 12, Ui.MUTED, false), grid);
        grid.gridy++;
        panel.add(registerPassword, grid);
        grid.gridy++;
        panel.add(Ui.label("Confirm Password", 12, Ui.MUTED, false), grid);
        grid.gridy++;
        panel.add(confirmPassword, grid);
        grid.gridy++;
        panel.add(Ui.button("REGISTER", Ui.ACCENT, this::register), grid);
        return panel;
    }

    /** Checks the form and asks AuthService to validate the credentials. */
    private void login() {
        String username = loginUser.getText().trim();
        String password = new String(loginPassword.getPassword()).trim();
        String enteredCaptcha = captchaInput.getText().trim().toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }
        if (enteredCaptcha.isEmpty()) {
            showError("Please enter the CAPTCHA text.");
            return;
        }
        if (!enteredCaptcha.equals(captchaText)) {
            showError("Incorrect CAPTCHA. Please try again.");
            refreshCaptcha();
            return;
        }

        var user = app.auth.login(username, password);
        if (user == null) {
            showError("Invalid username or password.");
            refreshCaptcha();
            return;
        }

        app.currentUser = user;
        loginUser.setText("");
        loginPassword.setText("");
        captchaInput.setText("");
        app.show("menu");
    }

    /** Validates the registration form and creates a normal user account. */
    private void register() {
        String username = registerUser.getText().trim();
        String password = new String(registerPassword.getPassword()).trim();
        String confirmation = new String(confirmPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (!password.equals(confirmation)) {
            showError("Passwords do not match.");
            return;
        }

        String error = app.auth.register(username, password);
        if (error == null) {
            JOptionPane.showMessageDialog(
                    this, "Registration successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            tabs.setSelectedIndex(0);
        } else {
            showError(error);
        }
    }

    /** Generates and draws a new CAPTCHA image. */
    private void refreshCaptcha() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder value = new StringBuilder();

        for (int index = 0; index < 6; index++) {
            value.append(characters.charAt(random.nextInt(characters.length())));
        }
        captchaText = value.toString();

        BufferedImage image = new BufferedImage(220, 70, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 220, 70);
        graphics.setColor(new Color(65, 80, 115));

        for (int index = 0; index < 9; index++) {
            graphics.drawLine(
                    random.nextInt(220), random.nextInt(70),
                    random.nextInt(220), random.nextInt(70));
        }

        graphics.setFont(new Font("Serif", Font.BOLD, 30));
        graphics.setColor(new Color(28, 40, 60));
        graphics.drawString(captchaText, 22, 47);
        graphics.dispose();

        captchaImage.setIcon(new ImageIcon(image));
        captchaInput.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
