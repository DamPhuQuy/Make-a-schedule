package com.schedule.app.client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;

import com.schedule.app.client.api.ReminderApiService;
import com.schedule.app.client.dto.ReminderDTO;

import java.util.List;

public class ReminderSelectionUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JComboBox<String> cbbReminder;
    private ReminderApiService reminderApi;
    private List<ReminderDTO> reminders;

    private final Color primaryColor = new Color(75, 119, 190);
    private final Color hoverColor = new Color(106, 149, 215);
    private final Color backgroundColor = new Color(245, 245, 250);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

    public ReminderSelectionUI() {
        this.reminderApi = new ReminderApiService();

        setTitle("Chọn bộ nhắc");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 500, 300);
        setResizable(false);
        setLocationRelativeTo(null);

        contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(250, 250, 255),
                    0, getHeight(), backgroundColor
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 15));

        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        contentPane.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        loadReminders();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Chọn bộ nhắc");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                int shadowSize = 5;
                for (int i = 0; i < shadowSize; i++) {
                    float alpha = 0.1f * (shadowSize - i) / shadowSize;
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fill(new RoundRectangle2D.Double(
                        i, i, getWidth() - i * 2, getHeight() - i * 2, 15, 15
                    ));
                }

                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(
                    shadowSize, shadowSize,
                    getWidth() - shadowSize * 2,
                    getHeight() - shadowSize * 2,
                    15, 15
                ));
                g2.dispose();
            }
        };
        containerPanel.setOpaque(false);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel() {
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        formPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblReminder = new JLabel("Chọn bộ nhắc:");
        lblReminder.setFont(labelFont);
        formPanel.add(lblReminder, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        cbbReminder = createStyledComboBox(new String[] {"Đang tải..."});
        formPanel.add(cbbReminder, gbc);

        containerPanel.add(formPanel, BorderLayout.CENTER);
        return containerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        JButton btnConfirm = createStyledButton("Xác nhận");
        btnConfirm.setPreferredSize(new Dimension(150, 40));
        btnConfirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleConfirm();
            }
        });

        JButton btnCancel = createStyledButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(150, 40));
        btnCancel.setBackground(new Color(230, 230, 230));
        btnCancel.setForeground(new Color(80, 80, 80));
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnConfirm);
        panel.add(btnCancel);
        return panel;
    }

    private void loadReminders() {
        try {
            reminders = reminderApi.getAllReminders();
            String[] reminderTitles = reminders.stream()
                    .map(ReminderDTO::getTitle)
                    .toArray(String[]::new);

            cbbReminder.setModel(new DefaultComboBoxModel<>(reminderTitles));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải danh sách bộ nhắc: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleConfirm() {
        int selectedIndex = cbbReminder.getSelectedIndex();
        if (selectedIndex < 0 || reminders == null || reminders.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn một bộ nhắc!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReminderDTO selectedReminder = reminders.get(selectedIndex);

        // TODO: Add reminder to the last created appointment
        JOptionPane.showMessageDialog(this,
            "Đã thêm bộ nhắc: " + selectedReminder.getTitle(),
            "Thành công", JOptionPane.INFORMATION_MESSAGE);

        showMyCalendarAndDispose();
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(mainFont);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(50, 50, 50));
        comboBox.setPreferredSize(new Dimension(250, 30));
        comboBox.setFocusable(true);
        comboBox.setEnabled(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 230), 1, true),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        return comboBox;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(primaryColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON
                );

                AbstractButton b = (AbstractButton)c;
                ButtonModel m = b.getModel();

                Color bgColor = m.isPressed() ? primaryColor.darker()
                              : m.isRollover() ? hoverColor
                              : primaryColor;

                GradientPaint gp = new GradientPaint(
                    0, 0, bgColor,
                    0, c.getHeight(),
                    new Color(
                        Math.max(bgColor.getRed() - 20, 0),
                        Math.max(bgColor.getGreen() - 20, 0),
                        Math.max(bgColor.getBlue() - 20, 0)
                    )
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);

                if (!m.isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fillRoundRect(
                      2, 2, c.getWidth() - 4, c.getHeight() / 2 - 2, 10, 10
                    );
                }

                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 12, 12);

                g2.dispose();
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setCursor(Cursor.getDefaultCursor());
            }
        });

        return btn;
    }

    private void showMyCalendarAndDispose() {
        MyCalendarUI myCalendar = new MyCalendarUI();
        myCalendar.setVisible(true);
        this.dispose();
    }
}
