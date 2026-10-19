package pe.com.upeu.PharmaBackend.mapper;

import org.springframework.stereotype.Component;
import pe.com.upeu.PharmaBackend.dto.DetalleVentaResponseDTO;
import pe.com.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.com.upeu.PharmaBackend.entity.DetalleVenta;
import pe.com.upeu.PharmaBackend.entity.Venta;

import java.util.List;

/*
 * Requiere que cliente, detalles y detalles.producto ya estén cargados
 * (fetch join / entity graph); no dispara consultas por su cuenta.
 */
@Component
public class VentaMapper {

    public VentaResponseDTO toResponse(Venta venta) {

        List<DetalleVentaResponseDTO> detalles =
                venta.getDetalles()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        String clienteNombre =
                venta.getCliente().getNombres()
                        + " "
                        + venta.getCliente().getApellidos();

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

    public DetalleVentaResponseDTO toResponse(DetalleVenta detalle) {
        return new DetalleVentaResponseDTO(
                detalle.getProducto().getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecio(),
                detalle.getSubtotal()
        );
    }
}