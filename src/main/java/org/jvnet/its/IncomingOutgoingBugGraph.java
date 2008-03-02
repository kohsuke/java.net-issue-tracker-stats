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
import org.jfree.data.time.Week;
import org.jfree.data.xy.IntervalXYDataset;
import org.kohsuke.jnt.IssueField;
import org.kohsuke.jnt.IssueStatus;
import org.kohsuke.jnt.JNIssue.Activity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class IncomingOutgoingBugGraph {
    /**
     * X-axis is per week.
     */
    public static final class Label implements Comparable<Label>, Serializable {
        public final int year;
        public final int week;

        public Label(Calendar timestamp) {
            year = timestamp.get(Calendar.YEAR);
            week = timestamp.get(Calendar.WEEK_OF_YEAR);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Label label = (Label) o;
            return week == label.week && year == label.year;
        }

        public int hashCode() {
            int result;
            result = year;
            result = 31 * result + week;
            return result;
        }

        public int compareTo(Label that) {
            if(this.year-that.year!=0)
                return this.year-that.year;
            return this.week-that.week;
        }

        public String toString() {
            return DATE.format(toCalendar().getTime());
        }

        private GregorianCalendar toCalendar() {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.WEEK_OF_YEAR,week);
            return cal;
        }

        public TimePeriod toTimePeriod() {
            return new Week(toCalendar().getTime());
        }

        private static final long serialVersionUID = 1L;
    }

    public void generate(List<Activity> activities) throws IOException {
        IntervalXYDataset ds;
        if(Main.full) {
            ds = buildDataSet(activities);
        } else {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./data.out"));
            try {
                ds = (IntervalXYDataset)ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        JFreeChart chart = createChart(ds);

        BufferedImage image = chart.createBufferedImage(640,480);
        FileOutputStream fos = new FileOutputStream("graph.png");
        ImageIO.write(image, "PNG", fos);
        fos.close();
    }

    private IntervalXYDataset buildDataSet(List<Activity> activities) throws IOException {
        Map<Label,Integer> created = new TreeMap<Label,Integer>();
        Map<Label,Integer> resolved = new TreeMap<Label,Integer>();

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

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./data.out"));
        oos.writeObject(ds);
        oos.close();

        return ds;
    }

    private void buildDataSet(Map<Label, Integer> incoming, TimeTableXYDataset ds, String label) {
        for (Entry<Label, Integer> e : incoming.entrySet())
            ds.add(e.getKey().toTimePeriod(),e.getValue(),label,false);
    }

    private void inc(Activity a, Map<Label, Integer> data) {
        IncomingOutgoingBugGraph.Label l = new Label(a.getTimestamp());
        Integer v = data.get(l);
        if(v==null) v=1;
        else        v=v+1;
        data.put(l,v);
    }

    private JFreeChart createChart(IntervalXYDataset dataset) {
        DateAxis xAxis = new DateAxis("date");

        NumberAxis yAxis = new NumberAxis("# of issues");
        XYItemRenderer renderer = new XYBarRenderer();
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setForegroundAlpha(0.8F);
        renderer.setSeriesPaint(0,new Color(0,255,0));
        renderer.setSeriesPaint(1,new Color(255,0,0));

        XYBarRenderer xybarrenderer = (XYBarRenderer)plot.getRenderer();
        xybarrenderer.setDrawBarOutline(false);

        JFreeChart chart = new JFreeChart("Created vs Resolved", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

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

    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy/MM/dd");
}
