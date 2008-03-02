package org.jvnet.its;

import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.kohsuke.jnt.JNIssue.Activity;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
public class TrendCounter {
    private final TreeMap<Calendar,Integer> trend = new TreeMap<Calendar,Integer>();
    private int n;

    public void inc(Activity a) {
        n++;
        trend.put(a.getTimestamp(), n);
    }

    public void dec(Activity a) {
        n--;
        trend.put(a.getTimestamp(), n);
    }

    /**
     * Auguments the trend data among each other
     * so that we have data points for all the dates.
     */
    public static void completeMissingLinks(Collection<TrendCounter> counters) {
        for (TrendCounter x : counters)
            for (TrendCounter y : counters)
                if(x!=y)
                    x.completeMissingLinks(y.trend.keySet());
    }

    public static void completeMissingLinks(TrendCounter... counters) {
        completeMissingLinks(Arrays.asList(counters));
    }

    private void completeMissingLinks(Set<Calendar> dataPoints) {
        for (Calendar dp : dataPoints) {
            Entry<Calendar, Integer> e = trend.floorEntry(dp);
            if(e==null)
                trend.put(dp,0);
            else
                trend.put(dp,e.getValue());
        }
    }

    /**
     * Adds this trend to the given data set.
     */
    public void addTo(TimeTableXYDataset ds, String seriesName) {
        Entry<Calendar,Integer> p = null;
        for (Entry<Calendar,Integer> e : trend.entrySet()) {
            if(p!=null)
                add(ds,p,e.getKey().getTime(),seriesName);
            p = e;
        }
        if(p!=null)
            add(ds,p,new Date(),seriesName);
    }

    private void add(TimeTableXYDataset ds, Entry<Calendar,Integer> p, Date end, String seriesName) {
        ds.add(new SimpleTimePeriod(p.getKey().getTime(),end),
               p.getValue(), seriesName, false);
    }
}
