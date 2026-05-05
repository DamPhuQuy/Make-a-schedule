package com.schedule.app.client.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.Calendar;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import com.toedter.calendar.JCalendar;

public class CalendarUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Color primaryColor    = new Color(75, 119, 190);
    private final Color hoverColor      = new Color(106, 149, 215);
    private final Color backgroundColor = new Color(245, 245, 250);
    private final Font  mainFont        = new Font("Segoe UI", Font.PLAIN, 14);

    private JPanel contentPane;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
              UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) { /* ignore */ }

        EventQueue.invokeLater(() -> {
            CalendarUI f = new CalendarUI();
            f.setVisible(true);
        });
    }

    public CalendarUI() {
        setTitle("Lịch hẹn");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 500);
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
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setLayout(new BorderLayout(0, 20));
        setContentPane(contentPane);

        contentPane.add(createHeaderPanel(),   BorderLayout.NORTH);
        contentPane.add(createCalendarPanel(), BorderLayout.CENTER);
        contentPane.add(createButtonPanel(),   BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lbl = new JLabel("Lịch hẹn của bạn");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(50, 50, 50));
        header.add(lbl, BorderLayout.WEST);

        URL u = getClass().getClassLoader().getResource("icons/calendar.png");
        ImageIcon icon = (u != null) ? new ImageIcon(u) : null;

        JButton btnMy = createStyledButton("Lịch của tôi", icon);
        btnMy.addActionListener(e -> {
            MyCalendarUI mc = new MyCalendarUI();
            mc.setVisible(true);
        });
        header.add(btnMy, BorderLayout.EAST);

        return header;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel cont = new JPanel() {
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
                        i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20
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
        cont.setOpaque(false);
        cont.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cont.setLayout(new BorderLayout());

        JCalendar calendar = new JCalendar();
        calendar.setFont(mainFont);
        calendar.setWeekOfYearVisible(false);
        calendar.setDecorationBackgroundVisible(false);
        calendar.setDecorationBordersVisible(false);
        calendar.setSundayForeground(new Color(220, 50, 50));
        calendar.setWeekdayForeground(new Color(50, 50, 50));

        styleMonthYearChooser(calendar);

        styleDayButtons(calendar);
        calendar.addPropertyChangeListener("calendar",
            evt -> styleDayButtons(calendar)
        );

        cont.add(calendar, BorderLayout.CENTER);
        panel.add(cont, BorderLayout.CENTER);
        return panel;
    }

    private void styleMonthYearChooser(JCalendar calendar) {
        JPanel dateHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        dateHeader.setOpaque(false);

        Component monthComp = calendar.getMonthChooser().getComboBox();
        if (monthComp instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> combo = (JComboBox<String>) monthComp;
            combo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            combo.setBackground(Color.WHITE);
            combo.setForeground(primaryColor);

            combo.setPreferredSize(new Dimension(130, 32));
            combo.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));

            combo.setUI(new BasicComboBoxUI() {
                @Override
                protected ComboPopup createPopup() {
                    return new BasicComboPopup(comboBox) {
                        @Override
                        protected void configureList() {
                            super.configureList();
                            list.setBackground(Color.WHITE);
                            list.setSelectionBackground(new Color(230, 240, 255));
                            list.setSelectionForeground(primaryColor);
                            list.setBorder(null);
                            list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        }

                        @Override
                        protected void configurePopup() {
                            super.configurePopup();
                            setBackground(Color.WHITE);
                            setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                            ));
                        }
                    };
                }

                @Override
                protected JButton createArrowButton() {
                    JButton arrowButton = new JButton() {
                        @Override
                        public void paint(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                            );

                            if (getModel().isRollover()) {
                                g2.setColor(hoverColor);
                            } else {
                                g2.setColor(primaryColor);
                            }
                            int size = 9;
                            int x = getWidth()/2 - size/2;
                            int y = getHeight()/2 - size/2;
                            int[] xPoints = {x, x + size, x + size/2};
                            int[] yPoints = {y, y, y + size};
                            g2.fillPolygon(xPoints, yPoints, 3);
                            g2.dispose();
                        }

                        @Override
                        public Dimension getPreferredSize() {
                            return new Dimension(20, 20);
                        }
                    };
                    arrowButton.setContentAreaFilled(false);
                    arrowButton.setBorderPainted(false);
                    arrowButton.setFocusPainted(false);
                    return arrowButton;
                }

                @Override
                protected Rectangle rectangleForCurrentValue() {
                    Rectangle rect = super.rectangleForCurrentValue();
                    rect.y -= 1;
                    rect.height += 3;
                    return rect;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    GradientPaint gp = new GradientPaint(
                        bounds.x, bounds.y, Color.WHITE,
                        bounds.x, bounds.y + bounds.height, new Color(245, 248, 255)
                    );
                    g2.setPaint(gp);
                    g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

                    g2.setColor(new Color(200, 215, 240));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 10, 10);

                    g2.dispose();
                }
            });
        }

        Component yearComp = calendar.getYearChooser().getSpinner();
        if (yearComp instanceof JSpinner) {
            JSpinner spinner = (JSpinner) yearComp;
            spinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
            spinner.setPreferredSize(new Dimension(90, 32));
            spinner.setOpaque(false);

            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
                spinnerEditor.getTextField().setBackground(Color.WHITE);
                spinnerEditor.getTextField().setForeground(primaryColor);
                spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
                spinnerEditor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 16));
                spinnerEditor.getTextField().setBorder(null);

                spinnerEditor.setBorder(BorderFactory.createEmptyBorder(1, 3, 4, 3));

                spinner.setBorder(new Border() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        GradientPaint gp = new GradientPaint(
                            x, y, Color.WHITE,
                            x, y + height, new Color(245, 248, 255)
                        );
                        g2.setPaint(gp);
                        g2.fillRoundRect(x, y, width, height, 10, 10);

                        g2.setColor(new Color(200, 215, 240));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(x, y, width - 1, height - 1, 10, 10);

                        g2.dispose();
                    }

                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(2, 5, 4, 5);
                    }

                    @Override
                    public boolean isBorderOpaque() {
                        return false;
                    }
                });
            }
        }
    }

    private void styleDayButtons(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        for (Component c : dayPanel.getComponents()) {
            if (!(c instanceof JButton)) continue;
            JButton b = (JButton) c;

            b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);

            b.setUI(new BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();

                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    int size = Math.min(c.getWidth() - 6, c.getHeight() - 6);
                    int x = (c.getWidth() - size) / 2;
                    int y = (c.getHeight() - size) / 2;

                    if (model.isSelected()) {
                        GradientPaint gp = new GradientPaint(
                            0, 0, primaryColor,
                            0, c.getHeight(), primaryColor.darker()
                        );
                        g2.setPaint(gp);
                        g2.fillOval(x, y, size, size);

                        g2.setColor(new Color(255, 255, 255, 60));
                        g2.fillArc(x, y, size, size/2, 0, 180);
                    } else if (model.isPressed()) {
                        g2.setColor(new Color(180, 210, 255));
                        g2.fillOval(x, y, size, size);
                    } else if (model.isRollover()) {
                        g2.setColor(new Color(230, 240, 255));
                        g2.fillOval(x, y, size, size);
                    }

                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle viewRect = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                    Rectangle textRect = new Rectangle();
                    String text = SwingUtilities.layoutCompoundLabel(
                        fm, b.getText(), null, b.getVerticalAlignment(),
                        b.getHorizontalAlignment(), b.getVerticalTextPosition(),
                        b.getHorizontalTextPosition(), viewRect, new Rectangle(), textRect,
                        0
                    );

                    if (model.isSelected()) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else if (model.isPressed()) {
                        g2.setColor(primaryColor.darker());
                        g2.setFont(b.getFont());
                    } else if (b.getForeground() == calendar.getSundayForeground()) {
                        g2.setColor(calendar.getSundayForeground());
                        g2.setFont(b.getFont());
                    } else {
                        g2.setColor(Color.DARK_GRAY);
                        g2.setFont(b.getFont());
                    }

                    g2.drawString(text, textRect.x, textRect.y + fm.getAscent());
                    g2.dispose();
                }
            });

            for (MouseListener ml : b.getMouseListeners()) {
                if (ml instanceof DayButtonMouseListener) {
                    b.removeMouseListener(ml);
                }
            }
            b.addMouseListener(new DayButtonMouseListener(calendar));
        }
        dayPanel.revalidate();
        dayPanel.repaint();
    }

    private class DayButtonMouseListener extends MouseAdapter {
        private final JCalendar calendar;

        DayButtonMouseListener(JCalendar cal) {
            this.calendar = cal;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            JButton b = (JButton)e.getComponent();
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JButton b = (JButton)e.getComponent();
            b.setCursor(Cursor.getDefaultCursor());
            b.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            JButton b = (JButton)e.getComponent();
            b.getModel().setPressed(true);
            b.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            JButton b = (JButton)e.getComponent();
            b.getModel().setPressed(false);
            b.repaint();

            try {
                int d = Integer.parseInt(b.getText());
                Calendar cal = calendar.getCalendar();
                cal.set(Calendar.DAY_OF_MONTH, d);
                calendar.setCalendar(cal);
            } catch (NumberFormatException ex) { }
        }
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(
          new FlowLayout(FlowLayout.CENTER, 20, 10)
        );
        p.setOpaque(false);

        JButton ok = createStyledButton("Thêm cuộc hẹn", null);
        ok.setPreferredSize(new Dimension(150, 40));
        ok.addActionListener(e -> {
            JCalendar cal = findCalendar(contentPane);
            if (cal != null) {
                new AssignmentDetailUI(cal.getDate()).setVisible(true);
            }
        });

        JButton cancel = createStyledButton("Đóng", null);
        cancel.setPreferredSize(new Dimension(150, 40));
        cancel.setBackground(new Color(230, 230, 230));
        cancel.setForeground(new Color(80, 80, 80));
        cancel.addActionListener(e -> dispose());

        p.add(ok);
        p.add(cancel);
        return p;
    }

    private JCalendar findCalendar(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JCalendar) return (JCalendar)c;
            if (c instanceof Container) {
                JCalendar f = findCalendar((Container)c);
                if (f != null) return f;
            }
        }
        return null;
    }

    private JButton createStyledButton(String txt, ImageIcon ic) {
        JButton btn = new JButton(txt, ic);
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
}
