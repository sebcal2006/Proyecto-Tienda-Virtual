package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Product;
import co.edu.sena.tiendavirtual.repository.CategoryRepository;
import co.edu.sena.tiendavirtual.repository.ProductRepository;
import co.edu.sena.tiendavirtual.service.BusinessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** RF-008 (catálogo), RF-009 (búsqueda y filtro) y RF-010 (detalle). */
@Controller
public class CatalogController {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public CatalogController(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @GetMapping("/catalogo")
    public String catalog(@RequestParam(required = false) String q,
                          @RequestParam(required = false) Integer categoria, Model model) {
        String query = q == null || q.isBlank() ? null : q.trim();
        model.addAttribute("products", products.search(query, categoria));
        model.addAttribute("categories", categories.findAllByOrderByNameAsc());
        model.addAttribute("q", query);
        model.addAttribute("categoria", categoria);
        return "catalogo";
    }

    @GetMapping("/catalogo/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Product product = products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        model.addAttribute("product", product);
        return "producto";
    }
}
