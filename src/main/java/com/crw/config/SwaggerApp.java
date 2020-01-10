package com.crw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerApp {

    @Bean
    public Docket buildDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInf())    //.apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.crw.contact.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo buildApiInf(){
        return new ApiInfoBuilder()
                .title("运营商数据接口API")
                .description("请求的API接口")
                .contact(new Contact("TT Team","",""))
                .version("1.0")
                .build();

    }
}
