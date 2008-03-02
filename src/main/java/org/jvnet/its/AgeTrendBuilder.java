package org.jvnet.its;

import org.jfree.data.xy.CategoryTableXYDataset;
import org.kohsuke.jnt.JNIssue.Activity;

/**
 * {@link TrendBuilder} where the type of <tt>x</tt> is the # of dates
 * since the bug was reported until the activity happened.
 *
 * @author Kohsuke Kawaguchi
 */
public class AgeTrendBuilder extends TrendBuilder<Double,CategoryTableXYDataset> {

    protected Double getKey(Activity a) {
        long event = a.getTimestamp().getTimeInMillis();
        long start = a.getParent().getCreationDate().getTimeInMillis();
        return (event-start)/MILLISECONDS_IN_DAY;
    }

    protected void add(CategoryTableXYDataset ds, Double start, Double end, int value, String seriesName) {
        ds.add(start,value,seriesName);
    }

    private static final double MILLISECONDS_IN_DAY =
            1000/*ms->s*/ * 60/*sec->min*/ * 60/*min->hour*/ * 24/*hour->day*/;
}