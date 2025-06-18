package org.example.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoxLittleFileReorder2 {
    static final String WORKSPACE_FILE = "C:\\Users\\Justin\\Desktop\\workspace.txt";
    static final String DEST_DIR = "D:\\tmp\\MP4\\";

    static final String OUTPUT_DIR = "D:\\tmp";
    public static void main(String[] args) throws IOException {
        Map<String, String> sourceMap = extractSourceFilenames(WORKSPACE_FILE);
        File destDir = new File(DEST_DIR);
        if (!destDir.exists()) {
            return;
        }
        for (File f : destDir.listFiles()) {
            String name = f.getName();
            int idx = name.lastIndexOf(".");
            String filename = name.substring(0, idx);
            String suffix = name.substring(idx + 1, name.length());
            String keyPart = null;
            if (suffix.equalsIgnoreCase("srt")) {
                int endIndx = filename.lastIndexOf("_");
                int beginIndx = filename.indexOf(" ");
                keyPart = filename.substring(beginIndx + 1, endIndx).trim();
            } else {
                int beginIndx = StringUtils.indexOfAny(filename, '.', '_', ' ');
                keyPart = filename.substring(beginIndx + 1, filename.length()).trim();
            }

            if (sourceMap.containsKey(keyPart)) {
                copy(sourceMap.get(keyPart), keyPart, suffix, f);
            } else {
                if (keyPart.endsWith("？")) {

                    keyPart = keyPart.substring(0, keyPart.length() - 1);
                    if (sourceMap.containsKey(keyPart)) {
                        System.out.println("trim with chinese ? as the end: " + keyPart);
                        copy(sourceMap.get(keyPart), keyPart, suffix, f);
                    } else {
                        System.err.println("Err!: " + name);
                    }
                } else {
                    System.err.println("Err!: " + name);
                }
            }
        }
    }

    static void copy(String prefix, String keyPart, String suffix, File sourceFile) throws IOException {
        String outputName = prefix + " " + keyPart
                + ((suffix.equalsIgnoreCase("srt") ? "_en-US" : "")
                + "." + suffix);
        FileUtils.moveFile(sourceFile, new File(OUTPUT_DIR, outputName));
    }

    static Map<String, String> extractSourceFilenames(String filepath) throws IOException {
        File file = new File(filepath);
        if (!file.exists()) {
            return null;
        }
        Map<String, String> resultMap = new HashMap<>();
        List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (StringUtils.endsWithAny(line, "?", "？")) {
                line = line.substring(0, line.length() - 1);
            }
            resultMap.put(line, String.format("%03d", i + 1));
        }

        return resultMap;
    }
}
