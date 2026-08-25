package com.bcsystems.bonds.dto;

import com.bcsystems.bonds.domain.en.Rol;

import java.time.LocalDateTime;

public record PersonaResponse(
    Integer idPersona,
    String nombre,
    String apellido,
    String usuario,
    Rol rol,
    Boolean activa,
    LocalDateTime fechaRegistro
) {}
