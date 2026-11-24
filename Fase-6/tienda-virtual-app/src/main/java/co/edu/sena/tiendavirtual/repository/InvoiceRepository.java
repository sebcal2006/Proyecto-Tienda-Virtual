package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Invoice;
import co.edu.sena.tiendavirtual.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Optional<Invoice> findByOrder(Order order);

    boolean existsByOrder(Order order);

    List<Invoice> findAllByOrderByIssuedAtDesc();

    List<Invoice> findAllByOrderByIdDesc();
}
