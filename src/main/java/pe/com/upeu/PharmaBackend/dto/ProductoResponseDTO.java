package pe.com.upeu.PharmaBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoResponseDTO {
    private long id;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private BigDecimal precio;
    private int stock;
    private Long categoriaId;
    private String categoriaNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}