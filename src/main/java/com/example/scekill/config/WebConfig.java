package com.example.scekill.config;

import com.example.scekill.interceptor.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private  UserArgmentResolver userArgmentResolver;
    @Autowired
    private AccessInterceptor accessInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers){
        resolvers.add(userArgmentResolver);
    }

//    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
//            "classpath:/META-INF/resources/", "classpath:/resources/",
//            "classpath:/static/", "classpath:/public/" };


//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        if (!registry.hasMappingForPattern("/webjars/**")) {
//            registry.addResourceHandler("/webjars/**").addResourceLocations(
//                    "classpath:/META-INF/resources/webjars/");
//        }
//        if (!registry.hasMappingForPattern("/**")) {
//            registry.addResourceHandler("/**").addResourceLocations(
//                    CLASSPATH_RESOURCE_LOCATIONS);
//        }
//
//    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
