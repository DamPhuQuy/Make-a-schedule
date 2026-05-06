package com.schedule.app.client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import com.toedter.calendar.JCalendar;
import com.schedule.app.client.api.AppointmentApiService;
import com.schedule.app.client.dto.AppointmentDTO;
import com.schedule.app.client.dto.CreateAppointmentRequest;
import com.schedule.app.client.dto.UpdateAppointmentRequest;

public class ModernCalendarUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // Modern Color Palette
    private final Color bgColor = new Color(250, 251, 252);
    private final Color sidebarColor = new Color(255, 255, 255);
    private final Color accentColor = new Color(99, 102, 241);
    private final Color accentHover = new Color(79, 82, 221);
    private final Color textPrimary = new Color(17, 24, 39);
    private final Color textSecondary = new Color(107, 114, 128);
    private final Color cardBg = new Color(255, 255, 255);
    private final Color borderColor = new Color(229, 231, 235);

    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private final Font headingFont = new Font("Segoe UI", Font.BOLD, 16);
    private final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 14);

    private JTabbedPane tabbedPane;
    private AppointmentApiService appointmentApi;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* ignore */ }

        SwingUtilities.invokeLater(() -> {
            ModernCalendarUI frame = new ModernCalendarUI();
            frame.setVisible(true);
        });
    }

    public ModernCalendarUI() {
        appointmentApi = new AppointmentApiService();

        setTitle("Schedule Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgColor);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bgColor);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Schedule Manager");
        title.setFont(titleFont);
        title.setForeground(textPrimary);
        header.add(title, BorderLayout.WEST);

        return header;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(bodyFont);
        tabs.setBackground(cardBg);
        tabs.setForeground(textPrimary);

        // Tab 1: Calendar View
        JPanel calendarPanel = createCalendarPanel();
        tabs.addTab("Calendar", calendarPanel);

        // Tab 2: My Appointments
        JPanel appointmentsPanel = createAppointmentsPanel();
        tabs.addTab("My Appointments", appointmentsPanel);

        // Tab 3: Create New
        JPanel createPanel = createNewAppointmentPanel();
        tabs.addTab("Create New", createPanel);

        return tabs;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(cardBg);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select a date to create appointment");
        label.setFont(headingFont);
        label.setForeground(textPrimary);
        panel.add(label, BorderLayout.NORTH);

        JCalendar calendar = new JCalendar();
        calendar.setFont(bodyFont);
        calendar.setWeekOfYearVisible(false);
        calendar.setBackground(cardBg);

        calendar.addPropertyChangeListener("calendar", evt -> {
            Date selectedDate = calendar.getDate();
            if (selectedDate != null) {
                tabbedPane.setSelectedIndex(2); // Switch to Create tab
                updateCreatePanelDate(selectedDate);
            }
        });

        panel.add(calendar, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(cardBg);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(cardBg);

        JLabel label = new JLabel("All Appointments");
        label.setFont(headingFont);
        label.setForeground(textPrimary);
        headerPanel.add(label, BorderLayout.WEST);

        JButton refreshBtn = createButton("Refresh", false);
        refreshBtn.addActionListener(e -> loadAppointments());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Location", "Date", "Start", "End", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(bodyFont);
        appointmentTable.setRowHeight(35);
        appointmentTable.setSelectionBackground(new Color(238, 242, 255));
        appointmentTable.setSelectionForeground(textPrimary);
        appointmentTable.setGridColor(borderColor);

        JTableHeader tableHeader = appointmentTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(new Color(249, 250, 251));
        tableHeader.setForeground(textPrimary);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(cardBg);

        JButton deleteBtn = createButton("Delete Selected", false);
        deleteBtn.addActionListener(e -> deleteSelectedAppointment());
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadAppointments();

        return panel;
    }

    private JPanel createNewAppointmentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Create New Appointment");
        label.setFont(headingFont);
        label.setForeground(textPrimary);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(cardBg);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = createTextField();
        formPanel.add(nameField, gbc);

        // Location
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Location:"), gbc);
        gbc.gridx = 1;
        JTextField locationField = createTextField();
        formPanel.add(locationField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Date:"), gbc);
        gbc.gridx = 1;
        JCalendar dateChooser = new JCalendar();
        dateChooser.setPreferredSize(new Dimension(300, 200));
        formPanel.add(dateChooser, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        startTimePanel.setBackground(cardBg);
        JSpinner startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
        JSpinner startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        startHourSpinner.setFont(bodyFont);
        startMinuteSpinner.setFont(bodyFont);
        startTimePanel.add(startHourSpinner);
        startTimePanel.add(new JLabel(":"));
        startTimePanel.add(startMinuteSpinner);
        formPanel.add(startTimePanel, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("End Time:"), gbc);
        gbc.gridx = 1;
        JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        endTimePanel.setBackground(cardBg);
        JSpinner endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
        JSpinner endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        endHourSpinner.setFont(bodyFont);
        endMinuteSpinner.setFont(bodyFont);
        endTimePanel.add(endHourSpinner);
        endTimePanel.add(new JLabel(":"));
        endTimePanel.add(endMinuteSpinner);
        formPanel.add(endTimePanel, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"NORMAL_APPOINTMENT", "GROUP_MEETING"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(bodyFont);
        formPanel.add(typeCombo, gbc);

        // Reminder
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Reminder before:"), gbc);
        gbc.gridx = 1;
        JPanel reminderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        reminderPanel.setBackground(cardBg);

        JSpinner reminderDaysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 365, 1));
        JSpinner reminderHoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        JSpinner reminderMinutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        reminderDaysSpinner.setFont(bodyFont);
        reminderHoursSpinner.setFont(bodyFont);
        reminderMinutesSpinner.setFont(bodyFont);

        reminderPanel.add(reminderDaysSpinner);
        reminderPanel.add(new JLabel("ngày"));
        reminderPanel.add(reminderHoursSpinner);
        reminderPanel.add(new JLabel("giờ"));
        reminderPanel.add(reminderMinutesSpinner);
        reminderPanel.add(new JLabel("phút"));

        formPanel.add(reminderPanel, gbc);

        panel.add(formPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Create button
        JButton createBtn = createButton("Create Appointment", true);
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        createBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String location = locationField.getText().trim();
                Date selectedDate = dateChooser.getDate();
                int startHour = (Integer) startHourSpinner.getValue();
                int startMinute = (Integer) startMinuteSpinner.getValue();
                int endHour = (Integer) endHourSpinner.getValue();
                int endMinute = (Integer) endMinuteSpinner.getValue();
                String type = (String) typeCombo.getSelectedItem();

                if (name.isEmpty() || location.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Compare start and end time
                int startTimeInMinutes = startHour * 60 + startMinute;
                int endTimeInMinutes = endHour * 60 + endMinute;
                if (startTimeInMinutes >= endTimeInMinutes) {
                    JOptionPane.showMessageDialog(this, "Start time must be before end time", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate meetingDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                CreateAppointmentRequest request = new CreateAppointmentRequest(
                    1L, name, location, meetingDate, startHour, endHour, type
                );

                AppointmentDTO createdAppointment = appointmentApi.createAppointment(request);

                // Add custom reminder if specified
                int reminderDays = (Integer) reminderDaysSpinner.getValue();
                int reminderHours = (Integer) reminderHoursSpinner.getValue();
                int reminderMinutes = (Integer) reminderMinutesSpinner.getValue();

                // Calculate total minutes
                int totalReminderMinutes = (reminderDays * 1440) + (reminderHours * 60) + reminderMinutes;

                if (totalReminderMinutes > 0) {
                    // Create a custom reminder title based on input
                    String reminderTitle;
                    if (reminderDays > 0 && reminderHours == 0 && reminderMinutes == 0) {
                        reminderTitle = reminderDays + " ngày trước";
                    } else if (reminderDays == 0 && reminderHours > 0 && reminderMinutes == 0) {
                        reminderTitle = reminderHours + " giờ trước";
                    } else if (reminderDays == 0 && reminderHours == 0 && reminderMinutes > 0) {
                        reminderTitle = reminderMinutes + " phút trước";
                    } else {
                        // Combined format
                        StringBuilder sb = new StringBuilder();
                        if (reminderDays > 0) sb.append(reminderDays).append(" ngày ");
                        if (reminderHours > 0) sb.append(reminderHours).append(" giờ ");
                        if (reminderMinutes > 0) sb.append(reminderMinutes).append(" phút ");
                        reminderTitle = sb.toString().trim() + "trước";
                    }

                    // Map to existing reminders
                    Long reminderId = null;
                    if (totalReminderMinutes == 15) reminderId = 1L;
                    else if (totalReminderMinutes == 30) reminderId = 2L;
                    else if (totalReminderMinutes == 60) reminderId = 3L;
                    else if (totalReminderMinutes == 1440) reminderId = 4L;

                    if (reminderId != null) {
                        appointmentApi.addReminderToAppointment(createdAppointment.getId(), reminderId);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Custom reminder time noted: " + reminderTitle + "\nNote: Only 15, 30, 60, or 1440 minutes are supported in the system.",
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                JOptionPane.showMessageDialog(this, "Appointment created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear form
                nameField.setText("");
                locationField.setText("");
                dateChooser.setDate(new Date());
                startHourSpinner.setValue(9);
                startMinuteSpinner.setValue(0);
                endHourSpinner.setValue(10);
                endMinuteSpinner.setValue(0);
                typeCombo.setSelectedIndex(0);
                reminderDaysSpinner.setValue(0);
                reminderHoursSpinner.setValue(0);
                reminderMinutesSpinner.setValue(0);

                // Refresh appointments tab
                loadAppointments();
                tabbedPane.setSelectedIndex(1);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(createBtn);

        return panel;
    }

    private void updateCreatePanelDate(Date date) {
        // This will be called when user selects date from calendar tab
        // We'll update the date in create panel
        Component createPanel = tabbedPane.getComponentAt(2);
        if (createPanel instanceof JPanel) {
            Component[] components = ((JPanel) createPanel).getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    Component[] formComponents = ((JPanel) comp).getComponents();
                    for (Component formComp : formComponents) {
                        if (formComp instanceof JCalendar) {
                            ((JCalendar) formComp).setDate(date);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void loadAppointments() {
        try {
            List<AppointmentDTO> appointments = appointmentApi.getAllAppointments();
            tableModel.setRowCount(0);

            for (AppointmentDTO apt : appointments) {
                tableModel.addRow(new Object[]{
                    apt.getId(),
                    apt.getName(),
                    apt.getLocation(),
                    apt.getMeetingDate(),
                    apt.getStartHour() + ":00",
                    apt.getEndHour() + ":00",
                    apt.getTypeAppointment()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentApi.deleteAppointment(id);
                JOptionPane.showMessageDialog(this, "Appointment deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(bodyFont);
        label.setForeground(textPrimary);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(bodyFont);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(primary ? Color.WHITE : textPrimary);
        btn.setBackground(primary ? accentColor : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(!primary);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));

        if (!primary) {
            btn.setBorder(BorderFactory.createLineBorder(borderColor));
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(primary ? accentHover : new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(primary ? accentColor : Color.WHITE);
            }
        });

        return btn;
    }
}
