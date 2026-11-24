package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.Order;
import co.edu.sena.tiendavirtual.entity.OrderStatus;
import co.edu.sena.tiendavirtual.entity.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    /** RF-040: historial del cliente. */
    List<Order> findByCustomerOrderByCreatedAtDesc(Customer customer);

    /** RF-028: listado con filtros opcionales por estado y canal. */
    @Query("""
            select o from SalesOrder o
            where (:status is null or o.status = :status)
              and (:channel is null or o.channel = :channel)
            order by o.createdAt desc
            """)
    List<Order> search(@Param("status") OrderStatus status, @Param("channel") SalesChannel channel);

    /** RF-041: pedidos no cancelados que todavía no tienen factura. */
    @Query("""
            select o from SalesOrder o
            where o.status <> co.edu.sena.tiendavirtual.entity.OrderStatus.CANCELLED
              and not exists (select i from Invoice i where i.order = o)
            order by o.createdAt desc
            """)
    List<Order> findWithoutInvoice();

    long countByStatus(OrderStatus status);
}
