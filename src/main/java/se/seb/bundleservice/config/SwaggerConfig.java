package se.seb.bundleservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TreeMap;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SEB Bundle Service")
                        .contact(new Contact()
                                .name("Amir Reza Mashalian")
                                .url("https://github.com/mashalian/bundle-service")
                        )
                        .version(SwaggerConfig.class.getPackage().getImplementationVersion())
                        .description("Recruitment task")
                );
    }

    @Bean
    public OpenApiCustomiser sortSchemasAlphabetically() {
        return openApi -> {
            var openApiComponents = openApi.getComponents();
            openApiComponents.setSchemas(new TreeMap<>(openApiComponents.getSchemas()));
        };
    }
}
