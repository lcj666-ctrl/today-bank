package com.aipaint.sms;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.DefaultCredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author cj.lu
 * @date 2026年03⽉16⽇ 16:52
 */
@Component
public class CheckSmsVerifyCode {
    private static final String ACCESS_KEY_ID = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
    private static final String ACCESS_KEY_SECRET = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");

    public String checkSms(String phoneNumber, String code)   {
        // HttpClient Configuration
        /*HttpClient httpClient = new ApacheAsyncHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(10)) // Set the connection timeout time, the default is 10 seconds
                .responseTimeout(Duration.ofSeconds(10)) // Set the response timeout time, the default is 20 seconds
                .maxConnections(128) // Set the connection pool size
                .maxIdleTimeOut(Duration.ofSeconds(50)) // Set the connection pool timeout, the default is 30 seconds
                // Configure the proxy
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<your-proxy-hostname>", 9001))
                        .setCredentials("<your-proxy-username>", "<your-proxy-password>"))
                // If it is an https connection, you need to configure the certificate, or ignore the certificate(.ignoreSSL(true))
                .x509TrustManagers(new X509TrustManager[]{})
                .keyManagers(new KeyManager[]{})
                .ignoreSSL(false)
                .build();*/

        // Configure Credentials authentication information
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(ACCESS_KEY_ID)
                        .accessKeySecret(ACCESS_KEY_SECRET)
//                        .securityToken(SECURITY_TOKEN)
                        .build()
        );

        // Configure the Client
        try (
                AsyncClient client = AsyncClient.builder()
                        .region("ap-southeast-1") // Region ID
                        //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                        .credentialsProvider(provider)
                        //.serviceConfiguration(Configuration.create()) // Service-level configuration
                        // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                        .overrideConfiguration(
                                ClientOverrideConfiguration.create()
                                        // Endpoint 请参考 https://api.aliyun.com/product/Dypnsapi
                                        .setEndpointOverride("dypnsapi.aliyuncs.com")
                                //.setConnectTimeout(Duration.ofSeconds(30))
                        )
                        .build()) {

            // Parameter settings for API request
            CheckSmsVerifyCodeRequest checkSmsVerifyCodeRequest = CheckSmsVerifyCodeRequest.builder()
                    .phoneNumber(phoneNumber)
                    .verifyCode(code)
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

            // Asynchronously get the return value of the API request
            CompletableFuture<CheckSmsVerifyCodeResponse> response = client.checkSmsVerifyCode(checkSmsVerifyCodeRequest);
            // Synchronously get the return value of the API request
            CheckSmsVerifyCodeResponse resp = null;
            try {
                resp = response.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            System.out.println(new Gson().toJson(resp));
            // Asynchronous processing of return values
            /*response.thenAccept(resp -> {
                System.out.println(new Gson().toJson(resp));
            }).exceptionally(throwable -> { // Handling exceptions
                System.out.println(throwable.getMessage());
                return null;
            });*/

        }
        return null;
    }
}