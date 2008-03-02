package org.jvnet.its;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.kohsuke.jnt.IssueField;
import org.kohsuke.jnt.IssueStatus;
import org.kohsuke.jnt.JNIssue.Activity;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class BugCountGraph extends Graph {
    public void generate(List<Activity> activities) throws IOException {
        IntervalXYDataset ds;
        if(Main.full) {
            ds = buildDataSet(activities);
        } else {
            ds = (IntervalXYDataset)loadDataset();
        }

        JFreeChart chart = createChart(ds);

        write(chart,new File("count.png"));
    }

    private IntervalXYDataset buildDataSet(final List<Activity> activities) throws IOException {
        final TimeTableXYDataset ds = new TimeTableXYDataset();

        /**
         * Nested class so that we can define method-local functions.
         */
        class Counter {
            final TreeMap<Calendar,Integer> openTrend = new TreeMap<Calendar,Integer>();
            final TreeMap<Calendar,Integer> totalTrend = new TreeMap<Calendar,Integer>();

            int open,total;

            void inc(Activity a) {
                open++;
                openTrend.put(a.getTimestamp(), open);
            }

            void dec(Activity a) {
                open--;
                openTrend.put(a.getTimestamp(), open);
            }

            void build() {
                for (Activity a : activities) {
                    if(!a.isUpdate()) {
                        inc(a);
                        totalTrend.put(a.getTimestamp(),++total);
                        continue;
                    }

                    if(a.getField()!= IssueField.STATUS)
                        continue;

                    IssueStatus o = IssueStatus.valueOf(a.getOldValue());
                    IssueStatus n = IssueStatus.valueOf(a.getNewValue());

                    if(o.needsWork && !n.needsWork)
                        dec(a);
                    if(!o.needsWork && n.needsWork)
                        inc(a);
                }

                completeMissingLinks(openTrend,totalTrend.keySet());
                completeMissingLinks(totalTrend,openTrend.keySet());

                addTrend(openTrend, "open issues");
                addTrend(totalTrend, "total issues");
            }

            private void completeMissingLinks(TreeMap<Calendar,Integer> trend, Set<Calendar> dataPoints) {
                for (Calendar dp : dataPoints) {
                    Entry<Calendar, Integer> e = trend.floorEntry(dp);
                    if(e==null)
                        trend.put(dp,0);
                    else
                        trend.put(dp,e.getValue());
                }
            }

            private void addTrend(Map<Calendar, Integer> trend, String seriesName) {
                Entry<Calendar,Integer> p = null;
                for (Entry<Calendar,Integer> e : trend.entrySet()) {
                    if(p!=null) {
                        add(p,e.getKey().getTime(),seriesName);
                    }
                    p = e;
                }
                if(p!=null)
                    add(p,new Date(),seriesName);
            }

            private void add(Entry<Calendar, Integer> p, Date end, String seriesName) {
                ds.add(new SimpleTimePeriod(p.getKey().getTime(),end),
                       p.getValue(), seriesName, false);
            }
        }

        new Counter().build();

        saveDataset(ds);

        return ds;
    }

    private JFreeChart createChart(IntervalXYDataset dataset) {
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
