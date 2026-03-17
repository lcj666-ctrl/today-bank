package com.aipaint.vo;

import lombok.Data;
import java.util.List;

@Data
public class DrawingListVO {
    private Integer allNumber;
    private Long aiNumber;
    private Long id;
    private Integer total;
    private String date;
    private List<DrawingVO> drawingList;
}