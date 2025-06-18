package org.example.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FoxLittleFileReorder {
    static final String SOURCE_DIR = "D:\\workspace\\english\\littlefox\\level2\\My First Readers 2\\";
    static final String DEST_DIR = "D:\\Downloads\\Bili23_Downloader_v1.56.1_win_x64_with_ffmpeg_release\\Bili23 Downloader\\download\\";

    static final String OUTPUT_DIR = "D:\\tmp";
    public static void main(String[] args) throws IOException {
        Map<String, String> sourceMap = extractSourceFilenames(SOURCE_DIR);
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
                int beginIndx = filename.indexOf(" ");
                keyPart = filename.substring(beginIndx + 1, filename.length()).trim();
            }

            if (sourceMap.containsKey(keyPart)) {
                copy(sourceMap.get(keyPart), keyPart, suffix, f);
            } else {
                if (keyPart.endsWith("？")) {
                    System.out.println("key part not contained in source with ？as the end: " + keyPart);
                    keyPart = keyPart.substring(0, keyPart.length() - 1);
                    copy(sourceMap.get(keyPart), keyPart, suffix, f);
                } else {
                    System.err.println("Err!");
                }
            }
        }
    }

    static void copy(String prefix, String keyPart, String suffix, File sourceFile) throws IOException {
        String outputName = prefix + "_" + keyPart
                + ((suffix.equalsIgnoreCase("srt") ? "_en-US" : "")
                + "." + suffix);
        FileUtils.copyFile(sourceFile, new File(OUTPUT_DIR, outputName));
    }

    static Map<String, String> extractSourceFilenames(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return null;
        }
        Map<String, String> resultMap = new HashMap<>();
        for (File f : dir.listFiles()) {
            String name = f.getName();
            name = name.substring(0, name.lastIndexOf("."));
            int index = name.indexOf("_");
            String prefix = name.substring(0, index);
            String suffix = name.substring(index + 1, name.length());
            resultMap.put(suffix, prefix);
        }
        return resultMap;
    }
}