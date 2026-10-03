package com.jasmeet.blogapi.config;

import com.jasmeet.blogapi.security.JwtAuthenticationEntryPoint;
import com.jasmeet.blogapi.security.JwtAuthenticationFilter;
import com.jasmeet.blogapi.service.impl.CustomUserDetailsServiceImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
		securedEnabled = true,
		jsr250Enabled = true
)
public class SecurityConfig {

	private final CustomUserDetailsServiceImpl customUserDetailsService;
	private final JwtAuthenticationEntryPoint unauthorizedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(
			CustomUserDetailsServiceImpl customUserDetailsService,
			JwtAuthenticationEntryPoint unauthorizedHandler,
			JwtAuthenticationFilter jwtAuthenticationFilter) {

		this.customUserDetailsService = customUserDetailsService;
		this.unauthorizedHandler = unauthorizedHandler;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				.cors(cors -> {})
				.csrf(csrf -> csrf.disable())

				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(unauthorizedHandler)
				)

				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)

				.authorizeHttpRequests(auth -> auth

						// Allow all GET requests
						.requestMatchers(HttpMethod.GET, "/api/**")
						.permitAll()

						// Authentication endpoints
						.requestMatchers(HttpMethod.POST, "/api/auth/**")
						.permitAll()

						// Username/email availability
						.requestMatchers(
								HttpMethod.GET,
								"/api/users/checkUsernameAvailability",
								"/api/users/checkEmailAvailability"
						)
						.permitAll()

						// Everything else requires authentication
						.anyRequest()
						.authenticated()
				);

		// JWT filter
		http.addFilterBefore(
				jwtAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class
		);

		return http.build();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider =
				new DaoAuthenticationProvider();

		provider.setUserDetailsService(customUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder());

		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}