package com.aipaint.ai;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author cj.lu
 * @date 2026年03⽉27⽇ 14:25
 */
@Component
@Slf4j
public class AiViewCoverGenerateUtil {

    // 各地域配置不同，请根据实际地域修改
    static {
        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
    }

    public static String simpleMultiModalConversationCall(String drawingUrl)
            throws ApiException, NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", drawingUrl),
                        Collections.singletonMap("text", "这个画的是什么 不要你的思考过程直接给我结论 简单简洁的结论 你觉得最像的物品"))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
//                .apiKey(System.getenv("DASHSCOPE_VIEW_API_KEY"))
                .apiKey("sk-d98a10f4d8834ba3af3f0efa60216a76")
                .model("qwen3.5-plus")  // 此处以qwen3.5-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                .messages(Arrays.asList(userMessage))
                .build();
        MultiModalConversationResult result = conv.call(param);
        String name = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
        log.info("你画的是:{}", name);
        return name;

    }

    public static void main(String[] args) {
        try {
            System.out.println(simpleMultiModalConversationCall("https://lcj666.oss-cn-hangzhou.aliyuncs.com/index/4/7057cf2c-914d-4668-8307-4a741e96a123.png"));
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("asdsdsads");
        System.exit(0);
    }

}
