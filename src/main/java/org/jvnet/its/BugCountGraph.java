package org.jvnet.its;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
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
            final Map<Calendar,Integer> count = new TreeMap<Calendar,Integer>();

            int n=0;

            void inc(Activity a) {
                n++;
                count.put(a.getTimestamp(),n);
            }

            void dec(Activity a) {
                n--;
                count.put(a.getTimestamp(),n);
            }

            void build() {
                for (Activity a : activities) {
                    if(!a.isUpdate()) {
                        inc(a);
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

                Entry<Calendar,Integer> p = null;
                for (Entry<Calendar,Integer> e : count.entrySet()) {
                    if(p!=null) {
                        add(p,e.getKey().getTime());
                    }
                    p = e;
                }
                if(p!=null)
                    add(p,new Date());
            }

            private void add(Entry<Calendar, Integer> p, Date end) {
                ds.add(new SimpleTimePeriod(p.getKey().getTime(),end),
                       p.getValue(), "# of issues", false);
            }
        }

        new Counter().build();

        saveDataset(ds);

        return ds;
    }

    private JFreeChart createChart(IntervalXYDataset dataset) {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
            null, "time", "# of issues", dataset, false, false, false);
        jfreechart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot)jfreechart.getPlot();
        plot.setRenderer(new XYStepAreaRenderer());

//        PeriodAxis periodaxis = new PeriodAxis("Date");
//        periodaxis.setAutoRangeTimePeriodClass(org.jfree.data.time.Month.class);
//        periodaxis.setMajorTickTimePeriodClass(org.jfree.data.time.Month.class);
//        PeriodAxisLabelInfo aperiodaxislabelinfo[] = new PeriodAxisLabelInfo[2];
//        aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(org.jfree.data.time.Month.class, new SimpleDateFormat("MMM"), new RectangleInsets(2D, 2D, 2D, 2D), new Font("SansSerif", 1, 10), Color.blue, false, new BasicStroke(0.0F), Color.lightGray);
//        aperiodaxislabelinfo[1] = new PeriodAxisLabelInfo(org.jfree.data.time.Year.class, new SimpleDateFormat("yyyy"));
//        periodaxis.setLabelInfo(aperiodaxislabelinfo);
//        plot.setDomainAxis(periodaxis);
        return jfreechart;
    }
}
