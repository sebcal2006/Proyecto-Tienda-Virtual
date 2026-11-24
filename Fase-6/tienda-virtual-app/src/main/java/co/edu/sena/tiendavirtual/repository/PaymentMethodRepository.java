package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    List<PaymentMethod> findByActiveTrueOrderByNameAsc();
}
