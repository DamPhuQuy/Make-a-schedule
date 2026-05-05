package com.schedule.app.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.schedule.app.client.api.AppointmentApiService;
import com.schedule.app.client.api.ReminderApiService;
import com.schedule.app.client.dto.CreateAppointmentRequest;

public class AssignmentDetailUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtNameEvent;
    private JTextField txtLocation;
    private JRadioButton rdBtnDon, rdBtnNhom;
    private JComboBox<String> cbbStart, cbbEnd;
    private JButton btnConfirmDetail;
    private AppointmentApiService appointmentApi;
    private ReminderApiService reminderApi;

    private Date selectedDate;

    private final Color primaryColor = new Color(75, 119, 190);
    private final Color hoverColor = new Color(106, 149, 215);
    private final Color backgroundColor = new Color(245, 245, 250);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

    public AssignmentDetailUI(Date date) {
        this.selectedDate = date;
        this.appointmentApi = new AppointmentApiService();
        this.reminderApi = new ReminderApiService();

        setTitle("Chi tiết cuộc hẹn");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 450);
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
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Chi tiết cuộc hẹn");
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
        JLabel lblNameEvent = new JLabel("Tên sự kiện:");
        lblNameEvent.setFont(labelFont);
        formPanel.add(lblNameEvent, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        txtNameEvent = createStyledTextField();
        formPanel.add(txtNameEvent, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JLabel lblLocation = new JLabel("Vị trí:");
        lblLocation.setFont(labelFont);
        formPanel.add(lblLocation, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        txtLocation = createStyledTextField();
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        JLabel lblStartTime = new JLabel("Thời gian bắt đầu:");
        lblStartTime.setFont(labelFont);
        formPanel.add(lblStartTime, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        cbbStart = createStyledComboBox(new String[] {
            "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"
        });
        formPanel.add(cbbStart, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblEndTime = new JLabel("Thời gian kết thúc:");
        lblEndTime.setFont(labelFont);
        formPanel.add(lblEndTime, gbc);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        cbbEnd = createStyledComboBox(new String[] {
            "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22"
        });
        formPanel.add(cbbEnd, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblMeetingType = new JLabel("Kiểu cuộc họp:");
        lblMeetingType.setFont(labelFont);
        formPanel.add(lblMeetingType, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        radioPanel.setOpaque(false);

        rdBtnDon = createStyledRadioButton("Đơn");
        rdBtnNhom = createStyledRadioButton("Nhóm");

        ButtonGroup group = new ButtonGroup();
        group.add(rdBtnDon);
        group.add(rdBtnNhom);
        rdBtnDon.setSelected(true);

        radioPanel.add(rdBtnDon);
        radioPanel.add(rdBtnNhom);

        formPanel.add(radioPanel, gbc);

        containerPanel.add(formPanel, BorderLayout.CENTER);
        return containerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        btnConfirmDetail = createStyledButton("Xác nhận");
        btnConfirmDetail.setPreferredSize(new Dimension(150, 40));
        btnConfirmDetail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleConfirm();
            }
        });

        JButton btnCancel = createStyledButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(150, 40));
        btnCancel.setBackground(new Color(230, 230, 230));
        btnCancel.setForeground(new Color(80, 80, 80));
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnConfirmDetail);
        panel.add(btnCancel);
        return panel;
    }

    private void handleConfirm() {
        String name = txtNameEvent.getText();
        String location = txtLocation.getText();
        int startHour = Integer.parseInt((String)cbbStart.getSelectedItem());
        int endHour = Integer.parseInt((String)cbbEnd.getSelectedItem());
        String type = rdBtnDon.isSelected() ? "Đơn" : "Nhóm";

        if(name.isEmpty() || location.isEmpty()) {
            showCustomMessageDialog("Vui lòng điền đủ thông tin!", "Thông báo");
            return;
        }

        if(startHour >= endHour) {
            showCustomMessageDialog("Giờ bắt đầu phải bé hơn giờ kết thúc!", "Thông báo");
            return;
        }

        try {
            LocalDate meetingDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    1L, // Default user ID
                    name,
                    location,
                    meetingDate,
                    startHour,
                    endHour,
                    type
            );

            appointmentApi.createAppointment(request);

            int kq = showCustomConfirmDialog(
                "Bạn có muốn thêm bộ nhắc?",
                "Xác nhận"
            );

            if(kq == JOptionPane.YES_OPTION) {
                showReminderUIAndDispose();
            } else {
                showCustomMessageDialog("Thêm lịch hẹn mới thành công!", "Thông báo");
                showMyCalendarAndDispose();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showCustomMessageDialog("Lỗi khi tạo cuộc hẹn: " + ex.getMessage(), "Lỗi");
        }
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(mainFont);
        textField.setPreferredSize(new Dimension(300, 30));
        textField.setFocusable(true);
        textField.setEnabled(true);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 230), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(primaryColor, 2, true),
                    BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 230), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });

        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(mainFont);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(50, 50, 50));
        comboBox.setPreferredSize(new Dimension(100, 30));
        comboBox.setFocusable(true);
        comboBox.setEnabled(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 230), 1, true),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        return comboBox;
    }

    private JRadioButton createStyledRadioButton(String text) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setFont(mainFont);
        radioButton.setForeground(new Color(50, 50, 50));
        radioButton.setOpaque(false);
        radioButton.setFocusPainted(false);
        radioButton.setIcon(new RadioButtonIcon(false));
        radioButton.setSelectedIcon(new RadioButtonIcon(true));
        return radioButton;
    }

    private class RadioButtonIcon implements Icon {
        private final boolean selected;

        public RadioButtonIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(selected ? primaryColor : new Color(180, 180, 180));
            g2.fillOval(x, y, 16, 16);

            if (selected) {
                g2.setColor(Color.WHITE);
                g2.fillOval(x + 4, y + 4, 8, 8);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
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

    private int showCustomConfirmDialog(String message, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(mainFont);
        msgLabel.setHorizontalAlignment(JLabel.CENTER);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(msgLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));

        JButton yesButton = createStyledButton("Có");
        yesButton.setPreferredSize(new Dimension(100, 35));

        JButton noButton = createStyledButton("Không");
        noButton.setPreferredSize(new Dimension(100, 35));
        noButton.setBackground(new Color(230, 230, 230));
        noButton.setForeground(new Color(80, 80, 80));

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        final int[] result = {JOptionPane.NO_OPTION};

        JDialog dialog = new JDialog(this, title, true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private void showCustomMessageDialog(String message, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(mainFont);
        panel.add(msgLabel, gbc);

        JButton okButton = createStyledButton("OK");
        okButton.setPreferredSize(new Dimension(100, 35));

        gbc.gridy = 1;
        panel.add(okButton, gbc);

        JDialog dialog = new JDialog(this, title, true);
        dialog.setContentPane(panel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        okButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showMyCalendarAndDispose() {
        MyCalendarUI myCalendar = new MyCalendarUI();
        myCalendar.setVisible(true);
        this.dispose();
    }

    private void showReminderUIAndDispose() {
        ReminderSelectionUI reminderUI = new ReminderSelectionUI();
        reminderUI.setVisible(true);
        this.dispose();
    }
}
