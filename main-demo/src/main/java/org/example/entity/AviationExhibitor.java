package org.example.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AviationExhibitor {
    @ExcelProperty(index = 0, value = "Name")
    private String name;
    @ExcelProperty(index = 1, value = "Stand Number")
    private String standNumber;
    @ExcelProperty(index = 2, value = "Products")
    private String products;
    @ExcelProperty(index = 3, value = "Contact")
    private String contact;
    @ExcelProperty(index = 4, value = "Details")
    private String detailsUrl;
}