package org.example.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FoxLittleRename {
    static final String WORKSPACE_FILE = "C:\\Users\\Justin\\Desktop\\workspace.txt";
    static final String SOURCE_DIR = "D:\\Downloads\\Bili23_Downloader_v1.56.1_win_x64_with_ffmpeg_release\\Bili23 Downloader\\download\\";

    static final String OUTPUT_DIR = "D:\\tmp";
    public static void main(String[] args) throws IOException {
        File sourceDir = new File(SOURCE_DIR);
        File[] files = sourceDir.listFiles(f -> f.isFile());
        List<String> names = FileUtils.readLines(new File(WORKSPACE_FILE), StandardCharsets.UTF_8);
        Arrays.sort(files, Comparator.comparing(File::getName));
        assert files.length == names.size();

        for (int i = 0; i < files.length; i++) {
            String sourceFilename = files[i].getName();
            int idx = sourceFilename.lastIndexOf(".");
            String suffix = sourceFilename.substring(idx + 1, sourceFilename.length());

            String targetName = names.get(i);
            idx = targetName.indexOf(".");
            targetName = targetName.substring(idx + 1, targetName.length()).trim();
            targetName = String.format("%03d ", i + 1) + targetName + "." + suffix;


            FileUtils.moveFile(files[i], new File(OUTPUT_DIR, targetName));
        }
    }
}
