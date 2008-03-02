package org.jvnet.its;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.kohsuke.jnt.IssueField;
import org.kohsuke.jnt.IssueStatus;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNIssue.Activity;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kohsuke Kawaguchi
 */
public class BugsLifeGraph extends Graph<TableXYDataset> {
    protected TableXYDataset buildDataSet(List<JNIssue.Activity> activities) throws IOException {
        Map<IssueStatus,AgeTrendBuilder> trends = new EnumMap<IssueStatus,AgeTrendBuilder>(IssueStatus.class);
        for (IssueStatus s : IssueStatus.values())
            trends.put(s,new AgeTrendBuilder());

        Collections.sort(activities,AGE_COMPARATOR);

        for (Activity a : activities) {
            if(!a.isUpdate()) {
                trends.get(a.getCurrentStatus()).inc(a);
                continue;
            }

            if(a.getField()!= IssueField.STATUS)
                continue;

            IssueStatus o = IssueStatus.valueOf(a.getOldValue());
            trends.get(o).dec(a);

            IssueStatus n = IssueStatus.valueOf(a.getNewValue());
            trends.get(n).inc(a);
        }

        TrendBuilder.completeMissingLinks(trends.values());

        CategoryTableXYDataset ds = new CategoryTableXYDataset();
        for (Entry<IssueStatus,AgeTrendBuilder> e : trends.entrySet())
            e.getValue().addTo(ds,e.getKey().name());

        return ds;
    }

    protected JFreeChart createChart(TableXYDataset dataset) {
        JFreeChart jfreechart = ChartFactory.createStackedXYAreaChart(
            null, "time", "# of issues", dataset, PlotOrientation.VERTICAL, true, false, false);
        jfreechart.setBackgroundPaint(Color.WHITE);

//        XYPlot plot = (XYPlot)jfreechart.getPlot();
//        XYStepAreaRenderer renderer = new StackedXYStepAreaRenderer();
//        plot.setRenderer(renderer);
        
//        renderer.setSeriesPaint(0,ColorPalette.RED);
//        renderer.setSeriesPaint(1,ColorPalette.GREEN);

        return jfreechart;
    }

    protected String getImageName() {
        return "life.png";
    }

    private static final Comparator<Activity> AGE_COMPARATOR = new Comparator<Activity>() {
        public int compare(Activity lhs, Activity rhs) {
            return new Long(lhs.getAge()).compareTo(rhs.getAge());
        }
    };

}
