package org.jvnet.its;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.jnt.JNIssue.Activity;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    @Argument
    public final List<String> projects = new ArrayList<String>();

    @Option(name="-span",metaVar="[week|month]",usage="Specifies the timespan for histogram")
    public void setTimePeriodFactory(TimePeriodFactory tpf) {
        generator.setTimePeriodFactory(tpf);
    }

    @Option(name="-o",usage="Specifies the output directory")
    public File outputDirectory = new File(".");

    @Option(name="-debug")
    public static boolean debug = false;

    private final Generator generator = new Generator();

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
                File dir = new File(outputDirectory, p.getName());
                dir.mkdirs();
                generator.generate(p, dir);
            }
        } else {
            generator.generateGraphs(new ArrayList<Activity>(),outputDirectory);
        }
    }

    // for debugging.
    static boolean full = true;
}
