package co.edu.sena.tiendavirtual.config;

import co.edu.sena.tiendavirtual.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.stream.Collectors;

/**
 * RF-001 a RF-003 y RNF-002 (control de acceso por rol).
 * Los roles de la tabla roles se convierten en ROLE_ADMIN, ROLE_SELLER, ROLE_CUSTOMER, etc.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return email -> users.findByEmailIgnoreCase(email)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .disabled(!u.isActive())
                        .authorities(u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                                .collect(Collectors.toList()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/catalogo/**", "/registro", "/login", "/css/**", "/img/**", "/error")
                    .permitAll()
                .requestMatchers("/carrito/**", "/checkout/**", "/mis-pedidos/**").hasRole("CUSTOMER")
                // RNF-002: el administrador consulta; el vendedor gestiona los cambios de estado.
                .requestMatchers(HttpMethod.POST, "/gestion/pedidos/**").hasRole("SELLER")
                .requestMatchers("/gestion/pedidos/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/vendedor/**").hasRole("SELLER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Inventario y facturación: el administrador solo consulta (RNF-002, RF-042 CA-02).
                .requestMatchers(HttpMethod.POST, "/inventario/**").hasRole("INVENTORY_MANAGER")
                .requestMatchers("/inventario/**").hasAnyRole("INVENTORY_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/facturacion/**").hasRole("BILLING_MANAGER")
                .requestMatchers("/facturacion/**").hasAnyRole("BILLING_MANAGER", "ADMIN")
                .requestMatchers("/domiciliario/**").hasRole("COURIER")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String target = co.edu.sena.tiendavirtual.web.HomeController.landing(authentication);
                    response.sendRedirect(request.getContextPath() + (target == null ? "/panel" : target));
                })
                .failureHandler((request, response, exception) -> {
                    // RF-001 E2: cuenta inactiva; E1: credenciales incorrectas.
                    String query = exception instanceof DisabledException ? "inactiva" : "error";
                    response.sendRedirect(request.getContextPath() + "/login?" + query);
                })
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salida")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
