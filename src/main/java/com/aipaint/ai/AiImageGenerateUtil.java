package com.aipaint.ai;

import com.alibaba.dashscope.aigc.imagesynthesis.*;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;

@Component
@Slf4j
public class AiImageGenerateUtil {


    public String asyncCall(String sketchImageUrl) {
        log.info("---create task----");
        String taskId = this.createAsyncTask( sketchImageUrl);
        log.info("---wait task done then return image url----");
       return this.waitAsyncTask(taskId);
    }

    /**
     * 创建异步任务
     *
     * @return taskId
     */
    public String createAsyncTask(String sketchImageUrl) {
        String prompt = "a cute 3D baby animal based on a child doodle drawing, simple shapes, round body, big glossy eyes";
//        String sketchImageUrl = "https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/1/d49df1bc-7bd0-4d63-9046-d1c57e840f1a.png";
        String model = "wanx-sketch-to-image-lite";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .model(model)
                .prompt(prompt)
                .n(1)
                .size("768*768")
                .sketchImageUrl(sketchImageUrl)
                .style("<3d cartoon>")
                .build();

        String task = "image2image";
        ImageSynthesis imageSynthesis = new ImageSynthesis(task);
        ImageSynthesisResult result = null;
        try {
            result = imageSynthesis.asyncCall(param);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        String taskId = result.getOutput().getTaskId();
        log.info("生成的任务id是：{}", taskId);
         return taskId;
    }


    /**
     * 等待异步任务结束
     *
     * @param taskId 任务id
     */
    public String waitAsyncTask(String taskId) {
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            // If you have set the DASHSCOPE_API_KEY in the system environment variable, the apiKey can be null.
            result = imageSynthesis.wait(taskId, null);
        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException(e.getMessage());
        }

        log.info(JsonUtils.toJson(result.getOutput()));
        log.info(JsonUtils.toJson(result.getUsage()));
        ImageSynthesisOutput output = result.getOutput();
        return output.getResults().get(0).get("url");
    }


    public static void main(String[] args) {
        AiImageGenerateUtil text2Image = new AiImageGenerateUtil();
        // text2Image.asyncCall();
    }


}
