package pe.com.upeu.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.upeu.PharmaBackend.dto.DetalleVentaResponseDTO;
import pe.com.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.com.upeu.PharmaBackend.enums.EstadoVenta;
import pe.com.upeu.PharmaBackend.exception.RecursoNoEncontradoException;
import pe.com.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.com.upeu.PharmaBackend.entity.Venta;
import pe.com.upeu.PharmaBackend.repository.ClienteRepository;
import pe.com.upeu.PharmaBackend.repository.ProductoRepository;
import pe.com.upeu.PharmaBackend.repository.VentaRepository;
import pe.com.upeu.PharmaBackend.service.service.VentaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "fecha", "total", "estado");
    private static final String ORDEN_POR_DEFECTO = "fecha";

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));
        return convertirResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listar() {
        return ventaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscar(
            Long clienteId,
            EstadoVenta estado,
            LocalDateTime desde,
            LocalDateTime hasta,
            String ordenarPor,
            String direccion) {

        long inicio = System.currentTimeMillis();
        log.info("Inicio buscar ventas | clienteId={} | estado={} | desde={} | hasta={} | ordenarPor={} | direccion={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion);

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }

        String campoOrden = (ordenarPor != null && !ordenarPor.isBlank()) ? ordenarPor : ORDEN_POR_DEFECTO;
        if (!CAMPOS_ORDENABLES.contains(campoOrden)) {
            throw new ReglaNegocioException("Campo de ordenamiento no permitido: " + campoOrden);
        }

        Sort sort = "desc".equalsIgnoreCase(direccion)
                ? Sort.by(campoOrden).descending()
                : Sort.by(campoOrden).ascending();

        List<VentaResponseDTO> resultado = ventaRepository.buscar(clienteId, estado, desde, hasta, sort)
                .stream()
                .map(this::convertirResponse)
                .toList();

        long duracion = System.currentTimeMillis() - inicio;
        log.info("Fin buscar ventas | filas={} | duracion={} ms", resultado.size(), duracion);

        return resultado;
    }

    private VentaResponseDTO convertirResponse(Venta venta) {
        List<DetalleVentaResponseDTO> detalles = venta.getDetalles()
                .stream()
                .map(detalle -> new DetalleVentaResponseDTO(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecio(),
                        detalle.getSubtotal()
                )).toList();

        String clienteNombre = venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getCliente().getId(),
                clienteNombre,
                venta.getEstado().name(),
                venta.getTotal(),
                detalles
        );
    }
}