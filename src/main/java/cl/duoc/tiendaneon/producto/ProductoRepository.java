package cl.duoc.tiendaneon.producto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository ya trae findAll, findById, save y delete.
 * Spring arma el SQL a partir del NOMBRE del método:
 * findByNombreContainingIgnoreCase("pol")  ->  SELECT ... WHERE lower(nombre) LIKE '%pol%'
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
