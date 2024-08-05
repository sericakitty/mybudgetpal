
package hh.sof03.mybudgetpal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import hh.sof03.mybudgetpal.security.services.UserDetailServiceImplement;
import hh.sof03.mybudgetpal.security.jwt.AuthTokenFilter;
import hh.sof03.mybudgetpal.security.jwt.AuthEntryPointJwt;
import hh.sof03.mybudgetpal.config.DotenvConfig;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig{
  @Autowired
  UserDetailServiceImplement userDetailServiceImplement;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private DotenvConfig dotEnvConfig;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }


  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(userDetailServiceImplement).passwordEncoder(passwordEncoder);
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.cors(Customizer.withDefaults())
              .csrf(csrf -> csrf.disable())
              .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandler))
              .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .authorizeHttpRequests(authorize -> authorize
              .requestMatchers("/request-password-reset", "/signup", "/login", "/verify-email", "/reset-password").permitAll()
              .requestMatchers("/auth/**").authenticated()
              .requestMatchers("/user/**").authenticated()
              .requestMatchers("/api/**").authenticated()
              .anyRequest().authenticated());

      http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

      return http.build();
  }

  @Bean
  public CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.addAllowedOrigin(dotEnvConfig.dotenv().get("CORS_ORIGIN"));
      config.addAllowedHeader("*");
      config.addAllowedMethod("*");
      config.setAllowCredentials(true);
      source.registerCorsConfiguration("/**", config);
      return new CorsFilter(source);
  }

}