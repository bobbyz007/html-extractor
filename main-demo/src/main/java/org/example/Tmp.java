package org.example;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Tmp {
    public static void main(String[] args) throws IOException {
        List<String> names = FileUtils.readLines(new File("/home/justin/tmp2"), StandardCharsets.UTF_8);
        double sum = 0;
        for (String name : names) {
            sum += Double.valueOf(name);
        }

        System.out.println(sum);
    }
}
