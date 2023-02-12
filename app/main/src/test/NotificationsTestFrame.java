package test;

import app.R;
import async.Async;
import async.Consumer;
import ui.frames.FourierUi;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class NotificationsTestFrame extends JFrame {

    private int notificationCount;

    public NotificationsTestFrame() {
        super("Notifications Test");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(Ui.windowBoundsCenterScreen(FourierUi.INITIAL_WIDTH, FourierUi.INITIAL_HEIGHT));
        setMinimumSize(FourierUi.MINIMUM_SIZE);
        setLayout(new BorderLayout());

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new OverlayLayout(mainPanel));
        mainPanel.setBackground(Color.BLACK);

        final JPanel contentsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        contentsPanel.add(new JLabel("Content"));
        contentsPanel.setBackground(new Color(70, 70, 70));

        // Overlay
        final JPanel overlayPanel = new JPanel(new GridBagLayout());
        overlayPanel.setOpaque(false);

        final JPanel notificationPanel = new JPanel();
        notificationPanel.setLayout(new GridBagLayout());
        notificationPanel.setOpaque(false);
        notificationPanel.setBackground(Color.BLUE.brighter());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1;
        gbc.gridwidth = gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        overlayPanel.add(Box.createGlue(), gbc);

        final GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = gbc2.gridy = 1;
        gbc2.weightx = gbc2.weighty = 0;
        gbc2.gridwidth = gbc2.gridheight = 1;
        overlayPanel.add(notificationPanel, gbc2);


        mainPanel.add(overlayPanel);
        mainPanel.add(contentsPanel);

        // controls
        final JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBackground(Color.CYAN);

        final JPanel controls = new JPanel();
        controls.setPreferredSize(new Dimension(100, 80));
        controlsPanel.add(controls);

        add(controlsPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);


        final Consumer<String> not = title -> {
            final JPanel content = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));

            final JLabel label = new JLabel(title);
            label.setFont(label.getFont().deriveFont(R.RANDOM.nextFloat(10, 40)));
            content.add(label);

            final GridBagConstraints cons = new GridBagConstraints();
            cons.gridx = 0;
            cons.gridy = notificationCount;
            cons.fill = GridBagConstraints.NONE;
            cons.weightx = cons.weighty = 0;
            cons.anchor = GridBagConstraints.EAST;
            cons.insets = new Insets(5, 5,5, 5);        // margins

            notificationPanel.add(content, cons);
            notificationPanel.revalidate();
            notificationCount++;

            Async.uiPost(() -> {
                notificationPanel.remove(content);
                notificationPanel.revalidate();
                notificationCount--;
            }, 2000);
        };

        Async.uiPost("First Notification", not, 500);
        Async.uiPost("Second Notification blah blah blah", not, 1000);

    }

}
