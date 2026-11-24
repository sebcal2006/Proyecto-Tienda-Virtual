package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByUser(User user);

    Optional<Customer> findByDocument(String document);
}
