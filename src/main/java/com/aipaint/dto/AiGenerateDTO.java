package com.aipaint.dto;

import lombok.Data;

@Data
public class AiGenerateDTO {
    private Long drawingId;
    private String style="cartoon";
    private String uuid;
}