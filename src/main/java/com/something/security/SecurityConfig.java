package com.something.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.something.filter.CustomAuthenticationFilter;
import com.something.filter.CustomAuthorizationFilter;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final UserDetailsService userDetailsService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
				authenticationManagerBean());
		customAuthenticationFilter.setFilterProcessesUrl("/api/login");
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(STATELESS);
		http.authorizeRequests().antMatchers("/api/login/**", "/api/token/refresh/**", "/api/wallet/**", "/api/admin/validation").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/authentication/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/authentication/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/active/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/changePassword/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/user/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/validation/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/history/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/cashWallet/balance/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/commissionWallet/balance/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/package/buy/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/affiliate/directCommission/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/packages/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/investment/withdrawCapital/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/affiliate/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/affiliateByRoot/**").permitAll();
		http.authorizeRequests().antMatchers(PUT, "/api/user/updateRef/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/userMapDown/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/userMapDown5Level/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/userTreeUp/**").permitAll();
		http.authorizeRequests().antMatchers(GET, "/api/userTreeUpToRoot/**").permitAll();
		http.authorizeRequests().antMatchers(PUT, "/api/affiliate/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/affiliate/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/wallet/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/forgotpassword/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/resetPassword/**").permitAll();
		http.authorizeRequests().antMatchers(POST, "/api/user/getPoint/**").permitAll();
		
		
		http.authorizeRequests().antMatchers(GET, "/api/getAllData/**", "/api/admin/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests().antMatchers(POST, "/api/admin/getPoint", "/api/admin/addPack").hasAnyAuthority("ROLE_ADMIN");
//		admin/getPoint
		http.authorizeRequests().antMatchers(POST, "/api/save/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests().antMatchers(POST, "/api/user/regis/**").permitAll();
		// http.authorizeRequests().antMatchers(POST,
		// "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN");
		http.authorizeRequests().anyRequest().authenticated();
		http.addFilter(customAuthenticationFilter);
		http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

		http.cors().configurationSource(request -> {
			var cors = new CorsConfiguration();
			cors.setAllowedOrigins(List.of("*"));
			cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			cors.setAllowedHeaders(List.of("*"));
			return cors;
		});

	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
