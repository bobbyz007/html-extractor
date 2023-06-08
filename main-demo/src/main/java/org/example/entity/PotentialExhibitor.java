package org.example.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PotentialExhibitor {
    private String name;
    private String booth;
    private String contact;
    private String profile;
    private String url;
}