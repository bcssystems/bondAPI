package com.bcsystems.bonds.config;

import com.bcsystems.bonds.domain.Permiso;
import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.domain.Rol;
import com.bcsystems.bonds.repository.PermisoRepository;
import com.bcsystems.bonds.repository.PersonaRepository;
import com.bcsystems.bonds.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataSeedRunner implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeedRunner(PermisoRepository permisoRepository,
                          RolRepository rolRepository,
                          PersonaRepository personaRepository,
                          PasswordEncoder passwordEncoder) {
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedPermisosYRoles();
        migrarPersonasRol();
        seedAdminInicial();
    }

    private void seedPermisosYRoles() {
        Map<String, List<String[]>> permisosPorModulo = buildCatalogo();

        Map<String, Permiso> permisoMap = new HashMap<>();
        if (permisoRepository.count() == 0) {
            for (var entry : permisosPorModulo.entrySet()) {
                String modulo = entry.getKey();
                for (String[] perm : entry.getValue()) {
                    Permiso p = Permiso.builder()
                            .clave(perm[0])
                            .nombre(perm[1])
                            .descripcion(perm[1] + " en " + modulo)
                            .modulo(modulo)
                            .activo(true)
                            .build();
                    p = permisoRepository.save(p);
                    permisoMap.put(p.getClave(), p);
                }
            }
        } else {
            permisoRepository.findAll().forEach(p -> permisoMap.put(p.getClave(), p));
        }

        Set<Permiso> todosLosPermisos = new HashSet<>(permisoMap.values());

        Map<String, Set<Permiso>> permisosPorRol = new LinkedHashMap<>();
        permisosPorRol.put("ADMINISTRADOR", todosLosPermisos);
        permisosPorRol.put("SISTEMAS", todosLosPermisos);
        permisosPorRol.put("AUDITORIAS", extraerPermisos(permisosPorModulo, permisoMap,
                List.of("KARDEX", "AUDITORIAS", "HISTORIAL_VENTAS", "CORTES", "GASTOS")));
        permisosPorRol.put("USUARIO", extraerPermisos(permisosPorModulo, permisoMap, List.of(), List.of(
                "VENTAS_VER", "VENTAS_CREAR",
                "CLIENTES_VER", "CLIENTES_CREAR",
                "COTIZACIONES_VER", "COTIZACIONES_CREAR", "COTIZACIONES_CONVERTIR",
                "GASTOS_VER", "GASTOS_CREAR",
                "CREDITOS_VER", "CREDITOS_ABONAR",
                "CAJAS_VER", "CAJAS_APERTURA", "CAJAS_CIERRE", "CAJAS_CORTE", "CAJAS_MOVIMIENTO",
                "HISTORIAL_VENTAS_VER",
                "PRODUCTOS_VER",
                "SUCURSALES_VER",
                "TIPOS_PAGO_VER",
                "CATEGORIAS_VER",
                "PROVEEDORES_VER",
                "KARDEX_VER",
                "MOVIMIENTOS_INVENTARIO_VER",
                "CAJA_CHICA_VER", "CAJA_CHICA_CREAR",
                "CANCELACIONES_VER",
                "PROMOCIONES_VER",
                "CONFIGURACION_VER")));

        for (var entry : permisosPorRol.entrySet()) {
            String nombre = entry.getKey();
            Set<Permiso> permisos = entry.getValue();
            if (rolRepository.existsByNombreIgnoreCase(nombre)) {
                continue;
            }
            Rol rol = Rol.builder()
                    .nombre(nombre)
                    .descripcion("Rol de sistema")
                    .esSistema(true)
                    .activo(true)
                    .permisos(permisos.isEmpty() ? new ArrayList<>(todosLosPermisos) : new ArrayList<>(permisos))
                    .build();
            rolRepository.save(rol);
        }
    }

    private Set<Permiso> extraerPermisos(Map<String, List<String[]>> permisosPorModulo,
                                         Map<String, Permiso> permisoMap,
                                         List<String> modulos) {
        return extraerPermisos(permisosPorModulo, permisoMap, modulos, List.of());
    }

    private Set<Permiso> extraerPermisos(Map<String, List<String[]>> permisosPorModulo,
                                         Map<String, Permiso> permisoMap,
                                         List<String> modulos,
                                         List<String> claves) {
        Set<Permiso> result = new HashSet<>();
        for (var entry : permisosPorModulo.entrySet()) {
            if (modulos.contains(entry.getKey())) {
                for (String[] perm : entry.getValue()) {
                    Permiso p = permisoMap.get(perm[0]);
                    if (p != null) result.add(p);
                }
            }
        }
        for (String clave : claves) {
            Permiso p = permisoMap.get(clave);
            if (p != null) result.add(p);
        }
        return result;
    }

    private void migrarPersonasRol() {
        List<Persona> pendientes = personaRepository.findPendientesMigracionRol();
        if (pendientes.isEmpty()) return;

        Rol rol = rolRepository.findByNombreIgnoreCase("USUARIO").orElse(null);
        if (rol == null) return;

        for (Persona persona : pendientes) {
            persona.setRol(rol);
            personaRepository.save(persona);
        }
    }

    private void seedAdminInicial() {
        if (personaRepository.count() > 0) return;
        if (!rolRepository.existsByNombreIgnoreCase("ADMINISTRADOR")) return;

        Rol admin = rolRepository.findByNombreIgnoreCase("ADMINISTRADOR").orElse(null);
        if (admin == null) return;

        Persona persona = Persona.builder()
                .nombre("Administrador")
                .apellido("Sistema")
                .usuario("admin")
                .password(passwordEncoder.encode("admin123"))
                .rol(admin)
                .activa(true)
                .build();
        personaRepository.save(persona);
    }

    private Map<String, List<String[]>> buildCatalogo() {
        Map<String, List<String[]>> map = new LinkedHashMap<>();
        map.put("VENTAS", List.of(
                new String[]{"VENTAS_VER", "Ver Ventas"},
                new String[]{"VENTAS_CREAR", "Crear Ventas"},
                new String[]{"VENTAS_CANCELAR", "Cancelar Ventas"}
        ));
        map.put("CANCELACIONES", List.of(
                new String[]{"CANCELACIONES_VER", "Ver Cancelaciones"},
                new String[]{"CANCELACIONES_AUTORIZAR", "Autorizar Cancelaciones"}
        ));
        map.put("HISTORIAL_VENTAS", List.<String[]>of(
                new String[]{"HISTORIAL_VENTAS_VER", "Ver Historial de Ventas"}
        ));
        map.put("CAJA_CHICA", List.of(
                new String[]{"CAJA_CHICA_VER", "Ver Caja Chica"},
                new String[]{"CAJA_CHICA_CREAR", "Registrar Caja Chica"}
        ));
        map.put("GASTOS", List.of(
                new String[]{"GASTOS_VER", "Ver Gastos"},
                new String[]{"GASTOS_CREAR", "Solicitar Gastos"},
                new String[]{"GASTOS_AUTORIZAR", "Autorizar Gastos"},
                new String[]{"GASTOS_RECHAZAR", "Rechazar Gastos"}
        ));
        map.put("COTIZACIONES", List.of(
                new String[]{"COTIZACIONES_VER", "Ver Cotizaciones"},
                new String[]{"COTIZACIONES_CREAR", "Crear Cotizaciones"},
                new String[]{"COTIZACIONES_CANCELAR", "Cancelar Cotizaciones"},
                new String[]{"COTIZACIONES_CONVERTIR", "Convertir a Venta"}
        ));
        map.put("CORTES", List.of(
                new String[]{"CORTES_VER", "Ver Cortes"},
                new String[]{"CORTES_EDITAR", "Editar Cortes"}
        ));
        map.put("PRODUCTOS", List.of(
                new String[]{"PRODUCTOS_VER", "Ver Productos"},
                new String[]{"PRODUCTOS_CREAR", "Crear Productos"},
                new String[]{"PRODUCTOS_EDITAR", "Editar Productos"},
                new String[]{"PRODUCTOS_ELIMINAR", "Eliminar Productos"},
                new String[]{"PRODUCTOS_MOVIMIENTO", "Movimientos de Stock"}
        ));
        map.put("RECEPCIONES", List.of(
                new String[]{"RECEPCIONES_VER", "Ver Recepciones"},
                new String[]{"RECEPCIONES_CREAR", "Crear Recepciones"},
                new String[]{"RECEPCIONES_ELIMINAR", "Eliminar Recepciones"}
        ));
        map.put("MOVIMIENTOS_INVENTARIO", List.<String[]>of(
                new String[]{"MOVIMIENTOS_INVENTARIO_VER", "Ver Movimientos de Inventario"}
        ));
        map.put("CLIENTES", List.of(
                new String[]{"CLIENTES_VER", "Ver Clientes"},
                new String[]{"CLIENTES_CREAR", "Crear Clientes"},
                new String[]{"CLIENTES_EDITAR", "Editar Clientes"},
                new String[]{"CLIENTES_ELIMINAR", "Eliminar Clientes"}
        ));
        map.put("CREDITOS", List.of(
                new String[]{"CREDITOS_VER", "Ver Creditos"},
                new String[]{"CREDITOS_ABONAR", "Registrar Abonos"}
        ));
        map.put("PRECIOS_CLIENTE", List.of(
                new String[]{"PRECIOS_CLIENTE_VER", "Ver Precios Cliente"},
                new String[]{"PRECIOS_CLIENTE_EDITAR", "Editar Precios Cliente"}
        ));
        map.put("SUCURSALES", List.of(
                new String[]{"SUCURSALES_VER", "Ver Sucursales"},
                new String[]{"SUCURSALES_CREAR", "Crear Sucursales"},
                new String[]{"SUCURSALES_EDITAR", "Editar Sucursales"},
                new String[]{"SUCURSALES_ELIMINAR", "Eliminar Sucursales"}
        ));
        map.put("TIPOS_PAGO", List.of(
                new String[]{"TIPOS_PAGO_VER", "Ver Tipos de Pago"},
                new String[]{"TIPOS_PAGO_CREAR", "Crear Tipos de Pago"},
                new String[]{"TIPOS_PAGO_EDITAR", "Editar Tipos de Pago"},
                new String[]{"TIPOS_PAGO_ELIMINAR", "Eliminar Tipos de Pago"}
        ));
        map.put("CATEGORIAS", List.of(
                new String[]{"CATEGORIAS_VER", "Ver Categorias"},
                new String[]{"CATEGORIAS_CREAR", "Crear Categorias"},
                new String[]{"CATEGORIAS_EDITAR", "Editar Categorias"},
                new String[]{"CATEGORIAS_ELIMINAR", "Eliminar Categorias"}
        ));
        map.put("PROVEEDORES", List.of(
                new String[]{"PROVEEDORES_VER", "Ver Proveedores"},
                new String[]{"PROVEEDORES_CREAR", "Crear Proveedores"},
                new String[]{"PROVEEDORES_EDITAR", "Editar Proveedores"},
                new String[]{"PROVEEDORES_ELIMINAR", "Eliminar Proveedores"}
        ));
        map.put("KARDEX", List.<String[]>of(
                new String[]{"KARDEX_VER", "Ver Kardex"}
        ));
        map.put("PERSONAS", List.of(
                new String[]{"PERSONAS_VER", "Ver Usuarios"},
                new String[]{"PERSONAS_CREAR", "Crear Usuarios"},
                new String[]{"PERSONAS_EDITAR", "Editar Usuarios"},
                new String[]{"PERSONAS_ELIMINAR", "Eliminar Usuarios"}
        ));
        map.put("CAJAS", List.of(
                new String[]{"CAJAS_VER", "Ver Cajas"},
                new String[]{"CAJAS_CREAR", "Crear Cajas"},
                new String[]{"CAJAS_EDITAR", "Editar Cajas"},
                new String[]{"CAJAS_ELIMINAR", "Eliminar Cajas"},
                new String[]{"CAJAS_APERTURA", "Abrir Cajas"},
                new String[]{"CAJAS_CIERRE", "Cerrar Cajas"},
                new String[]{"CAJAS_MOVIMIENTO", "Movimientos de Caja"},
                new String[]{"CAJAS_CORTE", "Realizar Cortes de Caja"}
        ));
        map.put("PROMOCIONES", List.of(
                new String[]{"PROMOCIONES_VER", "Ver Promociones"},
                new String[]{"PROMOCIONES_CREAR", "Crear Promociones"},
                new String[]{"PROMOCIONES_EDITAR", "Editar Promociones"},
                new String[]{"PROMOCIONES_ELIMINAR", "Eliminar Promociones"}
        ));
        map.put("CONFIGURACION", List.of(
                new String[]{"CONFIGURACION_VER", "Ver Configuracion"},
                new String[]{"CONFIGURACION_EDITAR", "Editar Configuracion"}
        ));
        map.put("AUDITORIAS", List.<String[]>of(
                new String[]{"AUDITORIAS_VER", "Ver Auditorias"}
        ));
        map.put("ROLES", List.of(
                new String[]{"ROLES_VER", "Ver Roles"},
                new String[]{"ROLES_CREAR", "Crear Roles"},
                new String[]{"ROLES_EDITAR", "Editar Roles"},
                new String[]{"ROLES_ELIMINAR", "Eliminar Roles"}
        ));
        return map;
    }
}