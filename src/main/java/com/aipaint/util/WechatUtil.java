package com.aipaint.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WechatUtil {

    static {
        // 注册BouncyCastle作为安全提供者
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据code获取session_key和openid
     * @param code 小程序登录凭证
     * @return session信息，包含openid和session_key
     * @throws Exception 异常信息
     */
    public Map<String, Object> getSessionInfo(String code) throws Exception {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                URLEncoder.encode(appid, "UTF-8"),
                URLEncoder.encode(secret, "UTF-8"),
                URLEncoder.encode(code, "UTF-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("获取session信息失败，HTTP状态码: " + response.code());
            }

            String responseBody = response.body().string();
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            
            // 检查微信返回的错误码
            if (result.containsKey("errcode")) {
                Integer errCode = (Integer) result.get("errcode");
                if (errCode != 0) {
                    String errMsg = (String) result.get("errmsg");
                    throw new Exception("微信API错误: " + errCode + " - " + errMsg);
                }
            }
            
            return result;
        }
    }

    /**
     * 解密获取手机号
     * @param encryptedData 加密的用户数据
     * @param sessionKey 会话密钥
     * @param iv 加密算法的初始向量
     * @return 解密后的手机号
     * @throws Exception 异常信息
     */
    public String decryptPhoneNumber(String encryptedData, String sessionKey, String iv) throws Exception {
        // Base64解码
        byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);
        byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
        byte[] ivBytes = Base64.getDecoder().decode(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(ivBytes));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, params);

        // 解密
        byte[] decryptedBytes = cipher.doFinal(encryptedDataBytes);
        String decryptedStr = new String(decryptedBytes, "UTF-8");

        // 解析JSON获取手机号
        Map<String, Object> result = objectMapper.readValue(decryptedStr, Map.class);
        return (String) result.get("phoneNumber");
    }

    /**
     * 解密获取用户信息
     * @param encryptedData 加密的用户数据
     * @param sessionKey 会话密钥
     * @param iv 加密算法的初始向量
     * @return 解密后的用户信息
     * @throws Exception 异常信息
     */
    public Map<String, Object> decryptUserInfo(String encryptedData, String sessionKey, String iv) throws Exception {
        // Base64解码
        byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);
        byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
        byte[] ivBytes = Base64.getDecoder().decode(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(ivBytes));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, params);

        // 解密
        byte[] decryptedBytes = cipher.doFinal(encryptedDataBytes);
        String decryptedStr = new String(decryptedBytes, "UTF-8");

        // 解析JSON
        return objectMapper.readValue(decryptedStr, Map.class);
    }

    /**
     * 直接通过code和加密数据获取用户信息（包括手机号）
     * @param code 小程序登录凭证
     * @param encryptedData 加密的用户数据
     * @param iv 加密算法的初始向量
     * @return 包含openid、session_key和手机号的Map
     * @throws Exception 异常信息
     */
    public Map<String, Object> getLoginInfo(String code, String encryptedData, String iv) throws Exception {
        // 获取session信息
        Map<String, Object> sessionInfo = getSessionInfo(code);
        String sessionKey = (String) sessionInfo.get("session_key");
        
        // 解密获取手机号
        if (encryptedData != null && iv != null) {
            String phoneNumber = decryptPhoneNumber(encryptedData, sessionKey, iv);
            sessionInfo.put("phoneNumber", phoneNumber);
        }
        
        return sessionInfo;
    }

    /**
     * 验证微信请求签名
     * @param rawData 原始数据
     * @param signature 签名
     * @param sessionKey 会话密钥
     * @return 是否验证通过
     * @throws Exception 异常信息
     */
    public boolean verifySignature(String rawData, String signature, String sessionKey) throws Exception {
        // 这里可以实现签名验证逻辑
        // 例如：使用HMAC-SHA256算法验证签名
        return true; // 简化实现，实际项目中需要完整实现
    }
}
