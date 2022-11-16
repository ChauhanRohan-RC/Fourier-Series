package org.knowm.xchart;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import app.R;
import models.graph.GraphSeries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.chartpart.ChartZoom;
import org.knowm.xchart.internal.chartpart.Cursor;
import org.knowm.xchart.internal.chartpart.ToolTips;
import org.knowm.xchart.style.XYStyler;
import ui.action.BaseAction;
import ui.util.ChooserConfig;
import util.*;
import util.async.Consumer;

/**
 * A Swing JPanel that contains a Chart
 *
 * <p>Right-click + Save As... or ctrl+S pops up a Save As dialog box for saving the chart as PNG,
 * JPEG, etc. file.
 */
public class XChartPanel<T extends Chart<?, ?>> extends JPanel {

    public static final String TAG = "XChartPanel";
    public static final boolean DEFAULT_SHOW_POPUP_ON_MOUSE_TRIGGER = true;

    public final T chart;
    private final Dimension preferredSize;
    private String resetZoomString = "Reset Zoom";
    private ToolTips toolTips = null;

    public final BaseAction saveAction;
    public final BaseAction printAction;
    public final BaseAction exportAction;
    @Nullable
    private List<Consumer<JMenu>> extraMenuBinders;

    private boolean showPopupMenuOnMouseTrigger = DEFAULT_SHOW_POPUP_ON_MOUSE_TRIGGER;

    public XChartPanel(final T chart) {
        this.chart = chart;
        preferredSize = new Dimension(chart.getWidth(), chart.getHeight());

        saveAction = new SaveAction();
        printAction = new PrintAction();
        exportAction = new ExportAction();
        init();
    }

    protected void init() {

        // Right-click listener for saving chart
        this.addMouseListener(new MouseHandler());

        // Control+S key listener for saving chart
        final KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "save");
        this.getActionMap().put("save", saveAction);

        // Control+E key listener for saving chart
        final KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlE, "export");
        this.getActionMap().put("export", exportAction);

        // Control+P key listener for printing chart
        final KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlP, "print");
        this.getActionMap().put("print", printAction);

        // Mouse Listener for Zoom. Only available for XYCharts
        if (chart instanceof XYChart && ((XYStyler) chart.getStyler()).isZoomEnabled()) {
            ChartZoom chartZoom = new ChartZoom((XYChart) chart, this, resetZoomString);
            this.addMouseListener(chartZoom); // for clicking
            this.addMouseMotionListener(chartZoom); // for moving
        }

        // Mouse motion listener for Cursor
        if (chart instanceof XYChart && ((XYStyler) chart.getStyler()).isCursorEnabled()) {
            this.addMouseMotionListener(new Cursor(chart));
        }

        // Mouse motion listener for Tooltips
        if (chart.getStyler().isToolTipsEnabled()) {
            toolTips = new ToolTips(chart);
            this.addMouseMotionListener(toolTips); // for moving
        }

        // Recalculate Tooltips at component resize
        this.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent ev) {
                        if (chart.getStyler().isToolTipsEnabled()) {
                            XChartPanel.this.removeMouseListener(toolTips);
                            toolTips = new ToolTips(chart);
                            XChartPanel.this.addMouseMotionListener(toolTips);
                        }
                    }
                });
    }

    public void setSaveActionName(String saveActionName) {
        saveAction.setName(saveActionName);
    }

    public void setPrintActionName(String printActionName) {
        printAction.setName(printActionName);
    }

    public void setExportActionName(String exportActionName) {
        exportAction.setName(exportActionName);
    }

    /**
     * Set the "Reset Zoom" String if you want to localize it. This is on the button which resets the zoom
     * feature.
     */
    public void setResetZoomActionName(String resetZoomActionName) {
        this.resetZoomString = resetZoomActionName;
    }

    @NotNull
    public XYSeries addXYSeries(@NotNull GraphSeries series) {
        if (chart instanceof final XYChart xyChart) {
            return xyChart.addSeries(series.name(), series.xData(), series.yData(), series.errorBars());
        }

        throw new AssertionError("Chart not an instance of XYChArt");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        chart.paint(g2d, getWidth(), getHeight());
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public XChartPanel<T> addExtraMenuBinder(@NotNull Consumer<JMenu> binder) {
        if (extraMenuBinders == null) {
            extraMenuBinders = new LinkedList<>();
        }

        extraMenuBinders.add(binder);
        return this;
    }

    public boolean removeExtraMenuBinder(@NotNull Consumer<JMenu> binder) {
        return extraMenuBinders != null && extraMenuBinders.remove(binder);
    }


    public XChartPanel<T> setShowPopupMenuOnMouseTrigger(boolean showPopupMenuOnMouseTrigger) {
        this.showPopupMenuOnMouseTrigger = showPopupMenuOnMouseTrigger;
        return this;
    }

    public boolean isShowPopupMenuOnMouseTriggerEnabled() {
        return showPopupMenuOnMouseTrigger;
    }

    @NotNull
    public JMenu createMenu(@Nullable Action action) {
        return new OpsMenu(action);
    }

    @NotNull
    public JPopupMenu createPopupMenu(@Nullable Action action) {
        return createMenu(action).getPopupMenu();
    }

    public void showPopupMenu(@Nullable Action action, Component component, int x, int y) {
        final JPopupMenu menu = createPopupMenu(action);
        menu.show(component, x, y);
        menu.getGraphics().dispose();
    }

    public void showPopupMenu(@Nullable Action action, @NotNull MouseEvent e) {
        showPopupMenu(action, e.getComponent(), e.getX(), e.getY());
    }

    public void showPopupMenu(@Nullable Action action) {
        showPopupMenu(action, this, getWidth() / 2,getHeight() / 2);
    }

    public void showPopupMenu() {
        showPopupMenu(null);
    }

    public void showPrintDialog() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        if (printJob.printDialog()) {
            try {
                // Page format
                PageFormat pageFormat = printJob.defaultPage();
                Paper paper = pageFormat.getPaper();
                if (this.getWidth() > this.getHeight()) {
                    pageFormat.setOrientation(PageFormat.LANDSCAPE);
                    paper.setImageableArea(0, 0, pageFormat.getHeight(), pageFormat.getWidth());
                } else {
                    paper.setImageableArea(0, 0, pageFormat.getWidth(), pageFormat.getHeight());
                }
                pageFormat.setPaper(paper);
                pageFormat = printJob.validatePage(pageFormat);

                String jobName = "Chart (" + chart.getTitle().trim() + ")";
                printJob.setJobName(jobName);

                printJob.setPrintable(new Printer(this), pageFormat);
                printJob.print();
            } catch (Throwable e) {
                Log.e(TAG, "failed to print " + chart.getTitle(), e);
            }
        }
    }



    /**
     * Saves the chart to a file, with format specified by file extension
     * Defaults to png
     *
     * @param file file to save
     *
     * @throws IOException if an IO error occurs
     * */
    public void saveToFile(@NotNull File file) throws IOException {
        final String path = file.getCanonicalPath();
        final String ext = FileUtil.getExtFromPath(path, false).toLowerCase();

        switch (ext) {
            case "jpg" -> BitmapEncoder.saveJPGWithQuality(chart, path, 1.0f);
            case "bmp" -> BitmapEncoder.saveBitmap(chart, path, BitmapFormat.BMP);
            case "gif" -> BitmapEncoder.saveBitmap(chart, path, BitmapFormat.GIF);
            case "svg" -> VectorGraphicsEncoder.saveVectorGraphic(chart, path, VectorGraphicsFormat.SVG);
            case "eps" -> VectorGraphicsEncoder.saveVectorGraphic(chart, path, VectorGraphicsFormat.EPS);
            case "pdf" -> VectorGraphicsEncoder.saveVectorGraphic(chart, path, VectorGraphicsFormat.PDF);
            default -> BitmapEncoder.saveBitmap(chart, path, BitmapFormat.PNG);
        }
    }

    public void showSaveAsDialog() {
        R.ensureExportsDir();

        final OpenFileFilter png = new OpenFileFilter(".png");
        final List<FileFilter> fileFilters = new LinkedList<>();
        fileFilters.add(png);
        fileFilters.add(new OpenFileFilter(".jpg"));
        fileFilters.add(new OpenFileFilter(".bmp"));
        fileFilters.add(new OpenFileFilter(".gif"));

        // VectorGraphics2D is optional, so if it's on the classpath, allow saving charts as vector
        // graphic
        try {
            Class.forName("de.erichseifert.vectorgraphics2d.VectorGraphics2D");
            // it exists on the classpath
            fileFilters.add(new OpenFileFilter(".svg"));
            fileFilters.add(new OpenFileFilter(".eps"));
        } catch (ClassNotFoundException e) {
            // it does not exist on the classpath
        }

        try {
            Class.forName("de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D");
            // it exists on the classpath
            fileFilters.add(new OpenFileFilter(".pdf"));
        } catch (ClassNotFoundException e) {
            // it does not exist on the classpath
        }

        final ChooserConfig config = ChooserConfig.saveFile(false)
                .setDialogTitle("Save Chart As")
                .setStartDir(R.DIR_EXPORTS)
                .setChoosableFileFilters(fileFilters)
                .setFIleFilter(png)
                .setUseAcceptAllFIleFilter(false)
                .setApproveButtonText("Save")
                .setApproveButtonTooltipText("Save Chart")
                .build();

        final File[] files = config.showFIleChooser(this);
        if (files == null || files.length == 0 || files[0] == null)
            return;

        try {
            saveToFile(files[0]);
        } catch (Throwable e) {
            Log.e(TAG, "Failed to save chart " + chart.getTitle(), e);
        }



//        UIManager.put("FileChooser.saveButtonText", "Save");
//        UIManager.put("FileChooser.fileNameLabelText", "File Name:");
//
//        JFileChooser fileChooser = new JFileChooser();
//        FileFilter pngFileFilter = new SuffixSaveFilter("png"); // default
//        fileChooser.addChoosableFileFilter(pngFileFilter);
//        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("jpg"));
//        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("bmp"));
//        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("gif"));
//
//        // VectorGraphics2D is optional, so if it's on the classpath, allow saving charts as vector
//        // graphic
//        try {
//            Class.forName("de.erichseifert.vectorgraphics2d.VectorGraphics2D");
//            // it exists on the classpath
//            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("svg"));
//            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("eps"));
//        } catch (ClassNotFoundException e) {
//            // it does not exist on the classpath
//        }
//        try {
//            Class.forName("de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D");
//            // it exists on the classpath
//            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("pdf"));
//        } catch (ClassNotFoundException e) {
//            // it does not exist on the classpath
//        }
//
//        fileChooser.setAcceptAllFileFilterUsed(false);
//        fileChooser.setFileFilter(pngFileFilter);
//
//        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            if (fileChooser.getSelectedFile() != null) {
//                try {
//                    saveToFile(fileChooser.getSelectedFile());
//                } catch (IOException exc) {
//                    exc.printStackTrace();
//                }
//            }
//        }
    }



    public void exportXYChartDataToDir(@NotNull File dir) throws IOException, IllegalStateException {
        if (!(chart instanceof XYChart))
            throw new IllegalStateException("Cannot export data of " + chart.getClass().getSimpleName() + ". Only works for " + XYChart.class.getSimpleName());
        CSVExporter.writeCSVColumns((XYChart) chart, dir.getCanonicalPath() + File.separatorChar);
    }

    public void showExportToDialog() {
        if (!(chart instanceof XYChart))
            return;

        R.ensureExportsDir();

        final String dialogTitle = "Export Chart Data";
        final ChooserConfig config = ChooserConfig.openDirSingle()
                .setDialogTitle(dialogTitle)
                .setStartDir(R.DIR_EXPORTS)
                .setUseAcceptAllFIleFilter(false)
                .setApproveButtonText("Export")
                .setApproveButtonTooltipText(dialogTitle)
                .build();

        final File[] files = config.showFIleChooser(this);
        if (files == null || files.length == 0 || files[0] == null)
            return;

        try {
            exportXYChartDataToDir(files[0]);
        } catch (Throwable e) {
            Log.e(TAG, "failed to export chart data", e);
        }

//        UIManager.put("FileChooser.saveButtonText", "Export");
//        UIManager.put("FileChooser.fileNameLabelText", "Export To:");
//        UIManager.put("FileChooser.fileNameLabelMnemonic", "Export To:");
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
//        disableLabel(fileChooser.getComponents());
//        disableTextField(fileChooser.getComponents());
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        fileChooser.setFileFilter(
//                new FileFilter() {
//
//                    @Override
//                    public boolean accept(File f) {
//
//                        return f.isDirectory();
//                    }
//
//                    @Override
//                    public String getDescription() {
//
//                        return "Any Directory";
//                    }
//                });
//        fileChooser.setAcceptAllFileFilterUsed(false);
//        fileChooser.setDialogTitle("Export");
//
//        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//
//            File theFileToSave = null;
//            if (fileChooser.getSelectedFile() != null) {
//                if (fileChooser.getSelectedFile().exists()) {
//                    theFileToSave = fileChooser.getSelectedFile();
//                } else {
//                    theFileToSave = new File(fileChooser.getSelectedFile().getParent());
//                }
//            }
//
//            try {
//                CSVExporter.writeCSVColumns(
//                        (XYChart) chart, theFileToSave.getCanonicalPath() + File.separatorChar);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }


//    private void disableTextField(Component[] comp) {
//        for (Component component : comp) {
//            //            System.out.println(component.toString());
//            if (component instanceof JPanel) {
//                disableTextField(((JPanel) component).getComponents());
//            } else if (component instanceof JTextField) {
//                component.setVisible(false);
//                return;
//            }
//        }
//    }
//
//    private void disableLabel(Component[] comp) {
//        for (Component component : comp) {
//            //      System.out.println(comp[x].toString());
//            if (component instanceof JPanel) {
//                disableLabel(((JPanel) component).getComponents());
//            } else if (component instanceof JLabel) {
//                //        System.out.println(comp[x].toString());
//                component.setVisible(false);
//                return;
//            }
//        }
//    }

    private class SaveAction extends BaseAction {

        SaveAction() {
            setName("Save");
            setShortDescription("Save Chart");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showSaveAsDialog();
        }
    }

    private class ExportAction extends BaseAction {

        ExportAction() {
            super();
            setName("Export");
            setShortDescription("Export Chart Data");
            setEnabled(chart instanceof XYChart);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showExportToDialog();
        }
    }

    private class PrintAction extends BaseAction {

        PrintAction() {
            super();
            setName("Print");
            setShortDescription("Print Chart");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showPrintDialog();
        }
    }

//    /**
//     * File filter based on the suffix of a file. This file filter accepts all files that end with
//     * .suffix or the capitalized suffix.
//     *
//     * @author Benedikt Bünz
//     */
//    private static class SuffixSaveFilter extends FileFilter {
//
//        private final String suffix;
//
//        /**
//         * @param suffix This file filter accepts all files that end with .suffix or the capitalized
//         *               suffix.
//         */
//        public SuffixSaveFilter(String suffix) {
//
//            this.suffix = suffix;
//        }
//
//        @Override
//        public boolean accept(File f) {
//
//            if (f.isDirectory()) {
//                return true;
//            }
//
//            String s = f.getName();
//
//            return s.endsWith("." + suffix) || s.endsWith("." + suffix.toUpperCase());
//        }
//
//        @Override
//        public String getDescription() {
//
//            return "*." + suffix + ",*." + suffix.toUpperCase();
//        }
//    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (showPopupMenuOnMouseTrigger && e.isPopupTrigger()) {
                showPopupMenu(null, e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (showPopupMenuOnMouseTrigger && e.isPopupTrigger()) {
                showPopupMenu(null, e);
            }
        }
    }


    private class OpsMenu extends JMenu {

        public OpsMenu(@Nullable Action action) {
            super(action);
            final List<Consumer<JMenu>> binders = extraMenuBinders;
            if (CollectionUtil.notEmpty(binders)) {
                binders.forEach(binder -> {
                    final int prev = getMenuComponentCount();
                    binder.consume(OpsMenu.this);
                    if (getMenuComponentCount() > prev) {
                        addSeparator();
                    }
                });
            }

            add(new JMenuItem(printAction));
            add(new JMenuItem(saveAction));
            if (chart instanceof XYChart) {
                add(new JMenuItem(exportAction));
            }
        }


    }


    private static class Printer implements Printable {

        private final Component component;

        Printer(Component c) {
            component = c;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double sx = pageFormat.getImageableWidth() / component.getWidth();
            double sy = pageFormat.getImageableHeight() / component.getHeight();
            g2.scale(sx, sy);

            component.printAll(g2);

            return PAGE_EXISTS;
        }
    }
}
