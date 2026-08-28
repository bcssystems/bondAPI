package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.AccionAuditoria;
import com.bcsystems.bonds.domain.en.TipoMovimiento;
import com.bcsystems.bonds.domain.en.TipoMultimedia;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.ProductoService;
import com.bcsystems.bonds.util.CodigoGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository multimediaRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final CodigoGeneratorService codigoGenerator;
    private final AuditoriaService auditoriaService;

    @Value("${app.upload.dir:./uploads/multimedia}")
    private String uploadDir;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProductoMultimediaRepository multimediaRepository,
                               SucursalRepository sucursalRepository,
                               InventarioSucursalRepository inventarioSucursalRepository,
                               MovimientoStockRepository movimientoStockRepository,
                               CodigoGeneratorService codigoGenerator,
                               AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.multimediaRepository = multimediaRepository;
        this.sucursalRepository = sucursalRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.codigoGenerator = codigoGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public Page<ProductoResponse> listar(String search, Boolean activo, Integer idSucursal, Pageable pageable) {
        return productoRepository.buscarConFiltros(search, activo, idSucursal, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductoVentaResponse> listarParaVenta(String search, Integer idSucursal, Pageable pageable) {
        return productoRepository.buscarParaVenta(search, idSucursal, pageable)
                .map(this::toVentaResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Transactional
    @Override
    public ProductoResponse crear(ProductoRequest request) {
        String usuario = obtenerUsuarioActual();
        return crearProductoSimple(request, usuario);
    }

    private ProductoResponse crearProductoSimple(ProductoRequest request, String usuario) {
        String sku = request.sku();
        if (sku == null || sku.isBlank()) {
            sku = codigoGenerator.generarSku();
        } else if (productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new InvalidEntryException("Ya existe un producto con el SKU: " + sku);
        }

        int stockTotal = 0;
        int minTotal = 0;
        int maxTotal = 0;
        if (request.inventarios() != null) {
            for (var invReq : request.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;
            }
        }

        Producto producto = Producto.builder()
                .sku(sku)
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precioBase(request.precioBase())
                .costoPromedio(request.costoPromedio())
                .unidadMedida(request.unidadMedida() != null ? request.unidadMedida() : "UNIDAD")
                .metrosPorRollo(request.metrosPorRollo())
                .stockActual(stockTotal)
                .stockMinimo(minTotal)
                .stockMaximo(maxTotal)
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        producto = productoRepository.save(producto);
        guardarInventarios(producto, request.inventarios(), usuario);
        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.CREACION.name(), usuario,
                "Se cre\u00f3 el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    private void guardarInventarios(Producto producto, List<InventarioSucursalRequest> inventarios, String usuario) {
        if (inventarios == null) return;

        for (var invReq : inventarios) {
            Sucursal sucursal = sucursalRepository.findById(invReq.idSucursal())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

            InventarioSucursal inventario = InventarioSucursal.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .stock(invReq.stock() != null ? invReq.stock() : 0)
                    .stockMinimo(invReq.stockMinimo())
                    .stockMaximo(invReq.stockMaximo())
                    .build();
            inventarioSucursalRepository.save(inventario);

            if (invReq.stock() != null && invReq.stock() > 0) {
                MovimientoStock movimiento = MovimientoStock.builder()
                        .producto(producto)
                        .sucursal(sucursal)
                        .tipoMovimiento(TipoMovimiento.ENTRADA)
                        .cantidad(invReq.stock())
                        .stockAnterior(0)
                        .stockNuevo(invReq.stock())
                        .referencia("Stock inicial")
                        .usuario(usuario)
                        .observacion("Stock inicial en sucursal: " + sucursal.getNombre())
                        .build();
                movimientoStockRepository.save(movimiento);

                auditoriaService.registrarMovimiento("PRODUCTO", producto.getIdProducto(), "ENTRADA", usuario,
                        "Stock inicial de " + invReq.stock() + " unidades en " + sucursal.getNombre(),
                        "Stock inicial", invReq.stock(), 0, invReq.stock());
            }
        }
    }

    @Transactional
    @Override
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        String usuario = obtenerUsuarioActual();

        Producto producto = buscarOExcepcion(id);

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecioBase(request.precioBase());
        if (request.costoPromedio() != null) producto.setCostoPromedio(request.costoPromedio());
        if (request.unidadMedida() != null) producto.setUnidadMedida(request.unidadMedida());
        if (request.metrosPorRollo() != null) producto.setMetrosPorRollo(request.metrosPorRollo());
        if (request.activo() != null) producto.setActivo(request.activo());

        if (request.inventarios() != null) {
            int stockTotal = 0;
            int minTotal = 0;
            int maxTotal = 0;
            for (var invReq : request.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;

                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(id, invReq.idSucursal())
                        .orElse(InventarioSucursal.builder()
                                .producto(producto)
                                .sucursal(sucursalRepository.findById(invReq.idSucursal())
                                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada")))
                                .stock(0)
                                .build());
                inv.setStock(invReq.stock() != null ? invReq.stock() : 0);
                inv.setStockMinimo(invReq.stockMinimo());
                inv.setStockMaximo(invReq.stockMaximo());
                inventarioSucursalRepository.save(inv);
            }
            producto.setStockActual(stockTotal);
            producto.setStockMinimo(minTotal);
            producto.setStockMaximo(maxTotal);
        }

        producto = productoRepository.save(producto);

        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualiz\u00f3 el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Producto producto = buscarOExcepcion(id);

        if (producto.getStockActual() != null && producto.getStockActual() > 0) {
            throw new InvalidEntryException("No se puede desactivar un producto con stock mayor a 0");
        }

        List<InventarioSucursal> inventarios = inventarioSucursalRepository.findByProductoIdProducto(id);
        for (InventarioSucursal inv : inventarios) {
            if (inv.getStock() != null && inv.getStock() > 0) {
                throw new InvalidEntryException("No se puede desactivar: stock en sucursal " +
                        (inv.getSucursal() != null ? inv.getSucursal().getNombre() : "") + " es " + inv.getStock());
            }
        }

        producto.setActivo(false);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se elimin\u00f3 (desactiv\u00f3) el producto: " + producto.getNombre());
    }

    @Transactional
    @Override
    public void reactivar(Integer id) {
        Producto producto = buscarOExcepcion(id);
        producto.setActivo(true);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", id, AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se reactiv\u00f3 el producto: " + producto.getNombre());
    }

    @Transactional
    @Override
    public ProductoResponse agregarMultimedia(Integer idProducto, MultipartFile archivo, Boolean esPrincipal) {
        Producto producto = buscarOExcepcion(idProducto);

        if (archivo.isEmpty()) {
            throw new InvalidEntryException("El archivo est\u00e1 vac\u00edo");
        }

        String contentType = archivo.getContentType();
        TipoMultimedia tipo;
        if (contentType != null && contentType.startsWith("video")) {
            tipo = TipoMultimedia.VIDEO;
        } else if (contentType != null && contentType.startsWith("image")) {
            tipo = TipoMultimedia.IMAGEN;
        } else {
            throw new InvalidEntryException("El archivo debe ser una imagen o video");
        }

        try {
            String extension = Objects.requireNonNull(archivo.getOriginalFilename())
                    .substring(archivo.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            if (Boolean.TRUE.equals(esPrincipal)) {
                multimediaRepository.findByProductoIdProductoAndEsPrincipalTrue(idProducto)
                        .ifPresent(m -> {
                            m.setEsPrincipal(false);
                            multimediaRepository.save(m);
                        });
            }

            ProductoMultimedia multimedia = ProductoMultimedia.builder()
                    .producto(producto)
                    .tipo(tipo)
                    .url("/uploads/" + nombreArchivo)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .esPrincipal(esPrincipal != null && esPrincipal)
                    .build();

            multimediaRepository.save(multimedia);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }

        return toResponse(productoRepository.findById(idProducto).orElseThrow());
    }

    @Transactional
    @Override
    public void eliminarMultimedia(Integer idMultimedia) {
        ProductoMultimedia multimedia = multimediaRepository.findById(idMultimedia)
                .orElseThrow(() -> new NotFoundException("Multimedia no encontrada"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(
                    multimedia.getUrl().replace("/uploads/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
        }

        multimediaRepository.delete(multimedia);
    }

    @Transactional
    @Override
    public ProductoResponse marcarMultimediaPrincipal(Integer idProducto, Integer idMultimedia) {
        multimediaRepository.findByProductoIdProductoAndEsPrincipalTrue(idProducto)
                .ifPresent(m -> {
                    m.setEsPrincipal(false);
                    multimediaRepository.save(m);
                });

        ProductoMultimedia multimedia = multimediaRepository.findById(idMultimedia)
                .orElseThrow(() -> new NotFoundException("Multimedia no encontrada"));
        multimedia.setEsPrincipal(true);
        multimediaRepository.save(multimedia);

        return toResponse(buscarOExcepcion(idProducto));
    }

    @Transactional
    @Override
    public ProductoResponse actualizarStockSucursal(Integer idProducto, Integer idSucursal, Integer nuevoStock) {
        Producto producto = buscarOExcepcion(idProducto);
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioSucursal inventario = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursal)
                .orElse(InventarioSucursal.builder()
                        .producto(producto)
                        .sucursal(sucursal)
                        .stock(0)
                        .build());

        int diferencia = nuevoStock - inventario.getStock();
        inventario.setStock(nuevoStock);
        inventarioSucursalRepository.save(inventario);

        int stockAnteriorGlobal = producto.getStockActual();
        producto.setStockActual(stockAnteriorGlobal + diferencia);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        TipoMovimiento tipo = diferencia >= 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(tipo)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnteriorGlobal)
                .stockNuevo(producto.getStockActual())
                .referencia("Ajuste manual")
                .usuario(usuario)
                .observacion("Ajuste de stock en sucursal: " + sucursal.getNombre())
                .build();
        movimientoStockRepository.save(movimiento);

        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, tipo.name(), usuario,
                "Ajuste de stock en " + sucursal.getNombre() + ": " + diferencia,
                "Ajuste manual", Math.abs(diferencia), stockAnteriorGlobal, producto.getStockActual());

        return toResponse(productoRepository.findById(idProducto).orElseThrow());
    }

    @Transactional
    @Override
    public ProductoResponse registrarMovimientoStock(Integer idProducto, MovimientoStockRequest request) {
        Producto producto = buscarOExcepcion(idProducto);

        Sucursal sucursal = null;
        if (request.idSucursal() != null) {
            sucursal = sucursalRepository.findById(request.idSucursal())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        int stockAnteriorGlobal = producto.getStockActual();
        int nuevoStockGlobal;
        int cantidad = request.cantidad();
        int stockAnteriorSuc = 0;
        int nuevoStockSuc = 0;
        boolean tieneSucursal = sucursal != null;

        if (tieneSucursal) {
            InventarioSucursal inventario = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursal())
                    .orElse(InventarioSucursal.builder()
                            .producto(producto)
                            .sucursal(sucursal)
                            .stock(0)
                            .build());
            stockAnteriorSuc = inventario.getStock();

            switch (request.tipoMovimiento()) {
                case ENTRADA -> {
                    nuevoStockSuc = stockAnteriorSuc + cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal + cantidad;
                }
                case SALIDA -> {
                    if (stockAnteriorSuc < cantidad) {
                        throw new InvalidEntryException("Stock insuficiente en sucursal. Actual: " + stockAnteriorSuc +
                                ", solicitado: " + cantidad);
                    }
                    nuevoStockSuc = stockAnteriorSuc - cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal - cantidad;
                }
                case AJUSTE -> {
                    int diff = cantidad - stockAnteriorSuc;
                    nuevoStockSuc = cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal + diff;
                }
                default -> nuevoStockGlobal = stockAnteriorGlobal;
            }

            inventario.setStock(nuevoStockSuc);
            inventarioSucursalRepository.save(inventario);
        } else {
            switch (request.tipoMovimiento()) {
                case ENTRADA -> nuevoStockGlobal = stockAnteriorGlobal + cantidad;
                case SALIDA -> {
                    if (stockAnteriorGlobal < cantidad) {
                        throw new InvalidEntryException("Stock insuficiente. Actual: " + stockAnteriorGlobal +
                                ", solicitado: " + cantidad);
                    }
                    nuevoStockGlobal = stockAnteriorGlobal - cantidad;
                }
                case AJUSTE -> nuevoStockGlobal = cantidad;
                default -> nuevoStockGlobal = stockAnteriorGlobal;
            }
        }

        producto.setStockActual(nuevoStockGlobal);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(request.tipoMovimiento())
                .cantidad(cantidad)
                .stockAnterior(tieneSucursal ? stockAnteriorSuc : stockAnteriorGlobal)
                .stockNuevo(tieneSucursal ? nuevoStockSuc : nuevoStockGlobal)
                .referencia(request.referencia())
                .usuario(usuario)
                .observacion(request.observacion())
                .build();
        movimientoStockRepository.save(movimiento);

        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, request.tipoMovimiento().name(), usuario,
                request.tipoMovimiento() + " de " + cantidad + " unidades" +
                        (sucursal != null ? " en " + sucursal.getNombre() : ""),
                request.referencia(), cantidad, stockAnteriorGlobal, nuevoStockGlobal);

        return toResponse(producto);
    }

    @Transactional
    @Override
    public ProductoResponse transferirStock(Integer idProducto, TransferenciaRequest request) {
        Producto producto = buscarOExcepcion(idProducto);
        Sucursal origen = sucursalRepository.findById(request.idSucursalOrigen())
                .orElseThrow(() -> new NotFoundException("Sucursal origen no encontrada"));
        Sucursal destino = sucursalRepository.findById(request.idSucursalDestino())
                .orElseThrow(() -> new NotFoundException("Sucursal destino no encontrada"));

        if (origen.getIdSucursal().equals(destino.getIdSucursal())) {
            throw new InvalidEntryException("La sucursal origen y destino deben ser diferentes");
        }

        int cantidad = request.cantidad();

        InventarioSucursal invOrigen = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursalOrigen())
                .orElseThrow(() -> new InvalidEntryException("Producto sin inventario en sucursal origen"));

        if (invOrigen.getStock() < cantidad) {
            throw new InvalidEntryException("Stock insuficiente en sucursal origen. Actual: " + invOrigen.getStock() +
                    ", solicitado: " + cantidad);
        }

        InventarioSucursal invDestino = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursalDestino())
                .orElse(InventarioSucursal.builder()
                        .producto(producto)
                        .sucursal(destino)
                        .stock(0)
                        .build());

        int stockOrigenAntes = invOrigen.getStock();
        int stockDestinoAntes = invDestino.getStock();

        invOrigen.setStock(stockOrigenAntes - cantidad);
        invDestino.setStock(stockDestinoAntes + cantidad);

        inventarioSucursalRepository.save(invOrigen);
        inventarioSucursalRepository.save(invDestino);

        String usuario = obtenerUsuarioActual();
        String referencia = request.referencia();
        String observacion = request.observacion();

        MovimientoStock movOrigen = MovimientoStock.builder()
                .producto(producto)
                .sucursal(origen)
                .tipoMovimiento(TipoMovimiento.TRANSFERENCIA)
                .cantidad(cantidad)
                .stockAnterior(stockOrigenAntes)
                .stockNuevo(invOrigen.getStock())
                .referencia(referencia)
                .usuario(usuario)
                .observacion(observacion != null ? observacion : "Transferido a " + destino.getNombre())
                .build();
        movimientoStockRepository.save(movOrigen);

        MovimientoStock movDestino = MovimientoStock.builder()
                .producto(producto)
                .sucursal(destino)
                .tipoMovimiento(TipoMovimiento.TRANSFERENCIA)
                .cantidad(cantidad)
                .stockAnterior(stockDestinoAntes)
                .stockNuevo(invDestino.getStock())
                .referencia(referencia)
                .usuario(usuario)
                .observacion(observacion != null ? observacion : "Transferido desde " + origen.getNombre())
                .build();
        movimientoStockRepository.save(movDestino);

        int stockGlobal = producto.getStockActual();
        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, TipoMovimiento.TRANSFERENCIA.name(), usuario,
                "Transferencia de " + cantidad + " unidades de " + origen.getNombre() + " a " + destino.getNombre(),
                referencia, cantidad, stockGlobal, stockGlobal);

        return toResponse(producto);
    }

    @Override
    public ProductoStats obtenerStats() {
        long total = productoRepository.count();
        long activos = productoRepository.countByActivoTrue();
        Integer stockGlobal = productoRepository.sumStockActual();
        Integer stockMinimo = productoRepository.sumStockMinimo();
        Double costoTotal = productoRepository.sumCostoTotalInventario();
        return new ProductoStats(total, activos,
                stockGlobal != null ? stockGlobal : 0,
                stockMinimo != null ? stockMinimo : 0,
                costoTotal != null ? costoTotal : 0);
    }

    @Override
    public List<Map<String, Object>> costoPorSucursal() {
        List<Object[]> rows = inventarioSucursalRepository.sumCostoPorSucursal();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("sucursal", row[0]);
            map.put("costo", row[1]);
            result.add(map);
        }
        return result;
    }

    private Producto buscarOExcepcion(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private ProductoVentaResponse toVentaResponse(Producto p) {
        List<ProductoVentaResponse.MultimediaResponse> multimedia = p.getMultimedia().stream()
                .map(m -> new ProductoVentaResponse.MultimediaResponse(
                        m.getIdMultimedia(), m.getTipo().name(), m.getUrl(),
                        m.getNombreArchivo(), m.getEsPrincipal()))
                .collect(Collectors.toList());

        List<ProductoVentaResponse.InventarioSucursalResponse> inventario = p.getInventarioSucursales().stream()
                .map(i -> new ProductoVentaResponse.InventarioSucursalResponse(
                        i.getId(), i.getSucursal().getIdSucursal(),
                        i.getSucursal().getNombre(), i.getStock(),
                        i.getStockMinimo(), i.getStockMaximo()))
                .collect(Collectors.toList());

        return new ProductoVentaResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(),
                p.getPrecioBase(),
                p.getStockActual(), p.getCostoPromedio(),
                p.getActivo(), multimedia, inventario);
    }

    private ProductoResponse toResponse(Producto p) {
        List<ProductoResponse.MultimediaResponse> multimedia = p.getMultimedia().stream()
                .map(m -> new ProductoResponse.MultimediaResponse(
                        m.getIdMultimedia(), m.getTipo().name(), m.getUrl(),
                        m.getNombreArchivo(), m.getEsPrincipal()))
                .collect(Collectors.toList());

        List<ProductoResponse.InventarioSucursalResponse> inventario = p.getInventarioSucursales().stream()
                .map(i -> new ProductoResponse.InventarioSucursalResponse(
                        i.getId(), i.getSucursal().getIdSucursal(),
                        i.getSucursal().getNombre(), i.getStock(),
                        i.getStockMinimo(), i.getStockMaximo()))
                .collect(Collectors.toList());

        return new ProductoResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecioBase(),
                p.getStockActual(), p.getStockMinimo(), p.getStockMaximo(),
                p.getCostoPromedio(), p.getUnidadMedida(), p.getMetrosPorRollo(),
                p.getActivo(), p.getFechaCreacion(), p.getFechaActualizacion(),
                multimedia, inventario);
    }
}