package org.kurento.tutorial.configs;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration  implements WebMvcConfigurer{
	 @Bean
	    public MessageSource messageSource() {
	        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
	        // Load file: validation.properties
	        messageSource.setBasename("classpath:validation");
	        messageSource.setDefaultEncoding("UTF-8");
	        return messageSource;
	    }

	  @Bean
	  public LocalValidatorFactoryBean validator() {
	      LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
	      bean.setValidationMessageSource(messageSource());
	      return bean;
	  }
}
