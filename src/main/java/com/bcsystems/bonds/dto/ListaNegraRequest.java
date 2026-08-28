package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record ListaNegraRequest(
        @NotNull Boolean enListaNegra,
        String motivo
) {}