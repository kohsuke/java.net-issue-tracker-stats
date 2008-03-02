package org.jvnet.its;

import org.jfree.data.time.Month;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.Week;

import java.util.Calendar;

/**
 * Given a date, return the {@link TimePeriod} that includes the date.
 *
 * <p>
 * Different instance of the factory uses different time period,
 * which in turn translates to different "bin width" in the final picture.
 *
 * @author Kohsuke Kawaguchi
 */
public enum TimePeriodFactory {
    WEEK() {
        public TimePeriod toTimePeriod(Calendar cal) {
            return new Week(cal.getTime());
        }
    },
    MONTH() {
        public TimePeriod toTimePeriod(Calendar cal) {
            return new Month(cal.getTime());
        }
    };

    public abstract TimePeriod toTimePeriod(Calendar cal);
}
