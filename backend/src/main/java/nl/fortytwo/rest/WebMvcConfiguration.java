package nl.fortytwo.rest;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import nl.fortytwo.rest.security.CorsInterceptor;

@EnableWebMvc
@ComponentScan(basePackageClasses = ApplicationConfiguration.class, 
        includeFilters = @Filter({ ControllerAdvice.class, Controller.class, RestController.class }) , 
        excludeFilters = @Filter({ Configuration.class, Service.class, Repository.class }) )
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //
        // Only use this if you actually have cross domain requests (most web-apps do not!)
        //
        registry.addInterceptor(new CorsInterceptor("https://localhost:8443"));
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }

    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        SimpleModule module = new SimpleModule("RestAPI", new Version(1, 0, 0, null, "nl.fortytwo.rest", "RestAPI"));
        mapper.registerModule(module);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}