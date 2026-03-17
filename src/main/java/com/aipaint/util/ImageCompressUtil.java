package com.aipaint.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageCompressUtil {

    public static InputStream compress(InputStream inputStream, int maxWidth, int maxHeight, float quality) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);
        int width = image.getWidth();
        int height = image.getHeight();

        // 计算压缩后的尺寸
        if (width > maxWidth || height > maxHeight) {
            float widthRatio = (float) maxWidth / width;
            float heightRatio = (float) maxHeight / height;
            float ratio = Math.min(widthRatio, heightRatio);
            width = (int) (width * ratio);
            height = (int) (height * ratio);
        }

        // 创建压缩后的图像
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        // 输出压缩后的图像
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}