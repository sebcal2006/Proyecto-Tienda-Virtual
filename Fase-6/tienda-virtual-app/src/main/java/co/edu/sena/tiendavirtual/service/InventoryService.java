package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.InventoryMovement;
import co.edu.sena.tiendavirtual.entity.MovementType;
import co.edu.sena.tiendavirtual.entity.Product;
import co.edu.sena.tiendavirtual.entity.User;
import co.edu.sena.tiendavirtual.repository.InventoryMovementRepository;
import co.edu.sena.tiendavirtual.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Épica 3: entradas (RF-011), salidas (RF-012), consulta (RF-013) e historial (RF-014). */
@Service
public class InventoryService {

    private final ProductRepository products;
    private final InventoryMovementRepository movements;

    public InventoryService(ProductRepository products, InventoryMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    @Transactional
    public InventoryMovement registerEntry(Integer productId, int quantity, String notes, User user) {
        checkQuantity(quantity);
        Product product = lock(productId);
        product.setStock(product.getStock() + quantity);
        return movements.save(new InventoryMovement(product, user, null, MovementType.ENTRY, quantity,
                notes(notes, "Entrada de mercancía")));
    }

    @Transactional
    public InventoryMovement registerExit(Integer productId, int quantity, String notes, User user) {
        checkQuantity(quantity);
        Product product = lock(productId);
        if (product.getStock() < quantity) {
            throw new BusinessException("Existencia insuficiente: quedan " + product.getStock() + " unidades");
        }
        product.setStock(product.getStock() - quantity);
        return movements.save(new InventoryMovement(product, user, null, MovementType.EXIT, quantity,
                notes(notes, "Salida de mercancía")));
    }

    public List<InventoryMovement> history(Product product) {
        return movements.findByProductOrderByCreatedAtDescIdDesc(product);
    }

    public List<InventoryMovement> latest() {
        return movements.findTop15ByOrderByCreatedAtDescIdDesc();
    }

    private static void checkQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }
    }

    private Product lock(Integer id) {
        return products.findByIdForUpdate(id).orElseThrow(() -> new BusinessException("Producto no encontrado"));
    }

    private static String notes(String notes, String fallback) {
        return notes == null || notes.isBlank() ? fallback : notes.trim();
    }
}
