package pe.com.upeu.PharmaBackend.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.com.upeu.PharmaBackend.dto.PaginaResponseDTO;
import pe.com.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.com.upeu.PharmaBackend.entity.Categoria;
import pe.com.upeu.PharmaBackend.entity.Producto;
import pe.com.upeu.PharmaBackend.exception.RecursoNoEncontradoException;
import pe.com.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.com.upeu.PharmaBackend.mapper.ProductoMapper;
import pe.com.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.com.upeu.PharmaBackend.repository.ProductoRepository;
import pe.com.upeu.PharmaBackend.service.service.ProductoService;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import pe.com.upeu.PharmaBackend.util.PaginacionUtil;


@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log =
            LoggerFactory.getLogger(ProductoServiceImpl.class);

    private static final Set<String> CAMPOS_ORDENABLES =
            Set.of("id", "nombre", "precio", "stock");

    private static final String ORDEN_POR_DEFECTO = "id";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
    }

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        String nombre = request.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre "+ nombre
            );
        }

        Categoria categoria = buscarCategoria(request.getCategoriaId());

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setEstado(request.getEstado());
        producto.setCategoria(categoria);

        Producto prodCreado = productoRepository.save(producto);

        return productoMapper.toResponse(prodCreado);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id).orElseThrow(()->
                new RecursoNoEncontradoException(
                        "Producto no encontrado con id: "+ id
                )
        );

        String nombre = request.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)){
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre "+ nombre
            );
        }

        Categoria categoria = buscarCategoria(request.getCategoriaId());

        producto.setNombre(nombre);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setEstado(request.getEstado());
        producto.setCategoria(categoria);

        Producto prodActualizado = productoRepository.saveAndFlush(producto);

        return productoMapper.toResponse(prodActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscar(Long id) {

        log.info("Buscando producto | id={}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(()->
                        new RecursoNoEncontradoException(
                                "Producto no encontrado con id: "+ id
                        )
                );
        return productoMapper.toResponse(producto);
    }

    /*
     * Baja lógica: el producto queda referenciado desde el histórico de
     * ventas (detalle_ventas.producto_id), así que nunca se borra
     * físicamente. Con estado=false ya no puede venderse (ver
     * VentaServiceImpl.registrar) y sigue apareciendo en reportes.
     */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id).orElseThrow(()->
                new RecursoNoEncontradoException(
                        "Producto no encontrado con id: "+ id
                )
        );

        if (!Boolean.TRUE.equals(producto.getEstado())) {
            throw new ReglaNegocioException(
                    "El producto "+ producto.getNombre()+ " ya se encuentra inactivo"
            );
        }

        producto.setEstado(false);
        productoRepository.save(producto);

        log.info("Producto dado de baja | id={} | nombre={}", id, producto.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<ProductoResponseDTO> listar(
            int pagina,
            int tamanio,
            String ordenarPor,
            String direccion) {

        long inicio = System.currentTimeMillis();
        log.info("Inicio listar productos | pagina={} | tamanio={} | "
                        + "ordenarPor={} | direccion={}",
                pagina, tamanio, ordenarPor, direccion);

        Pageable pageable = PaginacionUtil.construir(
                pagina, tamanio, ordenarPor, direccion,
                CAMPOS_ORDENABLES, ORDEN_POR_DEFECTO);

        Page<ProductoResponseDTO> resultado =
                productoRepository.findAll(pageable)
                        .map(productoMapper::toResponse);

        log.info("Fin listar productos | filas={} | total={} | duracionMs={}",
                resultado.getNumberOfElements(),
                resultado.getTotalElements(),
                System.currentTimeMillis() - inicio);

        return PaginaResponseDTO.de(resultado);
    }

    private Categoria buscarCategoria(Long categoriaId){
        return categoriaRepository.findById(categoriaId).orElseThrow(()->
                new RecursoNoEncontradoException(
                        "Categoria no encontrada con id: "+ categoriaId
                )
        );
    }
}