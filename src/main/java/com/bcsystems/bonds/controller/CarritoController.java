package com.bcsystems.bonds.controller;

import com.bcsystems.bonds.domain.Caja;
import com.bcsystems.bonds.domain.CarritoItemRapido;
import com.bcsystems.bonds.dto.CarritoItemRapidoRequest;
import com.bcsystems.bonds.dto.CarritoItemRapidoResponse;
import com.bcsystems.bonds.dto.ReservaProductoResponse;
import com.bcsystems.bonds.repository.CajaRepository;
import com.bcsystems.bonds.repository.CarritoItemRapidoRepository;
import com.bcsystems.bonds.service.ReservaProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final ReservaProductoService reservaProductoService;
    private final CarritoItemRapidoRepository carritoRapidoRepository;
    private final CajaRepository cajaRepository;

    public CarritoController(ReservaProductoService reservaProductoService,
                             CarritoItemRapidoRepository carritoRapidoRepository,
                             CajaRepository cajaRepository) {
        this.reservaProductoService = reservaProductoService;
        this.carritoRapidoRepository = carritoRapidoRepository;
        this.cajaRepository = cajaRepository;
    }

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregar(@RequestBody Map<String, Object> body) {
        Integer idCaja = Integer.valueOf(body.get("idCaja").toString());
        Integer idProducto = Integer.valueOf(body.get("idProducto").toString());
        Integer cantidad = body.get("cantidad") != null
                ? Integer.valueOf(body.get("cantidad").toString()) : 1;
        reservaProductoService.reservar(idCaja, idProducto, cantidad);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true));
    }

    @DeleteMapping("/quitar/{idProducto}")
    public ResponseEntity<Void> quitar(@PathVariable Integer idProducto,
                                        @RequestParam Integer idCaja) {
        reservaProductoService.quitarReserva(idCaja, idProducto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizar(@RequestBody Map<String, Object> body) {
        Integer idCaja = Integer.valueOf(body.get("idCaja").toString());
        Integer idProducto = Integer.valueOf(body.get("idProducto").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
        reservaProductoService.actualizarCantidad(idCaja, idProducto, cantidad);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiar(@RequestParam Integer idCaja) {
        reservaProductoService.limpiarReservas(idCaja);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservados/{idSucursal}")
    public ResponseEntity<List<ReservaProductoResponse>> reservados(
            @PathVariable Integer idSucursal) {
        return ResponseEntity.ok(
                reservaProductoService.obtenerReservasPorSucursal(idSucursal));
    }

    @GetMapping("/rapidos/{idCaja}")
    public ResponseEntity<List<CarritoItemRapidoResponse>> listarRapidos(
            @PathVariable Integer idCaja) {
        List<CarritoItemRapidoResponse> items = carritoRapidoRepository.findByCajaIdCaja(idCaja)
                .stream().map(r -> new CarritoItemRapidoResponse(
                        r.getIdItemRapido(),
                        r.getCaja().getIdCaja(),
                        r.getDescripcion(),
                        r.getPrecioVenta(),
                        r.getPrecioCompra(),
                        r.getCantidad()
                )).toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/rapidos")
    public ResponseEntity<CarritoItemRapidoResponse> agregarRapido(
            @RequestBody CarritoItemRapidoRequest request) {
        Caja caja = cajaRepository.findById(request.idCaja())
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        CarritoItemRapido item = CarritoItemRapido.builder()
                .caja(caja)
                .descripcion(request.descripcion())
                .precioVenta(request.precioVenta())
                .precioCompra(request.precioCompra())
                .cantidad(request.cantidad() != null ? request.cantidad() : 1)
                .fechaAgregado(LocalDateTime.now())
                .build();
        item = carritoRapidoRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CarritoItemRapidoResponse(
                item.getIdItemRapido(),
                item.getCaja().getIdCaja(),
                item.getDescripcion(),
                item.getPrecioVenta(),
                item.getPrecioCompra(),
                item.getCantidad()
        ));
    }

    @DeleteMapping("/rapidos/limpiar")
    public ResponseEntity<Void> limpiarRapidos(@RequestParam Integer idCaja) {
        carritoRapidoRepository.deleteByCajaIdCajaNative(idCaja);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rapidos/{id}")
    public ResponseEntity<Void> actualizarRapido(
            @PathVariable Integer id,
            @RequestBody CarritoItemRapidoRequest request) {
        CarritoItemRapido item = carritoRapidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        item.setCantidad(request.cantidad());
        if (request.precioVenta() != null) item.setPrecioVenta(request.precioVenta());
        if (request.precioCompra() != null) item.setPrecioCompra(request.precioCompra());
        carritoRapidoRepository.save(item);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rapidos/{id}")
    public ResponseEntity<Void> eliminarRapido(@PathVariable Integer id) {
        carritoRapidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
