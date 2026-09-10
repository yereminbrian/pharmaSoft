package pe.com.upeu.PharmaBackend.service.service;

import pe.com.upeu.PharmaBackend.dto.VentaRequestDTO;
import pe.com.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.com.upeu.PharmaBackend.enums.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    VentaResponseDTO registrar(VentaRequestDTO request);
    VentaResponseDTO buscar(Long id);
    List<VentaResponseDTO> listar();
    List<VentaResponseDTO> buscar(
            Long clienteId,
            EstadoVenta estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}