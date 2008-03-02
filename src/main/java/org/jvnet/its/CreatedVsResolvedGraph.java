package org.jvnet.its;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.kohsuke.jnt.IssueField;
import org.kohsuke.jnt.IssueStatus;
import org.kohsuke.jnt.JNIssue.Activity;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class CreatedVsResolvedGraph extends Graph<XYDataset> {
    private final TimePeriodFactory timePeriodFactory;

    public CreatedVsResolvedGraph(TimePeriodFactory timePeriodFactory) {
        this.timePeriodFactory = timePeriodFactory;
    }

    protected XYDataset buildDataSet(List<Activity> activities) throws IOException {
        Map<TimePeriod,Integer> created = new TreeMap<TimePeriod,Integer>();
        Map<TimePeriod,Integer> resolved = new TreeMap<TimePeriod,Integer>();

        for (Activity a : activities) {
            if(!a.isUpdate()) {
                inc(a,created);
                continue;
            }

            if(a.getField()!= IssueField.STATUS)
                continue;

            IssueStatus o = IssueStatus.valueOf(a.getOldValue());
            IssueStatus n = IssueStatus.valueOf(a.getNewValue());

            if(o.needsWork && !n.needsWork)
                inc(a,resolved);
            if(!o.needsWork && n.needsWork)
                inc(a,created);
        }

        TimeTableXYDataset ds = new TimeTableXYDataset();
        // add resolved first so that this becomes the foreground
        buildDataSet(resolved, ds, "resolved");
        buildDataSet(created, ds, "created");

        saveDataset(ds);

        return ds;
    }

    private void buildDataSet(Map<TimePeriod,Integer> incoming, TimeTableXYDataset ds, String label) {
        for (Entry<TimePeriod,Integer> e : incoming.entrySet())
            ds.add(e.getKey(),e.getValue(),label,false);
    }

    private void inc(Activity a, Map<TimePeriod,Integer> data) {
        TimePeriod l = timePeriodFactory.toTimePeriod(a.getTimestamp());
        Integer v = data.get(l);
        if(v==null) v=1;
        else        v=v+1;
        data.put(l,v);
    }

    protected String getImageName() {
        return "created-vs-resolved.png";
    }

    protected JFreeChart createChart(XYDataset dataset) {
        DateAxis xAxis = new DateAxis("date");

        NumberAxis yAxis = new NumberAxis("# of issues");
        XYItemRenderer renderer = new XYBarRenderer();
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setForegroundAlpha(0.8F);
        renderer.setSeriesPaint(0,ColorPalette.GREEN);
        renderer.setSeriesPaint(1,ColorPalette.RED);

        XYBarRenderer xybarrenderer = (XYBarRenderer)plot.getRenderer();
        xybarrenderer.setDrawBarOutline(false);

        JFreeChart chart = new JFreeChart("Created vs Resolved", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        //  not working as expected
//        // tick size should be int
//        double tickSize = yAxis.getTickUnit().getSize();
//        int factor = 1;
//        while(true) {
//            if(isInt(tickSize*factor)) {
//                yAxis.setTickUnit(new NumberTickUnit(tickSize*factor));
//                break;
//            }
//            if(factor==10) {
//                yAxis.setTickUnit(new NumberTickUnit(Math.ceil(tickSize)));
//                break;
//            }
//            factor++;
//        }

        return chart;
    }

    private boolean isInt(double v) {
        return Math.floor(v)==v;
    }
}
