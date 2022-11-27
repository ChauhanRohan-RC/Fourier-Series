package test;

import app.R;
import app.Settings;
import function.internal.basic.MergedFunction;
import function.internal.basic.SineSignal;
import org.intellij.lang.annotations.Flow;
import provider.FunctionMeta;
import provider.FunctionType;
import rotor.StandardRotorStateManager;
import rotor.frequency.FixedStartFrequencyProvider;
import ui.audio.AudioListPlayer;
import ui.audio.source.PathAudioSource;
import ui.panels.FTGraphPanel;
import ui.panels.FTWinderPanel;
import ui.frames.FourierUi;
import ui.panels.FunctionGraphPanel;
import ui.util.Ui;
import util.async.Async;
import util.async.Consumer;
import util.async.Task;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.stream.Collectors;


public class Test extends JFrame {

    public static final String TAG = "TestFrame";

    private int notificationCount;

    public Test() {
        super("test Frame");

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
            label.setFont(label.getFont().deriveFont(new Random().nextFloat(10, 40)));
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


    public static void main(String[] args) {
        R.init();
        Settings.getSingleton();
//        final Test frame = new Test();

        final AudioListPlayer player = new AudioListPlayer(true, R.MUSIC_FILES.stream().map(PathAudioSource::new).collect(Collectors.toList()));
        player.play();

        Async.uiPost(player::playNext, 2000);
        Async.uiPost(player::pause, 4000);
        Async.uiPost(player::play, 6000);

        Async.uiPost(player::reset, 8000);
        Async.uiPost(player::play, 9000);
    }

}
