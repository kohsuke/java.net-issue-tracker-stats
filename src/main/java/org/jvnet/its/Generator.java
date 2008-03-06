package org.jvnet.its;

import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.ProcessingException;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNIssue.Activity;
import org.apache.commons.io.IOUtils;
import static org.jvnet.its.TimePeriodFactory.MONTH;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class Generator {
    private TimePeriodFactory timePeriodFactory = MONTH;

    public void setTimePeriodFactory(TimePeriodFactory timePeriodFactory) {
        this.timePeriodFactory = timePeriodFactory;
    }

    public void generate(JNProject p, File outputDirectory) throws ProcessingException, IOException {
        Map<Integer,JNIssue> allIssues = p.getIssueTracker().getAll();

        // sort all activities in the time line order
        List<Activity> activities = new ArrayList<Activity>();
        for (JNIssue i : allIssues.values())
            activities.addAll(i.getActivities());
        Collections.sort(activities);

        generateGraphs(activities, outputDirectory);
    }

    /*package*/ void generateGraphs(List<Activity> activities, File dir) throws IOException {
        new CreatedVsResolvedGraph(timePeriodFactory).generate(activities,dir);
        new BugCountGraph().generate(activities,dir);
        new BugsLifeGraph().generate(activities,dir);

        // generate index.html
        FileOutputStream out = new FileOutputStream(new File(dir, "index.html"));
        out.write("<html><body>".getBytes());
        IOUtils.copy(Main.class.getResourceAsStream("index.html"),out);
        out.write("</body></html>".getBytes());
        out.close();
    }
}
