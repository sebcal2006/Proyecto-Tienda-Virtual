package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Order;
import co.edu.sena.tiendavirtual.entity.Product;
import co.edu.sena.tiendavirtual.repository.ProductRepository;
import co.edu.sena.tiendavirtual.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Venta presencial o telefónica del vendedor: RF-015/016 (cliente), RF-018 (buscar producto),
 * RF-019 (registrar venta), RF-020/021 (agregar y modificar productos) y RF-022 (confirmar).
 */
@Controller
@RequestMapping("/vendedor/venta")
public class InStoreSaleController {

    private static final String DRAFT = "saleDraft";

    private final ProductRepository products;
    private final CustomerService customers;
    private final OrderService orders;
    private final CurrentUserService current;

    public InStoreSaleController(ProductRepository products, CustomerService customers, OrderService orders,
                                 CurrentUserService current) {
        this.products = products;
        this.customers = customers;
        this.orders = orders;
        this.current = current;
    }

    /** Fila de la venta en curso para la vista. */
    public static class Line {
        private final Product product;
        private final int quantity;
        private final BigDecimal subtotal;

        public Line(Product product, int quantity, BigDecimal subtotal) {
            this.product = product;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    @GetMapping
    public String view(@RequestParam(required = false) String q, HttpSession session, Model model) {
        SaleDraft draft = draft(session);
        String query = q == null || q.isBlank() ? null : q.trim();
        model.addAttribute("q", query);
        model.addAttribute("results", query == null ? List.of() : products.search(query, null));
        model.addAttribute("customer", draft.getCustomerId() == null ? null : customers.get(draft.getCustomerId()));

        List<Line> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> e : draft.getLines().entrySet()) {
            Product p = products.findById(e.getKey()).orElseThrow();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
            lines.add(new Line(p, e.getValue(), subtotal));
            total = total.add(subtotal);
        }
        model.addAttribute("lines", lines);
        model.addAttribute("total", total);
        return "vendedor/venta";
    }

    @PostMapping("/cliente/buscar")
    public String findCustomer(@RequestParam String document, HttpSession session, RedirectAttributes flash) {
        customers.findByDocument(document).ifPresentOrElse(
                c -> {
                    draft(session).setCustomerId(c.getId());
                    flash.addFlashAttribute("ok", "Cliente seleccionado: " + c.getName());
                },
                () -> {
                    flash.addFlashAttribute("error", "No existe un cliente con ese documento. Regístrelo");
                    flash.addFlashAttribute("newDocument", document);
                });
        return "redirect:/vendedor/venta";
    }

    @PostMapping("/cliente/nuevo")
    public String newCustomer(@RequestParam String document, @RequestParam String name,
                              @RequestParam(required = false) String phone,
                              HttpSession session, RedirectAttributes flash) {
        try {
            var customer = customers.registerInStore(document, name, phone);
            draft(session).setCustomerId(customer.getId());
            flash.addFlashAttribute("ok", "Cliente registrado y seleccionado");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            flash.addFlashAttribute("newDocument", document);
        }
        return "redirect:/vendedor/venta";
    }

    @PostMapping("/agregar")
    public String add(@RequestParam Integer productId, @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) String q, HttpSession session, RedirectAttributes flash) {
        SaleDraft draft = draft(session);
        try {
            if (quantity <= 0) {
                throw new BusinessException("La cantidad debe ser mayor que cero");
            }
            Product product = product(productId);
            int total = draft.getLines().getOrDefault(productId, 0) + quantity;
            checkStock(product, total);
            draft.getLines().put(productId, total);
            flash.addFlashAttribute("ok", product.getName() + " agregado a la venta");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/vendedor/venta" + (q == null || q.isBlank() ? "" : "?q=" + encode(q));
    }

    @PostMapping("/cantidad")
    public String quantity(@RequestParam Integer productId, @RequestParam int quantity,
                           HttpSession session, RedirectAttributes flash) {
        SaleDraft draft = draft(session);
        try {
            if (quantity <= 0) {
                draft.getLines().remove(productId);
            } else {
                checkStock(product(productId), quantity);
                draft.getLines().put(productId, quantity);
            }
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/vendedor/venta";
    }

    @PostMapping("/quitar")
    public String remove(@RequestParam Integer productId, HttpSession session) {
        draft(session).getLines().remove(productId);
        return "redirect:/vendedor/venta";
    }

    @PostMapping("/confirmar")
    public String confirm(HttpSession session, Authentication auth, RedirectAttributes flash) {
        try {
            Order order = orders.confirmInStoreSale(draft(session), current.user(auth));
            session.removeAttribute(DRAFT);
            flash.addFlashAttribute("ok", "Venta confirmada: pedido " + order.getCode()
                    + ". El inventario se actualizó");
            return "redirect:/gestion/pedidos/" + order.getId();
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/vendedor/venta";
        }
    }

    @PostMapping("/descartar")
    public String discard(HttpSession session, RedirectAttributes flash) {
        session.removeAttribute(DRAFT);
        flash.addFlashAttribute("ok", "Venta descartada");
        return "redirect:/vendedor/venta";
    }

    private Product product(Integer id) {
        return products.findById(id).orElseThrow(() -> new BusinessException("Producto no encontrado"));
    }

    /** RN-002 antes de confirmar; al confirmar se valida de nuevo con la fila bloqueada. */
    private static void checkStock(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new BusinessException("Producto sin existencia disponible");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("Existencia insuficiente: quedan " + product.getStock()
                    + " unidades de " + product.getName());
        }
    }

    private static SaleDraft draft(HttpSession session) {
        SaleDraft draft = (SaleDraft) session.getAttribute(DRAFT);
        if (draft == null) {
            draft = new SaleDraft();
            session.setAttribute(DRAFT, draft);
        }
        return draft;
    }

    private static String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
