package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.InventoryMovement;
import co.edu.sena.tiendavirtual.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {

    /** RF-014: historial de un producto. */
    List<InventoryMovement> findByProductOrderByCreatedAtDescIdDesc(Product product);

    List<InventoryMovement> findTop15ByOrderByCreatedAtDescIdDesc();
}
