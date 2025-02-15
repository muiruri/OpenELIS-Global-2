package org.openelisglobal.config;

import java.util.Locale;

import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.interceptor.CommonPageAttributesInterceptor;
import org.openelisglobal.interceptor.UrlErrorsInterceptor;
import org.openelisglobal.internationalization.GlobalLocaleResolver;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "spring", "org.openelisglobal" })
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier(value = "ModuleAuthenticationInterceptor")
    HandlerInterceptor moduleAuthenticationInterceptor;
    @Autowired
    UrlErrorsInterceptor urlLocatedErrorsInterceptor;
    @Autowired
    CommonPageAttributesInterceptor pageAttributesInterceptor;
    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean
    public ViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        viewResolver.setContentType("text/html; charset=UTF-8");
        return viewResolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfig = new TilesConfigurer();
        String[] tilesFiles = new String[] { "classpath:/tiles/tiles-defs.xml",
                "classpath:/tiles/tiles-globalOpenELIS.xml" };
        tilesConfig.setDefinitions(tilesFiles);
        return tilesConfig;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/languages/message");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        MessageUtil.setMessageSource(messageSource);
        return messageSource;
    }

    @Bean("localeResolver")
    public LocaleResolver localeResolver() {
        GlobalLocaleResolver localeResolver = new GlobalLocaleResolver();
        String localeName = ConfigurationProperties.getInstance().getPropertyValue(Property.DEFAULT_LANG_LOCALE);
        localeResolver.setDefaultLocale(Locale.forLanguageTag(localeName));
        LocaleContextHolder.setDefaultLocale(Locale.forLanguageTag(localeName));
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(moduleAuthenticationInterceptor).addPathPatterns("/**")
                .excludePathPatterns(SecurityConfig.OPEN_PAGES).excludePathPatterns(SecurityConfig.RESOURCE_PAGES)
                .excludePathPatterns(SecurityConfig.AUTH_OPEN_PAGES);
        registry.addInterceptor(urlLocatedErrorsInterceptor).addPathPatterns("/**");
        registry.addInterceptor(pageAttributesInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Register resource handlers for css, js, etc.
        registry.addResourceHandler("scripts/**").addResourceLocations("classpath:static/scripts/");
        registry.addResourceHandler("css/**").addResourceLocations("classpath:static/css/");
        registry.addResourceHandler("images/**").addResourceLocations("/static/images/");
        registry.addResourceHandler("documentation/**").addResourceLocations("classpath:static/documentation/");
    }

}
