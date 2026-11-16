package pe.com.upeu.PharmaBackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.upeu.PharmaBackend.dto.reporte.ProductoMasVendidoDTO;
import pe.com.upeu.PharmaBackend.dto.reporte.VentaPorCategoriaDTO;
import pe.com.upeu.PharmaBackend.entity.Venta;
import pe.com.upeu.PharmaBackend.enums.EstadoVenta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @EntityGraph(attributePaths = {"cliente"})
    @Query(value = """
            select v
            from Venta v
            where (:clienteId is null or v.cliente.id = :clienteId)
              and (:estado is null or v.estado = :estado)
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha < :hasta)
            """,
            countQuery = """
            select count(v)
            from Venta v
            where (:clienteId is null or v.cliente.id = :clienteId)
              and (:estado is null or v.estado = :estado)
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha < :hasta)
            """)
    Page<Venta> buscar(
            @Param("clienteId") Long clienteId,
            @Param("estado") EstadoVenta estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    @Query("""
            select distinct v
            from Venta v
            left join fetch v.cliente c
            left join fetch v.detalles d
            left join fetch d.producto p
            where v.id in :ids
            """)
    List<Venta> cargarConDetalles(@Param("ids") List<Long> ids);

    @Query("""
            select new pe.com.upeu.PharmaBackend.dto.reporte.VentaPorCategoriaDTO(
                       cat.id,
                       cat.nombre,
                       sum(d.cantidad),
                       sum(d.subtotal))
            from DetalleVenta d
            join d.venta v
            join d.producto p
            join p.categoria cat
            where v.estado = pe.com.upeu.PharmaBackend.enums.EstadoVenta.REGISTRADA
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha <= :hasta)
            group by cat.id, cat.nombre
            order by sum(d.subtotal) desc
            """)
    List<VentaPorCategoriaDTO> reporteVentasPorCategoria(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
            select new pe.com.upeu.PharmaBackend.dto.reporte.ProductoMasVendidoDTO(
                       p.id,
                       p.nombre,
                       cat.nombre,
                       sum(d.cantidad),
                       sum(d.subtotal))
            from DetalleVenta d
            join d.venta v
            join d.producto p
            join p.categoria cat
            where v.estado = pe.com.upeu.PharmaBackend.enums.EstadoVenta.REGISTRADA
              and (:desde is null or v.fecha >= :desde)
              and (:hasta is null or v.fecha <= :hasta)
            group by p.id, p.nombre, cat.nombre
            order by sum(d.cantidad) desc
            """)
    List<ProductoMasVendidoDTO> reporteProductosMasVendidos(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
