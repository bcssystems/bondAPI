package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Cliente;
import com.bcsystems.bonds.domain.Credito;
import com.bcsystems.bonds.domain.en.EstadoCredito;
import com.bcsystems.bonds.repository.AbonoRepository;
import com.bcsystems.bonds.repository.ClienteRepository;
import com.bcsystems.bonds.repository.CreditoRepository;
import com.bcsystems.bonds.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListaNegraScheduler {

    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;
    private final AbonoRepository abonoRepository;
    private final ConfiguracionService configuracionService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void ejecutarRevisionAutomatica() {
        int diasLimite = configuracionService.getValorInt("diasListaNegra", 90);
        LocalDateTime ahora = LocalDateTime.now();

        List<Cliente> candidatos = clienteRepository.findActivos(null).stream()
                .filter(c -> Boolean.TRUE.equals(c.getTieneCredito()))
                .filter(c -> (c.getSaldoActual() == null || c.getSaldoActual() > 0))
                .filter(c -> !Boolean.TRUE.equals(c.getEnListaNegra()))
                .toList();

        int agregados = 0;
        for (Cliente cliente : candidatos) {
            LocalDateTime referencia = fechaReferenciaMorosidad(cliente.getIdCliente());
            if (referencia == null) continue;
            if (referencia.plusDays(diasLimite).isBefore(ahora)) {
                cliente.setEnListaNegra(true);
                cliente.setFechaListaNegra(ahora);
                cliente.setMotivoListaNegra("Sin abonos en los ultimos " + diasLimite + " dias");
                clienteRepository.save(cliente);
                agregados++;
                log.info("Cliente agregado a lista negra (automatico): {} {}", cliente.getNombre(), cliente.getApellidoPaterno());
            }
        }
        if (agregados > 0) {
            log.info("Lista negra automatica: {} clientes marcados", agregados);
        }
    }

    private LocalDateTime fechaReferenciaMorosidad(Integer idCliente) {
        var maxAbono = abonoRepository.findMaxFechaByCliente(idCliente);
        if (maxAbono.isPresent()) {
            return maxAbono.get();
        }
        List<Credito> activos = creditoRepository.findByClienteIdClienteAndEstadoOrderByFechaCreacionDesc(
                idCliente, EstadoCredito.ACTIVO);
        return activos.stream()
                .map(Credito::getFechaCreacion)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }
}