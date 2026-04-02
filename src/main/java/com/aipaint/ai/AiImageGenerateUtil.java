package com.aipaint.ai;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AiImageGenerateUtil {


    public String asyncCall(String sketchImageUrl) {
        log.info("---create task----");
        String taskId = this.createAsyncTask(sketchImageUrl);
        log.info("---wait task done then return image url----");
        return this.waitAsyncTask(taskId);
    }

    /**
     * 创建异步任务
     *
     * @return taskId
     */
    public String createAsyncTask(String sketchImageUrl) {

        String prompt = "";
        try {
            log.info("---视觉识别开始----");
            prompt = AiViewCoverGenerateUtil.simpleMultiModalConversationCall(sketchImageUrl);
            log.info("---视觉识别结束----");
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
        String resultPrompt= StrUtil.isEmpty(prompt)?"": "这个是"+prompt;
        String model = "wanx-sketch-to-image-lite";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .model(model)
                .prompt(resultPrompt)
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
//        System.out.println(text2Image.asyncCall("https://lcj666.oss-cn-hangzhou.aliyuncs.com/draw/5/554ad923-3d0e-4d6a-a12a-70ced73116f2.png"));

    }


}
