package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.domain.en.*;
import com.bcsystems.bonds.dto.PaisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    @GetMapping("/caja-estados")
    public ResponseEntity<List<Map<String, String>>> cajaEstados() {
        var list = Arrays.stream(CajaEstado.values())
                .map(e -> Map.of("value", e.name(), "label", e.name()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tipo-venta")
    public ResponseEntity<List<Map<String, String>>> tipoVenta() {
        var list = Arrays.stream(TipoVenta.values())
                .map(e -> Map.of("value", e.name(), "label", e.name()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/estado-venta")
    public ResponseEntity<List<Map<String, String>>> estadoVenta() {
        var list = Arrays.stream(EstadoVenta.values())
                .map(e -> Map.of("value", e.name(), "label", e.name()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tipo-movimiento-caja")
    public ResponseEntity<List<Map<String, String>>> tipoMovimientoCaja() {
        var list = Arrays.stream(TipoMovimientoCaja.values())
                .map(e -> Map.of("value", e.name(), "label", e.name()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/estado-gasto")
    public ResponseEntity<List<Map<String, String>>> estadoGasto() {
        var list = Arrays.stream(EstadoGasto.values())
                .map(e -> Map.of("value", e.name(), "label", e.name()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/regimenes-fiscales")
    public ResponseEntity<List<Map<String, String>>> regimenesFiscales() {
        var list = List.of(
                Map.of("clave", "601", "descripcion", "General de Ley Personas Morales"),
                Map.of("clave", "603", "descripcion", "Personas Morales con Fines no Lucrativos"),
                Map.of("clave", "605", "descripcion", "Sueldos y Salarios e Ingresos Asimilados a Salarios"),
                Map.of("clave", "606", "descripcion", "Arrendamiento"),
                Map.of("clave", "607", "descripcion", "R\u00e9gimen de Enajenaci\u00f3n o Adquisici\u00f3n de Bienes"),
                Map.of("clave", "608", "descripcion", "Dem\u00e1s ingresos"),
                Map.of("clave", "609", "descripcion", "Consolidaci\u00f3n"),
                Map.of("clave", "610", "descripcion", "Residentes en el Extranjero sin Establecimiento Permanente en M\u00e9xico"),
                Map.of("clave", "611", "descripcion", "Ingresos por Dividendos (socios y accionistas)"),
                Map.of("clave", "612", "descripcion", "Personas F\u00edsicas con Actividades Empresariales y Profesionales"),
                Map.of("clave", "614", "descripcion", "Ingresos por intereses"),
                Map.of("clave", "615", "descripcion", "R\u00e9gimen de los ingresos por obtenci\u00f3n de premios"),
                Map.of("clave", "616", "descripcion", "Sin obligaciones fiscales"),
                Map.of("clave", "620", "descripcion", "Sociedades Cooperativas de Producci\u00f3n que optan por diferir sus ingresos"),
                Map.of("clave", "621", "descripcion", "R\u00e9gimen de Incorporaci\u00f3n Fiscal"),
                Map.of("clave", "622", "descripcion", "Actividades Agr\u00edcolas, Ganaderas, Silv\u00edcolas y Pesqueras"),
                Map.of("clave", "623", "descripcion", "Opcional para Grupos de Sociedades"),
                Map.of("clave", "624", "descripcion", "Coordinados"),
                Map.of("clave", "625", "descripcion", "R\u00e9gimen de las Actividades Empresariales con ingresos a trav\u00e9s de Plataformas Tecnol\u00f3gicas"),
                Map.of("clave", "626", "descripcion", "R\u00e9gimen Simplificado de Confianza")
        );
        return ResponseEntity.ok(list);
    }

    @GetMapping("/paises")
    public ResponseEntity<List<PaisResponse>> paises() {
        var list = List.of(
                new PaisResponse("MX", "M\u00e9xico", "+52"),
                new PaisResponse("US", "Estados Unidos", "+1"),
                new PaisResponse("GT", "Guatemala", "+502"),
                new PaisResponse("HN", "Honduras", "+504"),
                new PaisResponse("SV", "El Salvador", "+503"),
                new PaisResponse("NI", "Nicaragua", "+505"),
                new PaisResponse("CR", "Costa Rica", "+506"),
                new PaisResponse("PA", "Panam\u00e1", "+507"),
                new PaisResponse("CO", "Colombia", "+57"),
                new PaisResponse("ES", "Espa\u00f1a", "+34")
        );
        return ResponseEntity.ok(list);
    }
}
