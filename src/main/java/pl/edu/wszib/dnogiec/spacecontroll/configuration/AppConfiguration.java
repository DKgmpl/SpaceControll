package pl.edu.wszib.dnogiec.spacecontroll.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pl.edu.wszib.dnogiec.spacecontroll.filters.AdminFilter;

@Configuration
@ComponentScan("pl.edu.wszib.dnogiec.spacecontroll")
public class AppConfiguration {

    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilter() {
        FilterRegistrationBean<AdminFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AdminFilter());
        registrationBean.addUrlPatterns("/*");  
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
