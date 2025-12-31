package dev.rfj.asqs;

import dev.rfj.asqs.rules.AbstractRule;
import dev.rfj.asqs.rules.impl.AllowsCleartextTraffic;
import dev.rfj.asqs.rules.impl.UsesFirebase;
import dev.rfj.asqs.util.FileGlobber;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        log.info("args: " + Arrays.toString(args));
        if ("-h".equalsIgnoreCase(args[0])) {
            System.err.println("Usage: java -jar asqs.jar <pattern>[ <pattern>[ <pattern>[...]]]");
            return;
        }

        log.info("Starting ASQS");

        List<File> files = new LinkedList<>();
        for (int i = 0; i < args.length; i++) {
            String pattern = args[i];
            List<File> filesForPattern = FileGlobber.getFilesForPattern(pattern);
            log.info("found %d files for pattern '%s' ".formatted(
                    filesForPattern.size(),
                    pattern
            ));
            files.addAll(filesForPattern);
        }
        log.info("Found %d files overall".formatted(files.size()));

        AbstractRule[] rules = constructRules();

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("file");
        for (AbstractRule rule : rules) {
            csvBuilder.append(",");
            csvBuilder.append(rule.getClass().getSimpleName());
        }
        csvBuilder.append("\n");

        long startTime = System.currentTimeMillis();
        for (File file : files) {
            log.finest("processing file '%s'".formatted(file));
            long startTimeForFile = System.currentTimeMillis();
            String csvLine = Arrays.stream(rules)
                    .map(rule -> rule.raisesRedFlag(file))
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            csvBuilder
                    .append(file.getName())
                    .append(",")
                    .append(csvLine)
                    .append("\n");
            long endTimeForFile = System.currentTimeMillis();
            log.finest("applied %d rules in %dms".formatted(rules.length, endTimeForFile - startTimeForFile));
        }
        long endTime = System.currentTimeMillis();
        log.info("processed %d files in %dms".formatted(files.size(), endTime - startTime));

        File outputFile = new File("result-%d.csv".formatted(System.currentTimeMillis()));
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(csvBuilder.toString());
        } catch (IOException e) {
            log.warning("failed to write result.csv");
        }
    }

    private static AbstractRule[] constructRules() {
        return new AbstractRule[]{
                new UsesFirebase(),
                new AllowsCleartextTraffic()
        };
    }
}
