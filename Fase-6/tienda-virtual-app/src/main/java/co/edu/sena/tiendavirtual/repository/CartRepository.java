package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Cart;
import co.edu.sena.tiendavirtual.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByCustomer(Customer customer);
}
