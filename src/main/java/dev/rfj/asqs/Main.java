package dev.rfj.asqs;

import dev.rfj.asqs.scans.AbstractScan;
import dev.rfj.asqs.scans.impl.*;
import dev.rfj.asqs.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        ApplicationConfig appConfig = ArgumentParser.parse(args);

        if (appConfig.shouldPrintUsageAndExit) {
            System.err.println("Usage: java -jar asqs.jar <pattern>[ <pattern>[ <pattern>[...]]]");
            return;
        }

        LoggingUtil.setLogLevel(appConfig.logLevel);

        log.info("Starting ASQS");
        log.fine("args: " + Arrays.toString(args));

        List<File> files = new LinkedList<>();
        for (String pattern : appConfig.filePatterns) {
            List<File> filesForPattern = FileGlobber.getFilesForPattern(pattern);
            log.fine(String.format("found %d files for pattern '%s' ",
                    filesForPattern.size(),
                    pattern
            ));
            files.addAll(filesForPattern);
        }
        log.info(String.format(
                "Found %d files to process",
                files.size()
        ));

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("file");
        for (AbstractScan rule : appConfig.scans) {
            csvBuilder.append(",");
            csvBuilder.append(rule.getClass().getSimpleName());
            log.log(Level.FINE, "apply scan '{0}'", rule.getClass().getSimpleName());
        }
        csvBuilder.append("\n");

        if (appConfig.isSingleThreaded) {
            processUsingFori(files, appConfig.scans, csvBuilder);
        } else {
            processUsingParallelStream(files, appConfig.scans, csvBuilder);
        }

        File outputFile = new File(appConfig.outputFileName);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(csvBuilder.toString());
        } catch (IOException e) {
            log.warning("failed to write result.csv");
        }
    }

    private static void processUsingParallelStream(List<File> files, AbstractScan[] rules, StringBuilder csvBuilder) {
        long startTime = System.currentTimeMillis();

        files.parallelStream()
                .map(file -> {
                    log.fine(String.format(
                            "processing file '%s'",
                            file
                    ));
                    try (ZipFile zipFile = new ZipFile(file)) {
                        long startTimeForFile = System.currentTimeMillis();
                        String csvLine = Arrays.stream(rules)
                                .map(rule -> rule.scan(zipFile))
                                .map(Object::toString)
                                .map(CsvEncoder::encodeString)
                                .collect(Collectors.joining(","));
                        long endTimeForFile = System.currentTimeMillis();
                        log.fine(String.format(
                                "applied %d rules in %dms",
                                rules.length,
                                endTimeForFile - startTimeForFile
                        ));
                        return CsvEncoder.encodeString(file.getName()) + "," + csvLine;
                    } catch (IOException e) {
                        log.warning("failed to read zip file " + file);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                .forEach(line -> csvBuilder.append(line).append("\n"));

        long endTime = System.currentTimeMillis();
        log.info(String.format(
                "processed %d files in %dms",
                files.size(),
                endTime - startTime)
        );
    }

    private static void processUsingFori(List<File> files, AbstractScan[] rules, StringBuilder csvBuilder) {
        long startTime = System.currentTimeMillis();

        for (File file : files) {
            log.fine(String.format(
                    "processing file '%s'",
                    file
            ));
            try (ZipFile zipFile = new ZipFile(file)) {
                long startTimeForFile = System.currentTimeMillis();
                String csvLine = Arrays.stream(rules)
                        .map(rule -> rule.scan(zipFile))
                        .map(Object::toString)
                        .map(CsvEncoder::encodeString)
                        .collect(Collectors.joining(","));
                long endTimeForFile = System.currentTimeMillis();
                log.fine(String.format(
                        "applied %d rules in %dms",
                        rules.length,
                        endTimeForFile - startTimeForFile
                ));
                csvBuilder
                        .append(CsvEncoder.encodeString(file.getName()))
                        .append(",")
                        .append(csvLine)
                        .append("\n");
            } catch (IOException e) {
                log.warning("failed to read zip file " + file);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info(String.format(
                "processed %d files in %dms",
                files.size(),
                endTime - startTime)
        );
    }
}
