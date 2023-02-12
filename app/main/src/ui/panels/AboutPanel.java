package ui.panels;

import app.R;
import misc.Format;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AboutPanel extends JPanel {

    public static final String TAG = "About Panel";

    public AboutPanel() {
        final JLabel footerImageLabel = new JLabel();
        final Image footerImg = R.createImage(R.IMG_RC_STUDIO500);
        if (footerImg != null) {
            footerImageLabel.setIcon(new ImageIcon(footerImg));
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JLabel titleLabel = new JLabel(R.APP_NAME + " " + R.APP_VERSION);
        Font titleFont = R.getFontAquire();
        if (titleFont == null)
            titleFont = titleLabel.getFont();
        titleLabel.setFont(titleFont.deriveFont(26f));

        final JLabel infoLabel = new JLabel("build " + R.BUILD_VERSION + ", built on " + R.BUILD_DATE);

        addComp(titleLabel);
        addComp(infoLabel);

        final String devUri = R.AUTHOR_DEVELOPER_PAGE_URI;
        addEntry("Author", R.AUTHOR + (Format.notEmpty(devUri)? " (click to see developer page)": ""), Format.notEmpty(devUri)? () -> Ui.browseNoThrow(devUri, "Failed to browse to Developer Page"): null);
        addEntry("Contact", R.AUTHOR_EMAIL, () -> Ui.openMainClientNoThrow(R.AUTHOR_EMAIL, "Failed to mail author"));

        final String srcUri = R.PROJECT_SOURCE_URI;
        if (Format.notEmpty(srcUri)) {
            addEntry("Project Source", srcUri, () -> Ui.browseNoThrow(srcUri, "Failed to browse to Project Source"));
        }

        if (footerImg != null) {
            addComp(footerImageLabel);
        }
    }

    private void addEntry(@Nullable String title, @NotNull String text, @Nullable Runnable onCLickAction) {
        final JPanel panel = new JPanel(new BorderLayout());
        if (Format.notEmpty(title)) {
            panel.setBorder(BorderFactory.createTitledBorder(title));
        }

        final JLabel label = new JLabel(text);
        if (onCLickAction != null) {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onCLickAction.run();
                }
            });
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        panel.add(label);

        addComp(panel);
    }


    private void addComp(@NotNull Component component) {
        addComp(component, 6);
    }

    private void addComp(@NotNull Component component, int vgap) {
        if (component instanceof JComponent jc) {
            jc.setAlignmentX(LEFT_ALIGNMENT);
        }

        if (getComponentCount() > 0 && vgap > 0) {
            add(Box.createVerticalStrut(vgap));
        }

        add(component);
    }

    public void showDialog(@Nullable Component parent) {
        final int option = JOptionPane.showOptionDialog(parent, this, "About " + R.APP_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, R.createIcon(R.IMG_APP_ICON_COLORFUL, 100, 100, false), new String[] { "Copy", "Close" }, "Copy");

        // Copy
        if (option == 0) {
            Ui.copyToClipboard(R.createAppDescription());
        }
    }
}
