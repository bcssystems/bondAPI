package com.bcsystems.bonds.auth;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String usuario,
        String nombre,
        String rol,
        List<String> permisos
) {}