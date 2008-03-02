package org.jvnet.its;

import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.kohsuke.jnt.JNIssue.Activity;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Kohsuke Kawaguchi
 */
public class TimelineTrendBuilder extends TrendBuilder<Calendar,TimeTableXYDataset> {
    protected Calendar getKey(Activity a) {
        return a.getTimestamp();
    }

    protected void add(TimeTableXYDataset ds, Calendar start, Calendar end, int value, String seriesName) {
        ds.add(new SimpleTimePeriod(start.getTime(),end==null?new Date():end.getTime()),value, seriesName, false);
    }
}
