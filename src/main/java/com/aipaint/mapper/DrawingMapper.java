package com.aipaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aipaint.entity.Drawing;
import java.util.List;

public interface DrawingMapper extends BaseMapper<Drawing> {
    List<Drawing> selectByUserId(Long userId);
}