package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.Cart;
import co.edu.sena.tiendavirtual.entity.CartItem;
import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.Product;
import co.edu.sena.tiendavirtual.repository.CartRepository;
import co.edu.sena.tiendavirtual.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-023 a RF-025. Aplica RN-002 y RN-008. */
@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    @Transactional
    public Cart cartOf(Customer customer) {
        return carts.findByCustomer(customer).orElseGet(() -> carts.save(new Cart(customer)));
    }

    /** RF-023: si el producto ya está, suma la cantidad. */
    @Transactional
    public void add(Customer customer, Integer productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }
        Cart cart = cartOf(customer);
        Product product = product(productId);
        int current = cart.find(product).map(CartItem::getQuantity).orElse(0);
        checkAvailable(product, current + quantity);
        cart.find(product).ifPresentOrElse(
                item -> item.setQuantity(current + quantity),
                () -> cart.getItems().add(new CartItem(cart, product, quantity)));
    }

    /** RF-024: con cantidad 0 o menos se elimina el producto. */
    @Transactional
    public void update(Customer customer, Integer productId, int quantity) {
        Cart cart = cartOf(customer);
        Product product = product(productId);
        CartItem item = cart.find(product)
                .orElseThrow(() -> new BusinessException("El producto no está en el carrito"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
            return;
        }
        checkAvailable(product, quantity);
        item.setQuantity(quantity);
    }

    @Transactional
    public void remove(Customer customer, Integer productId) {
        Cart cart = cartOf(customer);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
    }

    private Product product(Integer id) {
        return products.findById(id).orElseThrow(() -> new BusinessException("Producto no encontrado"));
    }

    /** RN-002. */
    static void checkAvailable(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }
        if (!product.isAvailable()) {
            throw new BusinessException("Producto sin existencia disponible");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("Existencia insuficiente: quedan " + product.getStock()
                    + " unidades de " + product.getName());
        }
    }
}
