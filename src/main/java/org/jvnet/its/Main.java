package org.jvnet.its;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.jnt.JNIssue;
import org.kohsuke.jnt.JNIssue.Activity;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    @Argument
    public final List<String> projects = new ArrayList<String>();

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
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

                new IncomingOutgoingBugGraph().generate(activities);
            }
        } else {
            new IncomingOutgoingBugGraph().generate(new ArrayList<Activity>());
        }
    }

    static boolean full = true;
}