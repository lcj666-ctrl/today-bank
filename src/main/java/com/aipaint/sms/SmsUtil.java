package com.aipaint.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信工具类
 * 用于发送验证码和验证验证码
 */
@Slf4j
@Component
public class SmsUtil {

//    @Value("${aliyun.sms.accessKeyId}")
//    private String accessKeyId;
//
//    @Value("${aliyun.sms.accessKeySecret}")
//    private String accessKeySecret;
//
//    @Value("${aliyun.sms.signName}")
//    private String signName;
//
//    @Value("${aliyun.sms.templateCode}")
//    private String templateCode;
//
//    @Value("${aliyun.sms.expireTime:300}") // 默认5分钟
//    private long expireTime;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SendSmsVerifyCode sendSmsVerifyCode;

    @Resource
    private CheckSmsVerifyCode checkSmsVerifyCode;

    /**
     * 生成6位随机验证码
     * @return 验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送短信验证码
     * @param phoneNumber 手机号
     * @return 验证码
     * @throws Exception 发送异常
     */
    public String sendSmsCode(String phoneNumber) throws Exception {
        sendSmsVerifyCode.SendSms(phoneNumber);
        // 生成验证码
//        String code = generateCode();
//        log.info("生成验证码: {} for phone: {}", code, phoneNumber);

        // 发送短信
//        sendSms(phoneNumber, code);

        // 存储验证码到Redis
        String key = "sms:code:" + phoneNumber;
//        redisTemplate.opsForValue().set(key, code, expireTime, TimeUnit.SECONDS);
//        log.info("验证码已存储到Redis: {} expires in {}s", key, expireTime);

        return null;
    }

    /**
     * 验证验证码
     * @param phoneNumber 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String phoneNumber, String code) {
        String result = checkSmsVerifyCode.checkSms(phoneNumber, code);
//        String key = "sms:code:" + phoneNumber;
//        String storedCode = redisTemplate.opsForValue().get(key);
//
//        if (storedCode == null) {
//            log.warn("验证码不存在或已过期: {}", phoneNumber);
//            return false;
//        }

//        boolean result = code.equals(storedCode);
//        if (result) {
//            // 验证成功后删除验证码
//            redisTemplate.delete(key);
//            log.info("验证码验证成功: {}", phoneNumber);
//        } else {
//            log.warn("验证码验证失败: {} - provided: {}, stored: {}", phoneNumber, code, storedCode);
//        }
        return true;
    }

    /**
     * 发送短信
     * @param phoneNumber 手机号
     * @param code 验证码
     * @throws ClientException 客户端异常
     */
   /* private void sendSms(String phoneNumber, String code) throws ClientException {
        // 设置超时时间-可自行调整
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        // 初始化ascClient需要的几个参数
        final String product = "Dysmsapi";
        final String domain = "dysmsapi.aliyuncs.com";

        // 初始化ascClient,暂时不支持多region
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        // 组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        // 使用post提交
        request.setMethod(com.aliyuncs.http.MethodType.POST);
        // 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
        request.setPhoneNumbers(phoneNumber);
        // 必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        // 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        request.setTemplateParam("{\"code\":\"" + code + "\"}");

        // 可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
        // request.setSmsUpExtendCode("90997");

        // 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        // request.setOutId("yourOutId");

        // 请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
            // 请求成功
            log.info("短信发送成功: {}, message: {}", phoneNumber, sendSmsResponse.getMessage());
        } else {
            log.error("短信发送失败: {}, code: {}, message: {}", 
                    phoneNumber, sendSmsResponse.getCode(), sendSmsResponse.getMessage());
            throw new ClientException(sendSmsResponse.getCode(), sendSmsResponse.getMessage());
        }
    }*/

    /**
     * 检查手机号是否在冷却期
     * @param phoneNumber 手机号
     * @param coolDownSeconds 冷却时间(秒)
     * @return 是否在冷却期
     */
    public boolean isInCoolDown(String phoneNumber, int coolDownSeconds) {
        String key = "sms:cooldown:" + phoneNumber;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * 设置手机号冷却期
     * @param phoneNumber 手机号
     * @param coolDownSeconds 冷却时间(秒)
     */
    public void setCoolDown(String phoneNumber, int coolDownSeconds) {
        String key = "sms:cooldown:" + phoneNumber;
        redisTemplate.opsForValue().set(key, "1", coolDownSeconds, TimeUnit.SECONDS);
        log.info("手机号已设置冷却期: {} for {}s", phoneNumber, coolDownSeconds);
    }
}
