package org.kurento.tutorial.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	@Autowired
	private UserDetailsService myAppUserDetailsService;
	
	private AuthenticationEntryPoint authEntryPoint;

	
	@Override
	protected void configure(HttpSecurity https) throws Exception {
		authEntryPoint= new AuthenticationEntryPoint();
		https.authorizeRequests()
		.antMatchers(
                "/js/**",
                "/css/**",
                "/img/**",
                "/webjars/**").permitAll().and()
		.authorizeRequests().antMatchers("/","/index","/register","/login","/error","/console/**","/get-link","/image/**").permitAll();
		https.authorizeRequests().and().formLogin()  //login configuration
                .loginPage("/login")
                .loginProcessingUrl("/handler-login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/work");
		https.csrf().disable().authorizeRequests()
		.anyRequest().authenticated()
		.and().httpBasic()
		.authenticationEntryPoint(authEntryPoint);
//		.and().logout()    //logout configuration
//		.logoutUrl("/hadler-logout") 
//		.logoutSuccessUrl("index")
//		.and().exceptionHandling() //exception handling configuration
//		.accessDeniedPage("/error");
	} 
        @Autowired
   	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
              auth.userDetailsService(myAppUserDetailsService).passwordEncoder(passwordEncoder);
	}
}
