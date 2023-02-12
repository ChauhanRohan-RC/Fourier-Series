package ui.panels;

import app.App;
import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexDomainFunctionI;
import function.definition.DomainAnimationDurationScalerI;
import misc.Format;
import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionMeta;
import provider.FunctionType;
import provider.SimpleFunctionProvider;
import rotor.FunctionState;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class FunctionExportPanel extends JPanel {

    private static final String TAG = "FunctionExportPanel";

    private static int sCounter;

    public static int nextFunctionNumber() {
        return ++sCounter;
    }

    public enum ExportMode {
        EXPORT_TO_FOURIER_UI("Export to Fourier Ui"),
        SAVE_FUNCTION_STATE("Save Function State")
        ;

        public final String displayName;

        ExportMode(String displayName) {
            this.displayName = displayName;
        }
    }


    private static final Dimension SIZE_ENTRY = new Dimension(200, 30);
    private static final int VERTICAL_SPACE = 10;


    @NotNull
    private final Ui parent;

    private final JTextField nameEntry;
    private final JTextField speedEntry;
    private final JCheckBox closeOnDoneCheck;

    private final ButtonGroup expModeGroup;

    public FunctionExportPanel(@NotNull Ui parent) {
        this.parent = parent;

        nameEntry = new JTextField();
        nameEntry.setPreferredSize(SIZE_ENTRY.getSize());

        speedEntry = new JTextField("1");
        speedEntry.setPreferredSize(SIZE_ENTRY.getSize());

        closeOnDoneCheck = new JCheckBox("Close after export");
        closeOnDoneCheck.setSelected(false);
        closeOnDoneCheck.setVisible(false);

        expModeGroup = new ButtonGroup();
        final List<JRadioButton> exportModeButtons = new LinkedList<>();

        for (ExportMode expMode: ExportMode.values()) {
            final JRadioButton bt = new JRadioButton(expMode.displayName);
            bt.setActionCommand(expMode.name());

            expModeGroup.add(bt);
            exportModeButtons.add(bt);
        }

        exportModeButtons.get(0).setSelected(true);     // first is selected


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JPanel namePanel = new JPanel(new BorderLayout(4, 4));
        namePanel.setBorder(BorderFactory.createTitledBorder("Function Name"));
        namePanel.add(nameEntry);
        addComp(namePanel);

        final JPanel speedPanel = new JPanel(new BorderLayout(4, 4));
        speedPanel.setBorder(BorderFactory.createTitledBorder("Animation Speed"));
        speedPanel.add(speedEntry);
        addComp(speedPanel);

        final JPanel expModePanel = new JPanel();
        expModePanel.setBorder(BorderFactory.createTitledBorder("Export Mode"));
        expModePanel.setLayout(new BoxLayout(expModePanel, BoxLayout.Y_AXIS));
        for (JComponent bt: exportModeButtons) {
            bt.setAlignmentX(LEFT_ALIGNMENT);
            expModePanel.add(bt);
            expModePanel.add(Box.createVerticalStrut(4));
        }

        addComp(expModePanel);

        addComp(closeOnDoneCheck);
    }

    private void addComp(@NotNull Component component) {
        addComp(component, VERTICAL_SPACE);
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


    public boolean isCloseOnDoneVisible() {
        return closeOnDoneCheck.isVisible();
    }

    public void setShowCloseOnDone(boolean showCloseOnDone) {
        closeOnDoneCheck.setVisible(showCloseOnDone);
    }

    public boolean isCloseOnDoneSelected() {
        return closeOnDoneCheck.isSelected();
    }

    public void setCloseOnDoneSelected(boolean closeOnDone) {
        closeOnDoneCheck.setSelected(closeOnDone);
    }



    /**
     * @return {@link ExportMode export mode}
     * */
    @Nullable
    public ExportMode showDialog(@NotNull ComplexDomainFunctionI function, @NotNull FunctionType type, @Nullable String defaultName) {
        if (Format.isEmpty(defaultName)) {
            defaultName = "Function " + nextFunctionNumber();
        }

        nameEntry.setText(defaultName);
        if (function instanceof DomainAnimationDurationScalerI scaler) {
            final float scale = scaler.getDomainAnimationDurationScale();

            speedEntry.setText(scale > 0? String.format("%.2f", 1 / scale): "1");
        } else {
            speedEntry.setText("1");
        }

        final String title = "Export Function";
        final int op = JOptionPane.showConfirmDialog(parent.getFrame(), this, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION)
            return null;

        String name = nameEntry.getText();
        if (Format.isEmpty(name)) {
            name = defaultName;
        }

        final String speedStr = speedEntry.getText();
        if (Format.notEmpty(speedStr)) {
            try {
                final float speed = Float.parseFloat(speedStr);
                if (speed > 0) {
                    final float frac = 1 / speed;
                    if (function instanceof DomainAnimationDurationScalerI scaler) {
                        scaler.setDomainAnimationDurationScale(frac);
                    } else {
                        function = new ComplexDomainFunctionWrapper(function).setDomainAnimationDurationScale(frac);
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid input: failed to parse animation speed: " + speedStr, e);
            }
        }

        // Export
        final FunctionMeta meta = new FunctionMeta(type, name);

        ExportMode expMode;
        final ButtonModel sbm = expModeGroup.getSelection();
        if (sbm == null) {
            expMode = ExportMode.EXPORT_TO_FOURIER_UI;      // default
        } else {
            expMode = ExportMode.valueOf(sbm.getActionCommand());
        }

        switch (expMode) {
            case EXPORT_TO_FOURIER_UI -> App.findOrLaunchFourierUi().addSelectFunctionProvider(new SimpleFunctionProvider(meta, function));
            case SAVE_FUNCTION_STATE -> Ui.askSaveFunctionStateToFIle(parent, FunctionState.from(meta, function));
            default -> throw new AssertionError("Unknown Export Mode: " + expMode);
        }

        return expMode;
    }

}
