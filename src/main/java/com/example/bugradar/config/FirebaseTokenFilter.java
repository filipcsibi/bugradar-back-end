package com.example.bugradar.config;

import com.example.bugradar.service.ModeratorService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final ModeratorService moderatorService;

    public FirebaseTokenFilter(FirebaseAuth firebaseAuth, ModeratorService moderatorService) {
        this.firebaseAuth = firebaseAuth;
        this.moderatorService = moderatorService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String idToken = authorizationHeader.substring(7);

            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
                String uid = decodedToken.getUid();

                // Verificăm dacă utilizatorul este banat
                if (moderatorService.isBanned(uid)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Your account has been banned. Please contact support.\"}");
                    return;
                }

                // Determinăm rolurile utilizatorului
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (moderatorService.isModerator(uid)) {
                    authorities.add(new SimpleGrantedAuthority("MODERATOR"));
                }
                authorities.add(new SimpleGrantedAuthority("USER"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uid, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid Firebase token\"}");
                return;
            } catch (Exception e) {
                // Dacă verificarea pentru ban eșuează, continuăm dar logăm eroarea
                System.err.println("Error checking user ban status: " + e.getMessage());

                try {
                    // Reluăm token-ul pentru că în catch nu avem acces la decodedToken
                    FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
                    String uid = decodedToken.getUid();

                    // Creăm autentificarea fără verificarea de ban
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(uid, null, List.of(new SimpleGrantedAuthority("USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (FirebaseAuthException ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}