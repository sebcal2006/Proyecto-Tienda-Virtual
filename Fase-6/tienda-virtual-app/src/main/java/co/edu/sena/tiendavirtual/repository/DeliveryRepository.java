package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Delivery;
import co.edu.sena.tiendavirtual.entity.Order;
import co.edu.sena.tiendavirtual.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Optional<Delivery> findByOrder(Order order);

    /** RF-036: solo los domicilios del domiciliario. */
    List<Delivery> findByCourierOrderByAssignedAtDesc(User courier);
}
