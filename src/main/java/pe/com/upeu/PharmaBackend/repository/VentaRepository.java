package pe.com.upeu.PharmaBackend.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.upeu.PharmaBackend.entity.Venta;
import pe.com.upeu.PharmaBackend.enums.EstadoVenta;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
                SELECT DISTINCT v FROM Venta v
                LEFT JOIN FETCH v.cliente c
                LEFT JOIN FETCH v.detalles d
                LEFT JOIN FETCH d.producto p
                WHERE (:clienteId IS NULL OR c.id = :clienteId)
                AND (:estado IS NULL OR v.estado  = :estado)
                AND (:desde IS NULL OR v.fecha >= :desde)
                AND (:hasta IS NULL OR v.fecha <= :hasta)
            """
    )
    List<Venta> buscar(
            @Param("clienteId") Long clienteId,
            @Param("estado") EstadoVenta estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Sort sort
    );
}