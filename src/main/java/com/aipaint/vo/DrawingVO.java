package com.aipaint.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DrawingVO {
    private Long id;
    private String drawingUrl;
    private String aiImageUrl;
    private String style;
    private Integer status;
    private LocalDate localDate;
}