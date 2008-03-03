package org.jvnet.its;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNIssue.Activity;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    @Argument
    public final List<String> projects = new ArrayList<String>();

    @Option(name="-span",metaVar="[week|month]",usage="Specifies the timespan for histogram")
    public TimePeriodFactory timePeriodFactory = TimePeriodFactory.MONTH;

    @Option(name="-o",usage="Specifies the output directory")
    public File outputDirectory = new File(".");

    @Option(name="-debug")
    public static boolean debug = false;

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            if(main.projects.isEmpty()) {
                System.err.println("No project is given");
                printUsage(parser);
                return -1;
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            printUsage(parser);
            return -1;
        }

        try {
            main.execute();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void printUsage(CmdLineParser parser) {
        System.err.println("Usage: java -jar issuetracker-stats.jar <java.net project> ...");
        parser.printUsage(System.err);
    }

    public void execute() throws ProcessingException, IOException {
        if(full) {
            JavaNet con = JavaNet.connect();
            for (String project : projects) {
                JNProject p = con.getProject(project);
                System.err.println("Working on "+project);
                Map<Integer,JNIssue> allIssues = p.getIssueTracker().getAll();

                // sort all activities in the time line order
                List<JNIssue.Activity> activities = new ArrayList<Activity>();
                for (JNIssue i : allIssues.values())
                    activities.addAll(i.getActivities());
                Collections.sort(activities);

                File dir = new File(outputDirectory,project);
                dir.mkdirs();
                generateGraphs(activities, dir);
            }
        } else {
            generateGraphs(new ArrayList<Activity>(),outputDirectory);
        }
    }

    private void generateGraphs(List<Activity> activities,File dir) throws IOException {
        new CreatedVsResolvedGraph(timePeriodFactory).generate(activities,dir);
        new BugCountGraph().generate(activities,dir);
        new BugsLifeGraph().generate(activities,dir);

        // generate index.html
        FileOutputStream out = new FileOutputStream(new File(dir, "index.html"));
        IOUtils.copy(getClass().getResourceAsStream("index.html"),out);
        out.close();
    }

    // for debugging.
    static boolean full = true;
}
