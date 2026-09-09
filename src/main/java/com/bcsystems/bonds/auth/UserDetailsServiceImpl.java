package com.bcsystems.bonds.auth;

import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.repository.PersonaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PersonaRepository personaRepository;

    public UserDetailsServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Persona persona = personaRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = persona.getRol().getPermisos().stream()
                .map(p -> new SimpleGrantedAuthority(p.getClave()))
                .collect(Collectors.toList());

        if (persona.getPermisosAdicionales() != null) {
            persona.getPermisosAdicionales().forEach(pa -> {
                authorities.add(new SimpleGrantedAuthority(pa.getPermiso().getClave()));
            });
        }

        return new User(
                persona.getUsuario(),
                persona.getPassword(),
                persona.getActiva(),
                true, true, true,
                authorities
        );
    }
}