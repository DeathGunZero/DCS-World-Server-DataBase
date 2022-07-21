package cn.deathgun.dcsluasocket.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 *  Swagger配置类
 */
@Configuration
public class SwaggerConfig {
    public static final String SWAGGER_SCAN_BASE_PACKAGE = "cn.deathgun.dcsluasocket.controller";

    public static final String VERSION = "1.0.0";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(SWAGGER_SCAN_BASE_PACKAGE))
                .paths(PathSelectors.any()) // 可以根据url路径设置哪些请求加入文档，忽略哪些请求
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("我的第一个项目")   //设置文档的标题
                .description("*** API 接口文档")   // 设置文档的描述
                .version(VERSION)   // 设置文档的版本
                .contact(new Contact("****", "", "***@qq.com"))
                .termsOfServiceUrl("http://***.***.***")   // 配置服务网站，
                .build();
    }

}