package nl.fortytwo.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;


@Configuration
@ComponentScan(basePackageClasses = ApplicationConfiguration.class, excludeFilters = {
        @Filter({ ControllerAdvice.class, Controller.class, RestController.class }),
        @Filter(value = WebMvcConfiguration.class, type = FilterType.ASSIGNABLE_TYPE),
        @Filter(value = SecurityConfiguration.class, type = FilterType.ASSIGNABLE_TYPE) })
public class ApplicationConfiguration {

}
