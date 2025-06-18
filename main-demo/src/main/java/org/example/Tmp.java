package org.example;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;

public class Tmp {
    public static void main(String[] args) throws IOException {
        File file = new File("D:\\workspace\\english\\littlefox\\level6\\The Lemonade Detectives\\audio\\");
        File[] files = file.listFiles();
        Arrays.sort(files, Comparator.comparing(File::getName));

        for (int i = 0; i < 100; i++) {
            String name = files[i].getName();
            name = name.substring(0, 3);
            if (Integer.valueOf(name) != (i + 1)) {
                System.err.println("error");
            }
        }
    }
}
