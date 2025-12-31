package dev.rfj.asqs.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Collections.*;

public class FileGlobber {

    private static final Logger log = Logger.getLogger(FileGlobber.class.getName());

    public static List<File> getFilesForPattern(String pattern) {
        Path path = Paths.get(pattern);
        if (Files.isRegularFile(path)) {
            return singletonList(path.toFile());
        }

        String baseDir = pattern.substring(0, pattern.lastIndexOf(File.separator));
        Path baseDirPath = Paths.get(baseDir);
        if (!Files.isDirectory(baseDirPath)) {
            System.err.println("not a directory: " + baseDir);
            return Collections.emptyList();
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        try (Stream<Path> stream = Files.walk(baseDirPath)) {
            return stream
                    .filter(matcher::matches)
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            log.warning("failed to list files in pattern '%s'".formatted(pattern));
            return Collections.emptyList();
        }
    }
}
