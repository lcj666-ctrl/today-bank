package com.aipaint.service;

import com.aipaint.entity.Drawing;
import com.aipaint.vo.UploadVO;
import com.aipaint.vo.DrawingListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DrawingService {
    UploadVO upload(MultipartFile file,String drawType);
    List<DrawingListVO> getList();
    Drawing getById(Long id );
    void update(Drawing drawing);
}