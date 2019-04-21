package editor;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import jdbc.entities.StatisticsViewEntity;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import sun.awt.X11.XToolkit;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LabelRenderer extends HintRenderer {
    private final int lineStartOffset;
    private XYSeries xySeries;

    public LabelRenderer(@Nullable String text, Pair<StatisticsViewEntity, List<Integer>> methodData) {
        super(text);
        this.lineStartOffset = methodData.getFirst().getStartOffset();
        this.xySeries = new XYSeries("");
        final AtomicInteger index = new AtomicInteger(1);
        methodData.getSecond().forEach(val -> xySeries.add(index.getAndIncrement(), val));
    }

    @Override
    public int calcWidthInPixels(Inlay inlay) {
        return 100000;
//            Editor editor = inlay.getEditor();
//            FontMetrics fontMetrics = getFontMetrics(editor).getMetrics();
//            return doCalcWidth(super.getText(), fontMetrics) + calcWidthAdjustment(editor, fontMetrics);
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return 40;
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        final Editor editor = inlay.getEditor();
        if (!(editor instanceof EditorImpl))
            return;
        final EditorImpl impl = (EditorImpl) editor;
        final int ascent = impl.getAscent();
        final int descent = impl.getDescent();
        final Graphics2D g2d = (Graphics2D) g;
        final TextAttributes attributes = getTextAttributes(editor);

        final XYSeriesCollection data = new XYSeriesCollection(xySeries);
        final JFreeChart chart = ChartFactory.createHistogram(
                null,
                null,
                null,
                data,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
final XYPlot xyPlot = chart.getXYPlot();
        chart.getXYPlot().setBackgroundPaint(new Color(255, 255, 255));
        chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(0, 0, 255));
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(150, 45));
        chartPanel.getScreenDataArea();
        xyPlot.getDomainAxis().setAxisLineVisible(false);
        xyPlot.getDomainAxis().setTickMarksVisible(false);
        xyPlot.getRangeAxis().setAxisLineVisible(false);
        xyPlot.getRangeAxis().setTickMarksVisible(false);
        xyPlot.getRangeAxis().setVisible(false);
        xyPlot.getDomainAxis().setVisible(false);

        XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
        renderer.setDrawBarOutline(false);
        // flat bars look best...
        renderer.setBarPainter(new StandardXYBarPainter());


        final BufferedImage bufferedImage = chart.createBufferedImage(150, 45 );


        if (super.getText() != null && attributes != null) {
            MyFontMetrics fontMetrics = getFontMetrics(editor);
            final int gap = r.height < fontMetrics.getLineHeight() + 2 ? 1 : 2;
            final Color foregroundColor = attributes.getForegroundColor();
            if (foregroundColor != null) {
                final Object savedHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                final Shape savedClip = g.getClip();

                g.setColor(foregroundColor);
                g.setFont(getFont(editor));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(false));
                g2d.setClip(r.x, r.y, 10000, 100);
                final FontMetrics metrics = fontMetrics.getMetrics();
                final int startX = r.x + 7 + fontMetrics.getMetrics().stringWidth(String.format("%"+ lineStartOffset +"s", ""));
                final int startY = r.y + Math.max(ascent, (r.height + metrics.getAscent() - metrics.getDescent()) / 2) - 1;

                final int widthAdjustment = calcWidthAdjustment(editor, g.getFontMetrics());
                if (widthAdjustment == 0) {
                    g.drawString(super.getText(), startX + 3, startY);
                    g2d.drawImage(bufferedImage, null, startX + 620, startY - 35);
                } else {
                    final int adjustmentPosition = this.getWidthAdjustment().getAdjustmentPosition();
                    final String firstPart = this.getText().substring(0, adjustmentPosition);
                    final String secondPart = this.getText().substring(adjustmentPosition);
                    g.drawString(firstPart, startX, startY);
                    g.drawString(secondPart, startX + g.getFontMetrics().stringWidth(firstPart) + widthAdjustment, startY);
                }

                g.setClip(savedClip);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedHint);
            }
        }
    }

    private int calcWidthAdjustment(Editor editor, FontMetrics fontMetrics) {
        if (super.getWidthAdjustment() == null || !(editor instanceof EditorImpl))
            return 0;
        final int editorTextWidth = ((EditorImpl) editor).getFontMetrics(Font.PLAIN)
                .stringWidth(super.getWidthAdjustment().getEditorTextToMatch());
        return Math.max(0, editorTextWidth + doCalcWidth(super.getWidthAdjustment().getHintTextToMatch(), fontMetrics)
                - doCalcWidth(super.getText(), fontMetrics));
    }

    private Font getFont(@NotNull Editor editor) {
        return editor.getColorsScheme().getFont(EditorFontType.PLAIN);
    }
}