package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    /** RF-008 / RF-009 / RF-018: productos activos por nombre o código y categoría. */
    @Query("""
            select p from Product p
            where p.active = true
              and (:q is null or lower(p.name) like lower(concat('%', :q, '%'))
                              or lower(p.code) like lower(concat('%', :q, '%')))
              and (:categoryId is null or p.category.id = :categoryId)
            order by p.name
            """)
    List<Product> search(@Param("q") String q, @Param("categoryId") Integer categoryId);

    /** RNF-008: bloquea la fila para que dos ventas no descuenten la misma unidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Integer id);
}
