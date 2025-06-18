package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class EpisodeContent {
    private String title;
    private List<String> paragraphs;
}
