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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import com.schedule.app.client.api.AppointmentApiService;
import com.schedule.app.client.dto.AppointmentDTO;
import com.schedule.app.client.dto.UpdateAppointmentRequest;

public class MyCalendarUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblNewLabel;
    private JButton btnSave;
    private AppointmentApiService appointmentApi;

    private final Color primaryColor = new Color(75, 119, 190);
    private final Color hoverColor = new Color(106, 149, 215);
    private final Color backgroundColor = new Color(245, 245, 250);
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);

    private boolean dataChanged = false;

    public MyCalendarUI() {
        this.appointmentApi = new AppointmentApiService();

        setTitle("Lịch hẹn của tôi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1000, 600);
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
        contentPane.setLayout(new BorderLayout(0, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        lblNewLabel = new JLabel("Danh sách các buổi hẹn");
        lblNewLabel.setFont(titleFont);
        lblNewLabel.setForeground(new Color(50, 50, 50));
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(lblNewLabel, BorderLayout.CENTER);

        contentPane.add(titlePanel, BorderLayout.NORTH);

        JPanel tablePanel = createTablePanel();
        contentPane.add(tablePanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        loadAllAppointments();
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout()) {
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
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table = new JTable();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    int column = table.getSelectedColumn();
                    if (row >= 0 && column >= 2 && column <= 7) {
                        table.editCellAt(row, column);
                        if (table.getEditorComponent() != null) {
                            table.getEditorComponent().requestFocus();
                        }
                    }
                }
            }
        });

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2 && column <= 7;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return LocalDate.class;
                } else if (columnIndex == 5 || columnIndex == 6) {
                    return Integer.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        tableModel.addColumn("STT");
        tableModel.addColumn("Mã sự kiện");
        tableModel.addColumn("Tên sự kiện");
        tableModel.addColumn("Vị trí");
        tableModel.addColumn("Ngày diễn ra");
        tableModel.addColumn("Giờ bắt đầu");
        tableModel.addColumn("Giờ kết thúc");
        tableModel.addColumn("Kiểu nhóm");

        table.setModel(tableModel);
        table.setFont(mainFont);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 240));
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(primaryColor);

        JTableHeader header = table.getTableHeader();
        header.setFont(headerFont);
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(primaryColor);
                label.setForeground(Color.WHITE);
                label.setFont(headerFont);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 255) : Color.WHITE);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        table.getModel().addTableModelListener(e -> {
            dataChanged = true;
            if (btnSave != null) {
                btnSave.setEnabled(true);
            }
        });

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(180);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(110);
        columnModel.getColumn(6).setPreferredWidth(110);
        columnModel.getColumn(7).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setColumnHeaderView(header);

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 190, 240);
                this.trackColor = Color.WHITE;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                                thumbBounds.width - 4, thumbBounds.height - 4,
                                10, 10);
                g2.dispose();
            }
        });

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));

        btnSave = createStyledButton("Lưu thay đổi");
        btnSave.setEnabled(false);
        btnSave.addActionListener(e -> saveAllChanges());

        JButton btnDetail = createStyledButton("Chi tiết");
        btnDetail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(contentPane,
                        "Vui lòng chọn một buổi hẹn từ danh sách",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                Long appId = (Long)table.getValueAt(selectedRow, 1);
                String name = (String)table.getValueAt(selectedRow, 2);
                String location = (String)table.getValueAt(selectedRow, 3);
                LocalDate date = (LocalDate)table.getValueAt(selectedRow, 4);
                Integer start = (Integer)table.getValueAt(selectedRow, 5);
                Integer end = (Integer)table.getValueAt(selectedRow, 6);

                // TODO: Create InfoDetailUI
                JOptionPane.showMessageDialog(contentPane,
                    "Chi tiết: " + name + " tại " + location,
                    "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnDelete = createStyledButton("Xóa");
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một buổi hẹn để xóa",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Long appId = (Long) table.getValueAt(row, 1);
            String appName = (String) table.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa buổi hẹn '" + appName + "' không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    appointmentApi.deleteAppointment(appId);
                    JOptionPane.showMessageDialog(this,
                        "Xóa buổi hẹn thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadAllAppointments();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Không thể xóa buổi hẹn: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnClose = createStyledButton("Đóng");
        btnClose.setBackground(new Color(230, 230, 230));
        btnClose.setForeground(new Color(80, 80, 80));
        btnClose.addActionListener(e -> {
            if (dataChanged) {
                int option = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có thay đổi chưa được lưu. Bạn có muốn lưu trước khi đóng không?",
                    "Thay đổi chưa lưu",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    if (saveAllChanges()) {
                        dispose();
                    }
                } else if (option == JOptionPane.NO_OPTION) {
                    dispose();
                }
            } else {
                dispose();
            }
        });

        JButton btnRefresh = createStyledButton("Làm mới");
        btnRefresh.setBackground(new Color(75, 190, 137));
        btnRefresh.addActionListener(e -> {
            if (dataChanged) {
                int option = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có thay đổi chưa được lưu. Bạn có muốn làm mới và hủy các thay đổi không?",
                    "Thay đổi chưa lưu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    loadAllAppointments();
                    dataChanged = false;
                    btnSave.setEnabled(false);
                }
            } else {
                loadAllAppointments();
            }
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDetail);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        return buttonPanel;
    }

    private void loadAllAppointments() {
        try {
            List<AppointmentDTO> appointments = appointmentApi.getAllAppointments();
            tableModel.setRowCount(0);
            int stt = 1;
            for (AppointmentDTO app : appointments) {
                tableModel.addRow(new Object[] {
                    stt++,
                    app.getId(),
                    app.getName(),
                    app.getLocation(),
                    app.getMeetingDate(),
                    app.getStartHour(),
                    app.getEndHour(),
                    app.getTypeAppointment()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi tải danh sách cuộc hẹn: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveAllChanges() {
        boolean allSaved = true;

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        for (int row = 0; row < table.getRowCount(); row++) {
            try {
                Long appId = (Long) table.getValueAt(row, 1);
                String name = (String) table.getValueAt(row, 2);
                String location = (String) table.getValueAt(row, 3);
                LocalDate date = (LocalDate) table.getValueAt(row, 4);
                Integer startHour = (Integer) table.getValueAt(row, 5);
                Integer endHour = (Integer) table.getValueAt(row, 6);
                String type = (String) table.getValueAt(row, 7);

                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên sự kiện không được để trống ở hàng " + (row + 1), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    allSaved = false;
                    continue;
                }

                if (location == null || location.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vị trí không được để trống ở hàng " + (row + 1), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    allSaved = false;
                    continue;
                }

                if (date == null) {
                    JOptionPane.showMessageDialog(this, "Ngày không hợp lệ ở hàng " + (row + 1), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    allSaved = false;
                    continue;
                }

                if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
                    JOptionPane.showMessageDialog(this, "Giờ bắt đầu và kết thúc phải từ 0-23 ở hàng " + (row + 1), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    allSaved = false;
                    continue;
                }

                if (startHour >= endHour) {
                    JOptionPane.showMessageDialog(this, "Giờ bắt đầu phải nhỏ hơn giờ kết thúc ở hàng " + (row + 1), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    allSaved = false;
                    continue;
                }

                UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                    name, location, date, startHour, endHour, type
                );
                appointmentApi.updateAppointment(appId, request);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xử lý dữ liệu ở hàng " + (row + 1) + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                allSaved = false;
            }
        }

        if (allSaved) {
            JOptionPane.showMessageDialog(this, "Đã lưu tất cả thay đổi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dataChanged = false;
            btnSave.setEnabled(false);
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu. Vui lòng kiểm tra và thử lại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
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
        btn.setPreferredSize(new Dimension(180, 40));

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
}
