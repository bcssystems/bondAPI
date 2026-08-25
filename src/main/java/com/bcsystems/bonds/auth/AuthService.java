package com.bcsystems.bonds.auth;

import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.domain.Token;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.repository.PersonaRepository;
import com.bcsystems.bonds.repository.TokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final PersonaRepository personaRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(PersonaRepository personaRepository,
                       TokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.personaRepository = personaRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (personaRepository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw new InvalidEntryException("El usuario ya existe");
        }

        Persona persona = Persona.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .usuario(request.usuario())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activa(true)
                .build();

        persona = personaRepository.save(persona);

        Token token = jwtService.generateToken(persona);
        tokenRepository.save(token);

        return buildAuthResponse(token, persona);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usuario(), request.password()
                )
        );

        Persona persona = personaRepository.findByUsuarioIgnoreCase(request.usuario())
                .orElseThrow(() -> new InvalidEntryException("Credenciales inválidas"));

        revokeAllTokens(persona);

        Token token = jwtService.generateToken(persona);
        tokenRepository.save(token);

        return buildAuthResponse(token, persona);
    }

    @Transactional
    public AuthResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidEntryException("Token de refresco inválido");
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        Persona persona = personaRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new InvalidEntryException("Usuario no encontrado"));

        var storedToken = tokenRepository.findByRefreshToken(refreshToken).orElse(null);
        if (storedToken == null || storedToken.getIsRevoked() || storedToken.getIsExpired()) {
            throw new InvalidEntryException("Token de refresco inválido o expirado");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            storedToken.setIsExpired(true);
            tokenRepository.save(storedToken);
            throw new InvalidEntryException("Token de refresco expirado");
        }

        revokeAllTokens(persona);

        Token newToken = jwtService.generateToken(persona);
        tokenRepository.save(newToken);

        return buildAuthResponse(newToken, persona);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            var storedToken = tokenRepository.findByToken(token).orElse(null);
            if (storedToken != null) {
                storedToken.setIsRevoked(true);
                storedToken.setIsExpired(true);
                tokenRepository.save(storedToken);
            }
        }
    }

    private void revokeAllTokens(Persona persona) {
        var tokens = tokenRepository.findAllValidTokensByPersona(persona.getIdPersona());
        tokens.forEach(t -> {
            t.setIsExpired(true);
            t.setIsRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    private AuthResponse buildAuthResponse(Token token, Persona persona) {
        return new AuthResponse(
                token.getToken(),
                token.getRefreshToken(),
                persona.getUsuario(),
                persona.getNombre() + " " + persona.getApellido(),
                persona.getRol().name()
        );
    }
}
