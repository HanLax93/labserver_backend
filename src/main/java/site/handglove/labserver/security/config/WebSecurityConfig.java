package site.handglove.labserver.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import site.handglove.labserver.security.custom.CustomBcryptPasswordEncoder;
import site.handglove.labserver.security.filter.TokenAuthenticationFilter;
import site.handglove.labserver.security.filter.TokenLoginFilter;

@Configuration
@EnableWebSecurity //@EnableWebSecurity是开启SpringSecurity的默认行为
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomBcryptPasswordEncoder customBcryptPasswordEncoder;


    @Bean
    protected AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService, CustomBcryptPasswordEncoder customBcryptPasswordEncoder) throws Exception {
            DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
            authenticationProvider.setUserDetailsService(userDetailsService);
            authenticationProvider.setPasswordEncoder(customBcryptPasswordEncoder);

            ProviderManager providerManager = new ProviderManager(authenticationProvider);
            providerManager.setEraseCredentialsAfterAuthentication(false);

            return providerManager;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOrigin("*");
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // .csrf().disable()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // .cors().and()
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login").permitAll()
                .requestMatchers("/user/apply").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/*/api-docs", "/druid/**","/doc.html").permitAll()
                // .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**/*.html", "/**/*.css", "/**/*.js", "/profile/**").permitAll()
                .anyRequest().authenticated())
                .addFilterBefore(new TokenAuthenticationFilter(redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new TokenLoginFilter(authenticationManager(userDetailsService, customBcryptPasswordEncoder), redisTemplate))
                .sessionManagement(manage -> manage.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
