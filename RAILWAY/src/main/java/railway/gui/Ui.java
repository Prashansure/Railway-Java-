package railway.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Shared Swing styling helpers.
 *
 * Keeping common component setup here makes every screen use the same colours,
 * fonts, spacing, and table behaviour.
 */
public final class Ui {

    public static final Color BG = Color.decode("#1a1f2e");
    public static final Color SURFACE = Color.decode("#232a3d");
    public static final Color SURFACE2 = Color.decode("#2d3548");
    public static final Color ACCENT = Color.decode("#f97316");
    public static final Color ORANGE = ACCENT;
    public static final Color TEXT = Color.decode("#e8eaf0");
    public static final Color MUTED = Color.decode("#9ca3af");
    public static final Color GREEN = Color.decode("#22c55e");
    public static final Color RED = Color.decode("#ef4444");
    public static final Color BLUE = Color.decode("#3b82f6");

    private Ui() {
        // Utility class; do not create an instance.
    }

    public static JPanel panel(Color background) {
        JPanel panel = new JPanel();
        panel.setBackground(background);
        return panel;
    }

    public static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    public static JButton button(String text, Color background, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(7, 13, 7, 13));
        button.addActionListener(event -> action.run());
        return button;
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        styleTextField(field);
        return field;
    }

    public static JPasswordField password(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleTextField(field);
        return field;
    }

    private static void styleTextField(JTextField field) {
        field.setBackground(SURFACE2);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
                new LineBorder(Color.decode("#374151")),
                new EmptyBorder(5, 7, 5, 7)));
    }

    /** Creates the coloured title bar used by all screens except login. */
    public static JPanel header(String title, Runnable back, Color color) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(color);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.add(button("Back", Color.decode("#c2540a"), back), BorderLayout.WEST);
        header.add(label(title, 15, Color.WHITE, true), BorderLayout.CENTER);
        return header;
    }

    /** Creates a read-only table model for the application's data tables. */
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(Color.decode("#374151"));
        table.setRowHeight(27);
        table.setSelectionBackground(Color.decode("#374151"));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setBackground(SURFACE2);
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        return table;
    }

    public static void setColumnWidths(JTable table, int... widths) {
        int count = Math.min(table.getColumnCount(), widths.length);
        for (int index = 0; index < count; index++) {
            table.getColumnModel().getColumn(index).setPreferredWidth(widths[index]);
        }
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.getViewport().setBackground(SURFACE);
        return scrollPane;
    }

    public static JScrollPane pageScroll(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG);
        return scrollPane;
    }

    public static JPanel card() {
        JPanel panel = panel(SURFACE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(Color.decode("#374151")),
                new EmptyBorder(18, 20, 18, 20)));
        return panel;
    }
}
