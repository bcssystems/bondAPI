package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Configuracion;
import com.bcsystems.bonds.dto.ConfiguracionRequest;
import com.bcsystems.bonds.dto.ConfiguracionResponse;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.ConfiguracionRepository;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;
    private final AuditoriaService auditoriaService;

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("fondoCajaChica", "2000"),
            Map.entry("tasaInteresMoraPagare", "5"),
            Map.entry("diasListaNegra", "90"),
            Map.entry("titularPagare", "PRISCILA ARONG KIM LOPEZ"),
            Map.entry("descripcionEmpresa", "PRISCILA ARONG KIM LOPEZ / BONDS"),
            Map.entry("direccionEmpresa", "")
    );

    private static final Map<String, String> DESCRIPCIONES = Map.ofEntries(
            Map.entry("fondoCajaChica", "Monto inicial de la caja chica al abrirla"),
            Map.entry("tasaInteresMoraPagare", "Tasa de interes mensual de mora que se imprime en el pagare"),
            Map.entry("diasListaNegra", "Dias sin abonar para pasar automaticamente a lista negra"),
            Map.entry("titularPagare", "Nombre del titular que se imprime en el pagare"),
            Map.entry("descripcionEmpresa", "Nombre/datos de la empresa en tickets y documentos"),
            Map.entry("direccionEmpresa", "Direccion de la empresa en tickets y documentos")
    );

    @PostConstruct
    public void inicializar() {
        DEFAULTS.forEach((clave, valor) -> {
            if (configuracionRepository.findByClave(clave).isEmpty()) {
                configuracionRepository.save(Configuracion.builder()
                        .clave(clave).valor(valor)
                        .descripcion(DESCRIPCIONES.get(clave))
                        .actualizadaEn(LocalDateTime.now())
                        .build());
            }
        });
    }

    @Override
    public List<ConfiguracionResponse> listar() {
        return configuracionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ConfiguracionResponse obtener(String clave) {
        Configuracion config = configuracionRepository.findByClave(clave)
                .orElseGet(() -> crearPorDefecto(clave));
        return toResponse(config);
    }

    @Override
    public String getValor(String clave, String valorDefault) {
        Optional<Configuracion> opt = configuracionRepository.findByClave(clave);
        if (opt.isPresent() && opt.get().getValor() != null && !opt.get().getValor().isBlank()) {
            return opt.get().getValor();
        }
        return valorDefault;
    }

    @Override
    public int getValorInt(String clave, int valorDefault) {
        String valor = getValor(clave, String.valueOf(valorDefault));
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    @Override
    public double getValorDouble(String clave, double valorDefault) {
        String valor = getValor(clave, String.valueOf(valorDefault));
        try {
            return Double.parseDouble(valor.trim().replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    @Override
    @Transactional
    public ConfiguracionResponse actualizar(String clave, ConfiguracionRequest request) {
        Configuracion config = configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new NotFoundException("Configuracion no encontrada con clave: " + clave));
        config.setValor(request.valor());
        if (request.descripcion() != null && !request.descripcion().isBlank()) {
            config.setDescripcion(request.descripcion());
        }
        config.setActualizadaEn(LocalDateTime.now());
        config = configuracionRepository.save(config);

        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar("Configuracion", config.getIdConfiguracion(),
                "ACTUALIZACION", usuario, clave + " = " + request.valor());

        return toResponse(config);
    }

    private Configuracion crearPorDefecto(String clave) {
        String valor = DEFAULTS.getOrDefault(clave, "");
        Configuracion config = Configuracion.builder()
                .clave(clave).valor(valor)
                .descripcion(DESCRIPCIONES.get(clave))
                .actualizadaEn(LocalDateTime.now())
                .build();
        return configuracionRepository.save(config);
    }

    private ConfiguracionResponse toResponse(Configuracion c) {
        return new ConfiguracionResponse(
                c.getIdConfiguracion(), c.getClave(), c.getValor(),
                c.getDescripcion(), c.getActualizadaEn());
    }
}