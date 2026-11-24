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

    /** Administración e inventario: todos los productos, activos e inactivos. */
    @Query("""
            select p from Product p
            where (:q is null or lower(p.name) like lower(concat('%', :q, '%'))
                              or lower(p.code) like lower(concat('%', :q, '%')))
            order by p.name
            """)
    List<Product> searchAll(@Param("q") String q);

    boolean existsByCodeIgnoreCase(String code);

    /** RF-007 CA-02: el producto está en carritos o en pedidos que aún no salen. */
    @Query("""
            select (select count(ci) from CartItem ci where ci.product.id = :id)
                 + (select count(oi) from OrderItem oi where oi.product.id = :id
                    and oi.order.status in (co.edu.sena.tiendavirtual.entity.OrderStatus.PENDING,
                                            co.edu.sena.tiendavirtual.entity.OrderStatus.IN_PREPARATION))
            from Product p where p.id = :id
            """)
    long countInProgressUses(@Param("id") Integer id);

    /** RNF-008: bloquea la fila para que dos ventas no descuenten la misma unidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Integer id);
}
