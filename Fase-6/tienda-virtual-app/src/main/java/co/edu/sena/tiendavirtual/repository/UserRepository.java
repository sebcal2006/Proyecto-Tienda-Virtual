package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByOrderByNameAsc();

    /** Usuarios activos con un rol, por ejemplo los domiciliarios disponibles (RF-035). */
    @Query("select distinct u from User u join u.roles r where r.name = :role and u.active = true order by u.name")
    List<User> findActiveByRole(@Param("role") String role);
}
