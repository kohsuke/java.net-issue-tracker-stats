package org.jvnet.its;

import org.jfree.data.xy.XYDataset;
import org.kohsuke.jnt.JNIssue.Activity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class TrendBuilder<K extends Comparable<K>, DS extends XYDataset> {
    private final TreeMap<K,Integer> trend = new TreeMap<K,Integer>();
    private int n;

    public void inc(Activity a) {
        n++;
        trend.put(getKey(a), n);
    }

    protected abstract K getKey(Activity a);

    public void dec(Activity a) {
        n--;
        trend.put(getKey(a), n);
    }

    /**
     * Auguments the trend data among each other
     * so that we have data points for all the dates.
     */
    public static void completeMissingLinks(Collection<TrendBuilder> builders) {
        for (TrendBuilder x : builders)
            for (TrendBuilder y : builders)
                if(x!=y)
                    x.completeMissingLinks(y.trend.keySet());
    }

    public static void completeMissingLinks(TrendBuilder... builders) {
        completeMissingLinks(Arrays.asList(builders));
    }

    private void completeMissingLinks(Set<K> dataPoints) {
        for (K dp : dataPoints) {
            Entry<K,Integer> e = trend.floorEntry(dp);
            if(e==null)
                trend.put(dp,0);
            else
                trend.put(dp,e.getValue());
        }
    }

    /**
     * Adds this trend to the given data set.
     */
    public void addTo(DS ds, String seriesName) {
        Entry<K,Integer> p = null;
        for (Entry<K,Integer> e : trend.entrySet()) {
            if(p!=null)
                add(ds,p.getKey(),e.getKey(),p.getValue(),seriesName);
            p = e;
        }
        if(p!=null)
            add(ds,p.getKey(),null,p.getValue(),seriesName);
    }

    /**
     * Lower-level function to add data to the dataset.
     */
    protected abstract void add(DS ds, K start, K end, int value, String seriesName);
}
