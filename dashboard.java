import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

class Dashboard {
    record Slot(String room, LocalDate date, String time) {}

    private final Map<Slot, String> bookings = new LinkedHashMap<>();
    private final JComboBox<String> rooms = new JComboBox<>(new String[]{
        "Cedar (4 seats)", "Maple (8 seats)", "Oak (12 seats)"
    });
    private final JTextField date = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> times = new JComboBox<>(new String[]{
        "09:00–10:00", "10:00–11:00", "11:00–12:00", "13:00–14:00",
        "14:00–15:00", "15:00–16:00", "16:00–17:00"
    });
    private final JTextField name = new JTextField();
    private final JLabel status = new JLabel("Choose a room and time to get started.");
    private final DefaultTableModel rows = new DefaultTableModel(
        new String[]{"Room", "Date", "Time", "Booked by"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().show());
    }

    private void show() {
        JFrame frame = new JFrame("Room Booking");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        frame.setContentPane(content);

        JLabel heading = new JLabel("Room Booking");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 26f));
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.add(heading, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(2, 4, 10, 6));
        form.add(label("Room", rooms));
        form.add(label("Date (YYYY-MM-DD)", date));
        form.add(label("Time", times));
        form.add(label("Your name", name));
        form.add(rooms);
        form.add(date);
        form.add(times);
        form.add(name);
        header.add(form, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        JTable table = new JTable(rows);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton book = new JButton("Book room");
        book.addActionListener(event -> bookRoom());
        JButton cancel = new JButton("Cancel selected booking");
        cancel.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(event ->
            cancel.setEnabled(table.getSelectedRow() >= 0));
        cancel.addActionListener(event -> cancelBooking(table.getSelectedRow()));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton check = new JButton("Check availability");
        check.addActionListener(event -> {
            try {
                LocalDate day = LocalDate.parse(date.getText().strip());
                String room = (String) rooms.getSelectedItem();
                java.util.List<String> free = new java.util.ArrayList<>();
                for (int index = 0; index < times.getItemCount(); index++) {
                    String time = times.getItemAt(index);
                    if (!bookings.containsKey(new Slot(room, day, time))) free.add(time);
                }
                String message = day.isBefore(LocalDate.now()) ? "Choose today or a future date."
                    : free.isEmpty() ? "No slots available." : String.join("\n", free);
                JOptionPane.showMessageDialog(frame, message, "Room availability",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException error) {
                status.setText("Enter a valid date in YYYY-MM-DD format.");
            }
        });
        actions.add(check);
        actions.add(cancel);
        actions.add(book);
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.add(actions, BorderLayout.NORTH);
        footer.add(status, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);
        frame.getRootPane().setDefaultButton(book);
        frame.setMinimumSize(new Dimension(680, 380));
        frame.setSize(760, 470);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JLabel label(String title, JComponent field) {
        JLabel label = new JLabel(title);
        label.setLabelFor(field);
        return label;
    }

    private void bookRoom() {
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(date.getText().strip());
        } catch (DateTimeParseException error) {
            status.setText("Enter a valid date in YYYY-MM-DD format.");
            return;
        }
        if (selectedDate.isBefore(LocalDate.now())) {
            status.setText("Choose today or a future date.");
            return;
        }
        String bookedBy = name.getText().strip();
        if (bookedBy.isEmpty()) {
            status.setText("Enter your name to book a room.");
            return;
        }
        Slot slot = new Slot((String) rooms.getSelectedItem(), selectedDate,
            (String) times.getSelectedItem());
        if (bookings.putIfAbsent(slot, bookedBy) != null) {
            status.setText("That slot is taken.");
            return;
        }
        rows.addRow(new Object[]{slot.room(), slot.date(), slot.time(), bookedBy});
        status.setText("Booking confirmed for " + slot.room() + ".");
    }

    private void cancelBooking(int row) {
        if (row < 0) return;
        Slot slot = new Slot((String) rows.getValueAt(row, 0),
            (LocalDate) rows.getValueAt(row, 1), (String) rows.getValueAt(row, 2));
        bookings.remove(slot);
        rows.removeRow(row);
        status.setText("Booking cancelled. The slot is available again.");
    }
}
