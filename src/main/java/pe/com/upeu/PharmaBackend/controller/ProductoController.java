package pe.com.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.com.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.com.upeu.PharmaBackend.service.service.ProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> create(@Valid @RequestBody ProductoRequestDTO dto) {
        return new ResponseEntity<>(productoService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> read(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.read(id));
    }

    @GetMapping
    public ResponseEntity<Iterable<ProductoResponseDTO>> readAll() {
        return ResponseEntity.ok(productoService.readAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}