package com.bcsystems.bonds.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String usuario,
        String nombre,
        String rol
) {}
