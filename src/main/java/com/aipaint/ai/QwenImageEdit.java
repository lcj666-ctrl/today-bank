package com.aipaint.ai;

/**
 * @author cj.lu
 * @date 2026年03⽉27⽇ 15:57
 */

import cn.hutool.json.JSON;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class QwenImageEdit {
    static {
        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
    }

    // 若没有配置环境变量，请用百炼API Key将下行替换为：apiKey="sk-xxx"
    static String apiKey = System.getenv("DASHSCOPE_API_KEY");

    public static String call(String drawingUrl, String prompt) throws ApiException, NoApiKeyException, UploadFileException, IOException {

        String prompt1 = "";
        try {
            prompt1 = AiViewCoverGenerateUtil.simpleMultiModalConversationCall(drawingUrl);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }

        MultiModalConversation conv = new MultiModalConversation();

        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", drawingUrl),
                        Collections.singletonMap("text", "将此图像转换为超高质量版本，保留原始构图，增强细节，改善色彩，使其清晰生动。将这张图转换成干净漂亮的儿童插画。\n" +
                                "保持原图构图。\n" +
                                "儿童插画风格，手绘，彩色铅笔纹理，\n" +
                                "蜡笔着色，线条精美，柔和阴影，\n" +
                                "柔和的色彩，暖色调，柔和的光线，\n" +
                                "故事书插图，干净明亮\n" +
                                "避免写实风格，避免照片写实。"+prompt1)
                )).build();

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("watermark", false);
        parameters.put("negative_prompt", "blurry, bad anatomy, low quality, distorted, extra fingers");

        // 核心质量参数
        parameters.put("steps", 35);
        parameters.put("guidance_scale", 7.5);

        // 分辨率
        parameters.put("size", "1024*1536");
        // 固定seed（方便调试）
        parameters.put("seed", 1234);

        parameters.put("n", 1);
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey("sk-4c8dca7e730d497ab62f92609c33a15f")
                .model("qwen-image-2.0")
                .messages(Collections.singletonList(userMessage))
                .parameters(parameters)
                .build();

        MultiModalConversationResult result = conv.call(param);
        System.out.println(JsonUtils.toJson(result));
        System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("image"));
        return ((String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("image"));

    }

    public static void main(String[] args) {
//        try {
//            call("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/4/48ba82a7-f2ef-4a0d-b681-44f3f0340694.png", "Transform this image into a high-quality version,\n" +
//                    "keep original composition, enhance details,\n" +
//                    "improve colors, make it clean and vivid  This is a children's drawing depicting rural life. The picture includes: a sun, clouds, a house, a car, two figures (possibly an adult and a child), and a large field of grass (or wheat field).");
//        } catch (ApiException | NoApiKeyException | UploadFileException | IOException e) {
//            System.out.println(e.getMessage());
//        }
//        System.exit(0);
//        try {
//            call("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/4/48ba82a7-f2ef-4a0d-b681-44f3f0340694.png", "A high-quality children's drawing of a rural countryside scene.\n" +
//                    "A bright yellow sun in the sky, with soft white clouds.\n" +
//                    "A simple house with a chimney on the left.\n" +
//                    "A small car parked near the house.\n" +
//                    "Two happy people (a man and a woman) standing together and holding hands.\n" +
//                    "A large green field with grass and golden wheat plants in the foreground.\n" +
//                    "Crayon drawing style, colorful, clean lines, simple shapes.\n" +
//                    "Vibrant colors, balanced composition, clear details, 4k illustration.");
//        } catch (ApiException | NoApiKeyException | UploadFileException | IOException e) {
//            System.out.println(e.getMessage());
//        }
//
//
        try {
            call("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/4/b4dbc0f2-4ff6-46d9-85a6-2a3fe2d45437.png", "将此图像转换为高质量版本，保留原始构图，增强细节，改善色彩，使其清晰生动。这是一幅**描绘田园风光的简笔画**（包含山峰、太阳、河流/水渠、庄稼地以及两个正在劳作或游玩的小人）。");
        } catch (ApiException | NoApiKeyException | UploadFileException | IOException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}