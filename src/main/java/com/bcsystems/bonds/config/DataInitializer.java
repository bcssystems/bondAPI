package com.bcsystems.bonds.config;

import com.bcsystems.bonds.domain.TipoPago;
import com.bcsystems.bonds.repository.TipoPagoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TipoPagoRepository tipoPagoRepository;

    public DataInitializer(TipoPagoRepository tipoPagoRepository) {
        this.tipoPagoRepository = tipoPagoRepository;
    }

    @Override
    public void run(String... args) {
        if (tipoPagoRepository.count() == 0) {
            String[] formas = {
                "EFECTIVO", "TARJETA DE CREDITO", "TARJETA DE DEBITO",
                "TRANSFERENCIA", "DEPOSITO", "CHEQUE",
                "MONEDERO ELECTRONICO", "QR", "OTRO"
            };
            for (String nombre : formas) {
                tipoPagoRepository.save(TipoPago.builder()
                        .nombre(nombre)
                        .activo(true)
                        .build());
            }
        }
    }
}
