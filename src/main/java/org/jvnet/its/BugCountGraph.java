package org.jvnet.its;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.kohsuke.jnt.IssueField;
import org.kohsuke.jnt.IssueStatus;
import org.kohsuke.jnt.JNIssue.Activity;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class BugCountGraph extends Graph<XYDataset> {
    protected IntervalXYDataset buildDataSet(List<Activity> activities) throws IOException {
        TimelineTrendCounter open = new TimelineTrendCounter();
        TimelineTrendCounter total = new TimelineTrendCounter();

        for (Activity a : activities) {
            if(!a.isUpdate()) {
                open.inc(a);
                total.inc(a);
                continue;
            }

            if(a.getField()!= IssueField.STATUS)
                continue;

            IssueStatus o = IssueStatus.valueOf(a.getOldValue());
            IssueStatus n = IssueStatus.valueOf(a.getNewValue());

            if(o.needsWork && !n.needsWork)
                open.dec(a);
            if(!o.needsWork && n.needsWork)
                open.inc(a);
        }

        TrendCounter.completeMissingLinks(open,total);

        TimeTableXYDataset ds = new TimeTableXYDataset();
        open.addTo(ds,"open issues");
        total.addTo(ds,"total issues");

        saveDataset(ds);

        return ds;
    }

    protected String getImageName() {
        return "count.png";
    }

    protected JFreeChart createChart(XYDataset dataset) {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
            null, "time", "# of issues", dataset, true, false, false);
        jfreechart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot)jfreechart.getPlot();
        XYStepAreaRenderer renderer = new XYStepAreaRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0,ColorPalette.RED);
        renderer.setSeriesPaint(1,ColorPalette.GREEN);

        return jfreechart;
    }
}
