//package com.aipaint.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.StringHttpMessageConverter;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        // 配置 String 消息转换器的编码为 UTF-8
//        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
//
//        // 配置 FastJson 或其他 JSON 转换器的默认编码
//        converters.forEach(converter -> {
//            if (converter instanceof StringHttpMessageConverter) {
//                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
//            }
//        });
//    }
//
//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        // 确保所有转换器的 Content-Type 都包含 UTF-8
//        converters.forEach(converter -> {
//            List<MediaType> supportedMediaTypes = converter.getSupportedMediaTypes();
//            if (supportedMediaTypes != null && !supportedMediaTypes.isEmpty()) {
//                for (int i = 0; i < supportedMediaTypes.size(); i++) {
//                    MediaType mediaType = supportedMediaTypes.get(i);
//                    if (mediaType != null && mediaType.includes(MediaType.APPLICATION_JSON)) {
//                        supportedMediaTypes.set(i, MediaType.APPLICATION_JSON_UTF8);
//                    }
//                }
//            }
//        });
//    }
//}
