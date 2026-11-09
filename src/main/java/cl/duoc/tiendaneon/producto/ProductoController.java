package cl.duoc.tiendaneon.producto;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rutas del recurso "productos". Todas empiezan con /api/productos.
 *
 * | Método | Ruta                  | Éxito                  | Errores  |
 * |--------|-----------------------|------------------------|----------|
 * | GET    | /api/productos        | 200 + lista            |          |
 * | GET    | /api/productos?nombre=pol | 200 + lista filtrada |        |
 * | GET    | /api/productos/{id}   | 200 + producto         | 404      |
 * | POST   | /api/productos        | 201 + Location + producto | 400   |
 * | PUT    | /api/productos/{id}   | 200 + producto         | 400, 404 |
 * | DELETE | /api/productos/{id}   | 204 sin cuerpo         | 404      |
 *
 * Los errores los transforma en JSON la clase ManejadorErrores.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repository;

    public ProductoController(ProductoRepository repository) {
        this.repository = repository;
    }

    /** GET /api/productos  o  GET /api/productos?nombre=pol */
    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    /** GET /api/productos/5 -> 200, o 404 si no existe. */
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return buscar(id);
    }

    /** POST /api/productos -> 201 Created, con el header Location: /api/productos/{id nuevo}. */
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        producto.setId(null); // el id lo genera la base de datos
        Producto guardado = repository.save(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + guardado.getId())).body(guardado);
    }

    /** PUT /api/productos/5 -> reemplaza el producto COMPLETO. */
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto datos) {
        Producto producto = buscar(id);
        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setImagenUrl(datos.getImagenUrl());
        return repository.save(producto);
    }

    /** DELETE /api/productos/5 -> 204 No Content (respuesta sin cuerpo). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        repository.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }

    private Producto buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto " + id + " no existe"));
    }
}
