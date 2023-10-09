package org.mmarini.hilbert.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static java.lang.String.format;

/**
 * Paints a pie chart
 */
public class PieChart extends JComponent {
    private static final int TEXT_GAP = 2;
    private static final int DEFAULT_RADIUS = 50;
    private static final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
    private static final Font DEFAULT_TITLE_FONT = new Font(Font.DIALOG, Font.BOLD, 16);
    private static final Insets CHART_INSETS = new Insets(40, 5, 5, 5);
    private static final Logger logger = LoggerFactory.getLogger(PieChart.class);
    private static final Color[] COLOR_MAP = {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA
    };
    private static final int LEGEND_GAP = 10;

    /**
     * Returns the normalized data
     *
     * @param data data
     */
    private static DoubleVector normalize(DoubleVector data) {
        double sum = abs(data.stream().sum());
        return sum != 0
                ? DoubleVector.from(data.stream().map(x -> x / sum))
                : data;
    }

    private Color[] colorMap;
    private Font titleFont;
    private DoubleVector data;
    private List<String> legend;
    private String title;
    private DoubleVector k;
    private Rectangle innerFrame;
    private int radius;
    private Rectangle chartFrame;
    private String legendPattern;
    private List<String> legendText;

    /**
     * Creates a pie chart component
     */
    public PieChart() {
        this.colorMap = COLOR_MAP;
        this.legendPattern = "%s (%s)";
        this.titleFont = DEFAULT_TITLE_FONT;
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setBorder(BorderFactory.createEtchedBorder());
        setFont(DEFAULT_FONT);
        logger.atDebug().log("Created");
    }

    /**
     * Computes prefered size
     */
    private void computePreferedSize() {
        if (data != null) {
            Font font = getFont();
            FontMetrics fm = getFontMetrics(font);
            int legendWidth = IntStream.range(0, data.size())
                    .mapToObj(i -> format(legendPattern,
                            Optional.ofNullable(legend)
                                    .filter(l -> i < l.size())
                                    .map(l -> l.get(i))
                                    .orElse(""),
                            LineChart.formatNumber(data.get(i))))
                    .mapToInt(fm::stringWidth)
                    .max()
                    .orElse(0);
            int hGap = legendWidth + LEGEND_GAP * 3 + TEXT_GAP;
            int width = (hGap + DEFAULT_RADIUS) * 2 + CHART_INSETS.left + CHART_INSETS.right;
            int heigth = DEFAULT_RADIUS * 2 + CHART_INSETS.top + CHART_INSETS.bottom;
            setPreferredSize(new Dimension(width, heigth));
        }
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
     * Returns the data
     */
    public DoubleVector getData() {
        return data;
    }

    /**
     * Sets the data
     *
     * @param data the data
     */
    public void setData(DoubleVector data) {
        this.data = data;
        computePreferedSize();
        repaint();
    }

    /**
     * Sets the data
     *
     * @param data the data
     */
    public void setData(double... data) {
        setData(DoubleVector.of(data));
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
        computePreferedSize();
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
     * Returns the legend pattern
     */
    public String getLegendPattern() {
        return legendPattern;
    }

    /**
     * Sets the legend patterns
     *
     * @param legendPattern the legend pattern
     */
    public void setLegendPattern(String legendPattern) {
        this.legendPattern = legendPattern;
        computePreferedSize();
        repaint();
    }

    /**
     * Returns the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title
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
     * @param titleFont the title cont
     */
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
        repaint();
    }

    /**
     * Initializes the paint properties
     */
    private void initProps() {
        this.legendText = IntStream.range(0, data.size())
                .mapToObj(i -> format(legendPattern,
                        Optional.ofNullable(legend)
                                .filter(l -> i < l.size())
                                .map(l -> l.get(i))
                                .orElse(""),
                        LineChart.formatNumber(data.get(i))))
                .collect(Collectors.toList());
        FontMetrics fm = getFontMetrics(getFont());
        int legendWidth = legendText.stream()
                .mapToInt(fm::stringWidth)
                .max()
                .orElse(0);
        this.k = normalize(data);
        int hGap = legendWidth + LEGEND_GAP * 3 + TEXT_GAP;
        this.radius = min(innerFrame.width - hGap * 2 - CHART_INSETS.left - CHART_INSETS.right,
                innerFrame.height - CHART_INSETS.top - CHART_INSETS.bottom) / 2;
        int x0 = (int) max(innerFrame.getCenterX() - radius, innerFrame.getX() + CHART_INSETS.left);
        int y0 = (int) max(innerFrame.getCenterY() - radius, innerFrame.getY() + CHART_INSETS.top);
        this.chartFrame = new Rectangle(x0, y0, radius * 2, radius * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        Dimension size = getSize();
        Insets insets = getInsets();
        this.innerFrame = new Rectangle(insets.left, insets.top,
                size.width - insets.left - insets.right,
                size.height - insets.top - insets.bottom);
        gr.setColor(getBackground());
        gr.fill(innerFrame);
        if (data != null && data.size() > 0) {
            initProps();
            paintData(gr);
            paintLegend(gr);
            if (title != null) {
                paintTitle(gr);
            }
        }
    }

    /**
     * Paints the pie
     *
     * @param gr the graphics context
     */
    private void paintData(Graphics2D gr) {
        Arc2D arc = new Arc2D.Double(chartFrame.x, chartFrame.y, chartFrame.width, chartFrame.height,
                0, 360, Arc2D.PIE);
        double start = 90;
        for (int i = 0; i < k.size(); i++) {
            double extent = -k.get(i) * 360;
            arc.setAngleStart(start);
            arc.setAngleExtent(extent);
            gr.setColor(colorMap[i % colorMap.length]);
            gr.fill(arc);
            gr.setColor(getForeground());
            gr.draw(arc);
            start += extent;
        }
    }

    /**
     * Paints the values
     *
     * @param gr the graphics context
     */
    private void paintLegend(Graphics2D gr) {
        FontMetrics fm = getFontMetrics(getFont());
        int height = fm.getHeight();

        // Builds data properties [value, start, ratio, sin, cos]
        double acc = 0;
        Stream.Builder<double[]> builder = Stream.builder();
        for (int i = 0; i < data.size(); i++) {
            double value = data.get(i);
            double ratio = k.get(i);
            double start = acc;
            // angle = pi*2*(start+ratio/2)
            // angle = pi*(2*start+ratio)
            double angle = PI * (2 * start + ratio);
            double sinA = sin(angle);
            double cosA = cos(angle);
            builder.add(new double[]{value, start, ratio, sinA, cosA});
            acc += k.get(i);
        }
        List<double[]> dataProps = builder.build().collect(Collectors.toList());

        // Builds string anchor Q1 [0, 0.25)
        List<Point> q1 = new ArrayList<>();
        int accY = -radius;
        for (double[] props : dataProps) {
            double pivot = props[1] + props[2] / 2;
            if (pivot >= 0.25) {
                break;
            }
            int ay = (int) round(-radius * props[4]);
            accY = max(ay, accY);
            q1.add(new Point(radius, accY));
            accY += height;
        }

        // Builds string anchor Q3 [0.5, 0.75)
        List<Point> q3 = new ArrayList<>();
        accY = radius;
        for (double[] props : dataProps) {
            double pivot = props[1] + props[2] / 2;
            if (pivot >= 0.75) {
                break;
            }
            if (pivot >= 0.5) {
                int ay = (int) round(-radius * props[4]);
                accY = min(ay, accY);
                q3.add(new Point(-radius, accY));
                accY -= height;
            }
        }

        // Builds string anchor Q4 [0.75, 1]
        List<Point> q4 = new ArrayList<>();
        accY = -radius;
        for (int i = dataProps.size() - 1; i >= 0; i--) {
            double[] props = dataProps.get(i);
            double pivot = props[1] + props[2] / 2;
            if (pivot < 0.75) {
                break;
            }
            int ay = (int) round(-radius * props[4]);
            accY = max(ay, accY);
            q4.add(new Point(-radius, accY));
            accY += height;
        }
        Collections.reverse(q4);

        // Builds string anchor Q2 [0.25, 0.5)
        List<Point> q2 = new ArrayList<>();
        accY = radius;
        for (int i = dataProps.size() - 1; i >= 0; i--) {
            double[] props = dataProps.get(i);
            double pivot = props[1] + props[2] / 2;
            if (pivot < 0.25) {
                break;
            }
            if (pivot < 0.5) {
                int ay = (int) round(-radius * props[4]);
                accY = min(ay, accY);
                q2.add(new Point(radius, accY));
                accY -= height;
            }
        }
        Collections.reverse(q2);

        // Collects anchors
        List<Point> anchors = Stream.of(q1, q2, q3, q4)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // paints values
        gr.setColor(getForeground());
        int xc = (int) chartFrame.getCenterX();
        int yc = (int) chartFrame.getCenterY();
        for (int i = 0; i < dataProps.size(); i++) {
            double[] props = dataProps.get(i);
            Point textAnchor = anchors.get(i);
            Point pieAnchor = new Point(
                    (int) round(radius * props[3]),
                    (int) round(-radius * props[4]));

            // Paint junction
            int xp = pieAnchor.x + xc;
            int yp = pieAnchor.y + yc;
            int xa1 = textAnchor.x > 0
                    ? xc + radius + LEGEND_GAP
                    : xc - radius - LEGEND_GAP;
            int xa2 = textAnchor.x > 0
                    ? xc + radius + 2 * LEGEND_GAP
                    : xc - radius - 2 * LEGEND_GAP;
            int xa3 = textAnchor.x > 0
                    ? xc + radius + 3 * LEGEND_GAP
                    : xc - radius - 3 * LEGEND_GAP;
            int ys = textAnchor.y + yc;
            gr.drawLine(xp, yp, xa1, yp);
            gr.drawLine(xa1, yp, xa2, ys);
            gr.drawLine(xa2, ys, xa3, ys);
            String text = legendText.get(i);
            int width = fm.stringWidth(text);
            int xs = textAnchor.x > 0
                    ? textAnchor.x + xc + 3 * LEGEND_GAP + TEXT_GAP
                    : textAnchor.x + xc - 3 * LEGEND_GAP - TEXT_GAP - width;
            gr.drawString(text, xs, ys + fm.getAscent() / 2);
        }
    }

    /**
     * Paints the title
     *
     * @param gr the graphics context
     */
    private void paintTitle(Graphics2D gr) {
        Font font = getFont().deriveFont(12f).deriveFont(Font.BOLD);
        FontMetrics fm = getFontMetrics(font);
        int width = fm.stringWidth(title);
        int x = (int) chartFrame.getCenterX() - width / 2;
        int y = innerFrame.y + fm.getHeight();
        gr.setFont(font);
        gr.setColor(getForeground());
        gr.drawString(title, x, y);
    }
}
