package com.bcsystems.bonds.util;

import com.bcsystems.bonds.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodigoGeneratorService {

    private final ProductoRepository productoRepository;

    public CodigoGeneratorService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public String sanitizar(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    public String generarSku() {
        long count = productoRepository.count();
        String prefix = "BOND";
        String number = String.format("%05d", count + 1);
        String sku = prefix + number;

        while (productoRepository.findBySkuIgnoreCase(sku).isPresent()) {
            count++;
            number = String.format("%05d", count);
            sku = prefix + number;
        }

        return sku;
    }

    public String generarSkuVariante(String parentSku, List<String> codigosValores) {
        String base = sanitizar(parentSku);
        String suffix = codigosValores.stream()
                .map(this::sanitizar)
                .collect(java.util.stream.Collectors.joining());
        String sku = base + suffix;

        if (sku.length() > 50) {
            sku = sku.substring(0, 50);
        }

        int counter = 0;
        String baseSku = sku;
        while (productoRepository.findBySkuIgnoreCase(sku).isPresent()) {
            counter++;
            String suffixCounter = String.valueOf(counter);
            String trimmed = baseSku;
            if (trimmed.length() + suffixCounter.length() > 50) {
                trimmed = trimmed.substring(0, 50 - suffixCounter.length());
            }
            sku = trimmed + suffixCounter;
        }

        return sku;
    }
}
