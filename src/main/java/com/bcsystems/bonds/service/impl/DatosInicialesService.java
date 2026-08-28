package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Caja;
import com.bcsystems.bonds.domain.Sucursal;
import com.bcsystems.bonds.domain.en.CajaEstado;
import com.bcsystems.bonds.domain.en.TipoCaja;
import com.bcsystems.bonds.repository.CajaRepository;
import com.bcsystems.bonds.repository.SucursalRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatosInicialesService {

    private final SucursalRepository sucursalRepository;
    private final CajaRepository cajaRepository;

    public DatosInicialesService(SucursalRepository sucursalRepository,
                                 CajaRepository cajaRepository) {
        this.sucursalRepository = sucursalRepository;
        this.cajaRepository = cajaRepository;
    }

    @PostConstruct
    @Transactional
    public void inicializar() {
        Sucursal sucursal = sucursalRepository.findByActivaTrueOrderByNombreAsc().stream()
                .findFirst()
                .orElse(null);
        if (sucursal == null && sucursalRepository.count() == 0) {
            sucursal = sucursalRepository.save(Sucursal.builder()
                    .nombre("Sucursal Principal")
                    .direccion("")
                    .telefono("")
                    .activa(true)
                    .build());
        }
        if (sucursal == null) {
            return;
        }

        boolean existeNormal = cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .anyMatch(c -> c.getTipo() == TipoCaja.NORMAL);
        if (!existeNormal) {
            cajaRepository.save(Caja.builder()
                    .nombre("Caja Principal")
                    .tipo(TipoCaja.NORMAL)
                    .sucursal(sucursal)
                    .estado(CajaEstado.CERRADA)
                    .saldoActual(0.0)
                    .activa(true)
                    .build());
        }

        boolean existeChica = cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .anyMatch(c -> c.getTipo() == TipoCaja.CHICA);
        if (!existeChica) {
            cajaRepository.save(Caja.builder()
                    .nombre("Caja Chica")
                    .tipo(TipoCaja.CHICA)
                    .sucursal(sucursal)
                    .estado(CajaEstado.CERRADA)
                    .saldoActual(0.0)
                    .activa(true)
                    .build());
        }
    }
}