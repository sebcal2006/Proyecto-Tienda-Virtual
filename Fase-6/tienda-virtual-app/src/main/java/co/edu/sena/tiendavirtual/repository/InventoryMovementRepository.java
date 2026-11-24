package co.edu.sena.tiendavirtual.repository;

import co.edu.sena.tiendavirtual.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {
}
