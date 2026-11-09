package cl.duoc.tiendaneon.config;

import cl.duoc.tiendaneon.producto.Producto;
import cl.duoc.tiendaneon.producto.ProductoRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Inserta productos de ejemplo solo si la tabla está vacía. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepository repository;

    public DatosIniciales(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Producto("Polera básica", "Polera de algodón", 9990, 20, null),
                new Producto("Polerón", "Polerón con capucha", 24990, 10, null),
                new Producto("Jockey", "Jockey ajustable", 7990, 15, null)));
    }
}
