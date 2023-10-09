package org.mmarini.hilbert.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.*;

/**
 * Paints line chart
 */
public class LineChart extends JComponent {
    private static final int LEGEND_GAP = 5;
    private static final int DASH_LENGTH = 4;
    private static final int MINOR_TICK_NUMBER = 5;
    private static final int LEGEND_LINE_WIDTH = 10;
    private static final int TICK_LENGTH = 5;
    private static final Insets CHART_INSETS = new Insets(40, 60, 40, 20);
    private static final Dimension DEFAULT_PREFERED_SIZE = new Dimension(
            200 + CHART_INSETS.left + CHART_INSETS.right,
            100 + CHART_INSETS.top + CHART_INSETS.bottom);
    private static final Color DEFAULT_GRID_COLOR = new Color(0xd0d0d0);
    private static final BasicStroke STROKE0 = new BasicStroke(0);
    private static final BasicStroke THICK_STROKE = new BasicStroke(2);
    private static final BasicStroke DOTS_STROKE = new BasicStroke(0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10,
            new float[]{DASH_LENGTH, DASH_LENGTH}, 0);
    private static final Logger logger = LoggerFactory.getLogger(LineChart.class);
    private static final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.BOLD, 10);
    private static final Font DEFAULT_TITLE_FONT = new Font(Font.DIALOG, Font.BOLD, 14);
    private static final Color[] COLOR_MAP = {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA
    };
    private static final DecimalFormat expFormat = new DecimalFormat("0.######E0");
    private static final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();

    /**
     * Returns the text of a number
     *
     * @param value the value
     */
    public static String formatNumber(double value) {
        double a = abs(value);
        if (a == 0 || a >= 1e-3 && a <= 1e6) {
            return decimalFormat.format(value);
        } else {
            return expFormat.format(value);
        }
    }

    /**
     * Returns the axis configuration [min, max, major tick, minor tick]
     *
     * @param series the series
     */
    static AxixConfig getAxisConfigs(List<DoubleVector> series) {
        double min = series.stream().flatMapToDouble(DoubleVector::stream).min().orElse(0);
        double max = series.stream().flatMapToDouble(DoubleVector::stream).max().orElse(0);
        if (min == max && abs(min) == 0) {
            return new AxixConfig(-1, 1, 1d / MINOR_TICK_NUMBER, 1);
        }
        double range = max - min;
        if (range == 0) {
            range = min * 1e-4;
        }
        double exp = floor(log10(range));
        double majorTicks = pow(10, exp);
        double minorTicks = majorTicks / MINOR_TICK_NUMBER;
        min = (floor(min / minorTicks) - 1) * minorTicks;
        max = (ceil(max / minorTicks) + 1) * minorTicks;
        return new AxixConfig(min, max, minorTicks, majorTicks);
    }

    private Color[] colorMap;
    private Font titleFont;
    private boolean gridOn;
    private List<String> legend;
    private DoubleVector xData;
    private List<DoubleVector> yData;
    private Color gridColor;
    private DoubleVector x;
    private AxixConfig xConfig;
    private AxixConfig yConfig;
    private Rectangle2D chartBound;
    private Rectangle2D chartCoordBound;
    private AffineTransform chartTransform;
    private String title;
    private String xLabel;
    private String yLabel;

    /**
     * Creates the line chart
     */
    public LineChart() {
        this.gridColor = DEFAULT_GRID_COLOR;
        this.gridOn = true;
        this.titleFont = DEFAULT_TITLE_FONT;
        this.colorMap = COLOR_MAP;
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder());
        setFont(DEFAULT_FONT);
        setPreferredSize(DEFAULT_PREFERED_SIZE);
        logger.atDebug().log("Created");
    }

    /**
     * Computes the prefered size
     */
    void computePreferedSize() {
    }

    /**
     * Returns the x data
     */
    private DoubleVector createXData() {
        if (xData == null) {
            int n = yData.stream()
                    .mapToInt(DoubleVector::size)
                    .max()
                    .orElse(0);
            return DoubleVector.from(IntStream.range(1, n + 1).mapToDouble(x -> x));
        } else {
            return xData;
        }
    }

    /**
     * Returns the chart bounds (pixels)
     */
    private Rectangle getChartBounds() {
        Rectangle box = (Rectangle) getInnerBound().clone();
        box.translate(CHART_INSETS.left, CHART_INSETS.top);
        box.setSize(box.width - CHART_INSETS.left - CHART_INSETS.right,
                box.height - CHART_INSETS.top - CHART_INSETS.bottom);
        return box;
    }

    /**
     * Returns the chart bounds (chart coordinates)
     */
    private Rectangle2D getChartCoordBound() {
        return new Rectangle2D.Double(xConfig.min, yConfig.min,
                xConfig.max - xConfig.min, yConfig.max - yConfig.min);
    }

    /**
     * Returns the transformation for a give series
     */
    private AffineTransform getChartTransform() {
        double xs = chartBound.getWidth() / chartCoordBound.getWidth();
        double ys = chartBound.getHeight() / chartCoordBound.getHeight();
        AffineTransform tr0 = AffineTransform.getTranslateInstance(chartBound.getX(), chartBound.getMaxY());
        tr0.scale(xs, -ys);
        tr0.translate(-chartCoordBound.getX(), -chartCoordBound.getY());
        return tr0;
    }

    /**
     * Returns the color map
     */
    public Color[] getColorMap() {
        return colorMap;
    }

    /**
     * Sets the color map
     *
     * @param colorMap the color map
     */
    public void setColorMap(Color[] colorMap) {
        this.colorMap = colorMap;
        repaint();
    }

    /**
     * Returns the grid color
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets the grid color
     *
     * @param gridColor the grid color
     */
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
        repaint();
    }

    /**
     * Returns the inner size (without insets)
     */
    private Rectangle getInnerBound() {
        Dimension size = getSize();
        Insets insets = getInsets();
        int width = size.width - insets.left - insets.right;
        int height = size.height - insets.top - insets.bottom;
        return new Rectangle(insets.left, insets.top, width, height);
    }

    /**
     * Returns the legend
     */
    public List<String> getLegend() {
        return legend;
    }

    /**
     * Sets the legend
     *
     * @param legend the legend
     */
    public void setLegend(List<String> legend) {
        this.legend = legend;
        repaint();
    }

    /**
     * Sets the legend
     *
     * @param legend the legend
     */
    public void setLegend(String... legend) {
        setLegend(List.of(legend));
    }

    /**
     * Returns the title of chart
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of chart
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    /**
     * Returns the title font
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * Sets the title font
     *
     * @param titleFont the title font
     */
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
        repaint();
    }

    /**
     * Returns the x data
     */
    public DoubleVector getxData() {
        return xData;
    }

    /**
     * Sets the x data
     *
     * @param xData the x data
     */
    public void setxData(double... xData) {
        setxData(DoubleVector.of(xData));
    }

    public void setxData(DoubleVector xData) {
        this.xData = xData;
        repaint();
    }

    /**
     * Returns the x label
     */
    public String getxLabel() {
        return xLabel;
    }

    /**
     * Sets the x label
     *
     * @param xLabel the label
     */
    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
        repaint();
    }

    /**
     * Returns the y data
     */
    public List<DoubleVector> getyData() {
        return yData;
    }

    /**
     * Sets the y data
     *
     * @param yData the y data
     */
    public void setyData(List<DoubleVector> yData) {
        this.yData = yData;
        repaint();
        computePreferedSize();
    }

    /**
     * Sets the y data
     *
     * @param yData the y data
     */
    public void setyData(DoubleVector... yData) {
        setyData(List.of(yData));
    }

    /**
     * Returns the y label
     */
    public String getyLabel() {
        return yLabel;
    }

    /**
     * Sets the y label
     *
     * @param yLabel the label
     */
    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
        repaint();
    }

    /**
     * Returns true if grid is shown
     */
    public boolean isGridOn() {
        return gridOn;
    }

    /**
     * Sets the grid on
     *
     * @param gridOn true if grid is shown
     */
    public void setGridOn(boolean gridOn) {
        this.gridOn = gridOn;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle box = getInnerBound();
        Graphics2D gr = (Graphics2D) g;
        gr.setColor(getBackground());
        gr.fill(box);
        if (yData != null && !yData.isEmpty()) {
            this.x = createXData();
            this.xConfig = getAxisConfigs(List.of(x));
            this.yConfig = getAxisConfigs(yData);
            this.chartBound = getChartBounds();
            this.chartCoordBound = getChartCoordBound();
            this.chartTransform = getChartTransform();
            if (gridOn) {
                paintGrid(gr);
            }
            paintTicks(gr);
            if (title != null) {
                paintTitle(gr);
            }
            if (xLabel != null) {
                paintXLabel(gr);
            }
            if (yLabel != null) {
                paintYLabel(gr);
            }
            paintData(gr);
            if (legend != null) {
                paintLegend(gr);
            }
        }
    }

    /**
     * Paints the data
     *
     * @param gr the graphics
     */
    protected void paintData(Graphics2D gr) {
        gr.setStroke(THICK_STROKE);
        Point2D p0 = new Point2D.Double();
        Point2D p1 = new Point2D.Double();
        for (int i = 0; i < yData.size(); i++) {
            DoubleVector y = yData.get(i);
            gr.setColor(colorMap[i % colorMap.length]);
            int n = min(x.size(), y.size());
            for (int j = 0; j < n - 1; j++) {
                p0.setLocation(x.get(j), y.get(j));
                chartTransform.transform(p0, p0);
                p1.setLocation(x.get(j + 1), y.get(j + 1));
                chartTransform.transform(p1, p1);
                gr.drawLine((int) p0.getX(), (int) p0.getY(),
                        (int) p1.getX(), (int) p1.getY());
            }
        }
    }

    /**
     * Paints the grid
     *
     * @param gr the graphics
     */
    private void paintGrid(Graphics2D gr) {
        double x0 = floor(xConfig.min / xConfig.minorTick) * xConfig.minorTick;
        double y0 = floor(yConfig.min / yConfig.minorTick) * yConfig.minorTick;

        gr.setColor(getGridColor());
        int phase = (int) floor((xConfig.min - x0) / xConfig.minorTick) % MINOR_TICK_NUMBER;
        Point2D p0 = new Point2D.Double();
        Point2D p1 = new Point2D.Double();
        for (double xx = xConfig.min; xx <= xConfig.max; xx += xConfig.minorTick) {
            p0.setLocation(xx, yConfig.min);
            chartTransform.transform(p0, p0);
            p1.setLocation(xx, yConfig.max);
            chartTransform.transform(p1, p1);
            int xp0 = (int) p0.getX();
            int yp0 = (int) p0.getY();
            int xp1 = (int) p1.getX();
            int yp1 = (int) p1.getY();
            if (phase == 0) {
                gr.setStroke(STROKE0);
                yp0 += TICK_LENGTH;
            } else {
                gr.setStroke(DOTS_STROKE);
            }
            gr.drawLine(xp0, yp0, xp1, yp1);
            phase = (phase + 1) % MINOR_TICK_NUMBER;
        }

        phase = (int) floor((yConfig.min - y0) / yConfig.minorTick) % MINOR_TICK_NUMBER;
        for (double yy = yConfig.min; yy <= yConfig.max; yy += yConfig.minorTick) {
            p0.setLocation(xConfig.min, yy);
            chartTransform.transform(p0, p0);
            p1.setLocation(xConfig.max, yy);
            chartTransform.transform(p1, p1);
            int xp0 = (int) p0.getX();
            int yp0 = (int) p0.getY();
            int xp1 = (int) p1.getX();
            int yp1 = (int) p1.getY();
            if (phase == 0) {
                gr.setStroke(STROKE0);
                xp0 -= TICK_LENGTH;
            } else {
                gr.setStroke(DOTS_STROKE);
            }
            gr.drawLine(xp0, yp0, xp1, yp1);
            phase = (phase + 1) % MINOR_TICK_NUMBER;
        }
    }

    /**
     * Paints the legend
     *
     * @param gr the graphics context
     */
    private void paintLegend(Graphics2D gr) {
        FontMetrics fm = getFontMetrics(getFont());
        int textWidth = legend.stream().mapToInt(fm::stringWidth).max().orElse(0);
        int legendWidth = textWidth + LEGEND_GAP * 3 + LEGEND_LINE_WIDTH;
        int height = fm.getHeight();
        int legendHeight = height * legend.size() + LEGEND_GAP * 2;
        Rectangle box = getInnerBound();
        Rectangle legendBox = new Rectangle(box.x + box.width - LEGEND_GAP - legendWidth,
                box.y + LEGEND_GAP,
                legendWidth, legendHeight);
        gr.setColor(getBackground());
        gr.fill(legendBox);
        gr.setColor(getGridColor());
        gr.setStroke(STROKE0);
        gr.draw(legendBox);
        for (int i = 0; i < legend.size(); i++) {
            String text = legend.get(i);
            gr.setColor(colorMap[i % colorMap.length]);
            int x = legendBox.x + LEGEND_GAP;
            int y = legendBox.y + LEGEND_GAP + i * height + 2;
            gr.fillRect(x, y, LEGEND_LINE_WIDTH, height - 4);
            gr.setColor(getForeground());
            gr.drawString(text, legendBox.x + LEGEND_GAP * 2 + LEGEND_LINE_WIDTH, legendBox.y + LEGEND_GAP + (i + 1) * height);
        }
    }

    /**
     * Paints a number
     *
     * @param gr    graphics context
     * @param text  the text
     * @param x     x location (chart coord)
     * @param y     y location (chart coord)
     * @param align the alignment 0=N 2=E 4=S 6=W 8=Center
     * @param gap   the gap between anchor point and edge point
     */
    private void paintText(Graphics2D gr, String text, double x, double y, int align, int gap) {
        Point2D.Double pt = new Point2D.Double(x, y);
        chartTransform.transform(pt, pt);
        int xp = (int) pt.getX();
        int yp = (int) pt.getY();
        int width = getFontMetrics(getFont()).stringWidth(text);

        switch (align) {
            case 1:
            case 2:
            case 3:
                xp += width + gap;
                break;
            case 5:
            case 6:
            case 7:
                xp -= width + gap;
                break;
            default:
                xp -= width / 2;
        }
        switch (align) {
            case 3:
            case 4:
            case 5:
                yp += getFontMetrics(getFont()).getHeight() + gap;
                break;
            case 0:
            case 1:
            case 7:
                yp -= gap;
                break;
            default:
                yp += getFontMetrics(getFont()).getHeight() / 2;
        }
        gr.drawString(text, xp, yp);
    }

    /**
     * Paints the ticks
     *
     * @param gr the graphics
     */
    private void paintTicks(Graphics2D gr) {
        double xMin = ceil(xConfig.min / xConfig.majorTick) * xConfig.majorTick;
        double yMin = ceil(yConfig.min / yConfig.majorTick) * yConfig.majorTick;

        AffineTransform tr = gr.getTransform();
        gr.setColor(getForeground());
        Font font = getFont();
        gr.setFont(font);
        for (double xx = xMin; xx <= xConfig.max; xx += xConfig.majorTick) {
            paintText(gr, formatNumber(xx), xx, yConfig.min, 4, TICK_LENGTH);
        }
        for (double yy = yMin; yy <= yConfig.max; yy += yConfig.majorTick) {
            paintText(gr, formatNumber(yy), xConfig.min, yy, 6, TICK_LENGTH);
        }
        gr.setTransform(tr);
    }

    /**
     * Paints the title
     *
     * @param gr the graphics
     */
    private void paintTitle(Graphics2D gr) {
        FontMetrics fm = getFontMetrics(titleFont);
        int width = fm.stringWidth(title);
        int x = (int) chartBound.getCenterX() - width / 2;
        int y = (int) chartBound.getMinY() - fm.getHeight() - 2;
        Font font = gr.getFont();
        gr.setColor(getForeground());
        gr.setFont(titleFont);
        gr.drawString(title, x, y);
        gr.setFont(font);
    }

    /**
     * Paints the x label
     *
     * @param gr the graphics
     */
    private void paintXLabel(Graphics2D gr) {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth(xLabel);
        int x = (int) chartBound.getCenterX() - width / 2;
        int y = (int) chartBound.getMaxY() + TICK_LENGTH + fm.getHeight() + fm.getHeight() + 2;
        gr.setColor(getForeground());
        gr.drawString(xLabel, x, y);
    }

    /**
     * Paints the y label
     *
     * @param gr the graphics
     */
    private void paintYLabel(Graphics2D gr) {
        FontMetrics fm = getFontMetrics(getFont());
        Rectangle box = getInnerBound();
        int x = box.x + 2;
        int y = (int) chartBound.getMinY() - fm.getHeight();
        gr.setColor(getForeground());
        gr.drawString(yLabel, x, y);
    }

    static class AxixConfig {
        public final double majorTick;
        public final double max;
        public final double min;
        public final double minorTick;

        /**
         * Creates the axix config
         *
         * @param min       the minimum value
         * @param max       the maximum value
         * @param minorTick the minor tick
         * @param majorTick the major tick
         */
        AxixConfig(double min, double max, double minorTick, double majorTick) {
            this.majorTick = majorTick;
            this.max = max;
            this.min = min;
            this.minorTick = minorTick;
        }
    }
}


