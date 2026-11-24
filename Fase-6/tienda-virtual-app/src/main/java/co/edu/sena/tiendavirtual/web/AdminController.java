package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.*;
import co.edu.sena.tiendavirtual.service.AdminService;
import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** Módulo del administrador: panel, usuarios y roles (RF-002, RF-003) y productos (RF-005 a RF-007). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService admin;
    private final CurrentUserService current;
    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final OrderRepository orders;
    private final InvoiceRepository invoices;

    public AdminController(AdminService admin, CurrentUserService current, UserRepository users,
                           RoleRepository roles, ProductRepository products, CategoryRepository categories,
                           OrderRepository orders, InvoiceRepository invoices) {
        this.admin = admin;
        this.current = current;
        this.users = users;
        this.roles = roles;
        this.products = products;
        this.categories = categories;
        this.orders = orders;
        this.invoices = invoices;
    }

    // ------------------------------------------------------------------ panel

    @GetMapping
    public String dashboard(Model model) {
        List<Order> all = orders.findAll();
        BigDecimal sales = all.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Product> active = products.searchAll(null).stream().filter(Product::isActive).toList();
        model.addAttribute("sales", sales);
        model.addAttribute("orderCount", all.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).count());
        model.addAttribute("pending", orders.countByStatus(OrderStatus.PENDING));
        model.addAttribute("preparing", orders.countByStatus(OrderStatus.IN_PREPARATION));
        model.addAttribute("onTheWay", orders.countByStatus(OrderStatus.ON_THE_WAY));
        model.addAttribute("lowStock", active.stream().filter(p -> p.getStock() <= 10).toList());
        model.addAttribute("userCount", users.count());
        model.addAttribute("invoiceCount", invoices.count());
        model.addAttribute("latest", orders.search(null, null).stream().limit(5).toList());
        return "admin/panel";
    }

    // ------------------------------------------------------------------ usuarios

    @GetMapping("/usuarios")
    public String users(Model model) {
        model.addAttribute("users", users.findAllByOrderByNameAsc());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String newUser(Model model) {
        model.addAttribute("roles", roles.findAllByOrderByIdAsc());
        model.addAttribute("form", new AdminService.UserData("", "", "", "", true, List.of()));
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/nuevo")
    public String createUser(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                             @RequestParam(required = false) String phone,
                             @RequestParam(defaultValue = "false") boolean active,
                             @RequestParam(required = false) List<Integer> roleIds,
                             Model model, RedirectAttributes flash) {
        var data = new AdminService.UserData(name, email, password, phone, active, roleIds);
        try {
            User user = admin.createUser(data);
            flash.addFlashAttribute("ok", "Usuario " + user.getEmail() + " creado");
            return "redirect:/admin/usuarios";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("roles", roles.findAllByOrderByIdAsc());
            model.addAttribute("form", data);
            return "admin/usuario-form";
        }
    }

    @GetMapping("/usuarios/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        User user = admin.user(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roles.findAllByOrderByIdAsc());
        model.addAttribute("form", new AdminService.UserData(user.getName(), user.getEmail(), "", user.getPhone(),
                user.isActive(), user.getRoles().stream().map(Role::getId).toList()));
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/{id}")
    public String updateUser(@PathVariable Integer id, @RequestParam String name,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String phone,
                             @RequestParam(defaultValue = "false") boolean active,
                             @RequestParam(required = false) List<Integer> roleIds,
                             Authentication auth, Model model, RedirectAttributes flash) {
        User user = admin.user(id);
        var data = new AdminService.UserData(name, user.getEmail(), password, phone, active, roleIds);
        try {
            admin.updateUser(id, data, current.user(auth));
            flash.addFlashAttribute("ok", "Usuario " + user.getEmail() + " actualizado");
            return "redirect:/admin/usuarios";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", roles.findAllByOrderByIdAsc());
            model.addAttribute("form", data);
            return "admin/usuario-form";
        }
    }

    // ------------------------------------------------------------------ productos

    @GetMapping("/productos")
    public String products(@RequestParam(required = false) String q, Model model) {
        String query = q == null || q.isBlank() ? null : q.trim();
        model.addAttribute("products", products.searchAll(query));
        model.addAttribute("q", query);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String newProduct(Model model) {
        model.addAttribute("categories", categories.findAllByOrderByNameAsc());
        model.addAttribute("form", new AdminService.ProductData("", "", "", null, null));
        return "admin/producto-form";
    }

    @PostMapping("/productos/nuevo")
    public String createProduct(@RequestParam String code, @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) BigDecimal price,
                                @RequestParam(required = false) Integer categoryId,
                                Model model, RedirectAttributes flash) {
        var data = new AdminService.ProductData(code, name, description, price, categoryId);
        try {
            Product product = admin.createProduct(data);
            flash.addFlashAttribute("ok", "Producto " + product.getCode()
                    + " creado con existencia 0. Las unidades se cargan con una entrada de inventario");
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", categories.findAllByOrderByNameAsc());
            model.addAttribute("form", data);
            return "admin/producto-form";
        }
    }

    @GetMapping("/productos/{id}")
    public String editProduct(@PathVariable Integer id, Model model) {
        Product p = admin.product(id);
        model.addAttribute("product", p);
        model.addAttribute("categories", categories.findAllByOrderByNameAsc());
        model.addAttribute("form", new AdminService.ProductData(p.getCode(), p.getName(), p.getDescription(),
                p.getPrice(), p.getCategory().getId()));
        return "admin/producto-form";
    }

    @PostMapping("/productos/{id}")
    public String updateProduct(@PathVariable Integer id, @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) BigDecimal price,
                                @RequestParam(required = false) Integer categoryId,
                                Model model, RedirectAttributes flash) {
        Product p = admin.product(id);
        var data = new AdminService.ProductData(p.getCode(), name, description, price, categoryId);
        try {
            admin.updateProduct(id, data);
            flash.addFlashAttribute("ok", "Producto " + p.getCode() + " actualizado");
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("product", p);
            model.addAttribute("categories", categories.findAllByOrderByNameAsc());
            model.addAttribute("form", data);
            return "admin/producto-form";
        }
    }

    /** RF-007 CA-02: si el producto está en una venta en curso, primero se advierte y se pide confirmación. */
    @PostMapping("/productos/{id}/desactivar")
    public String deactivate(@PathVariable Integer id, @RequestParam(defaultValue = "false") boolean confirmed,
                             Model model, RedirectAttributes flash) {
        long uses = admin.inProgressUses(id);
        if (uses > 0 && !confirmed) {
            model.addAttribute("product", admin.product(id));
            model.addAttribute("uses", uses);
            return "admin/producto-desactivar";
        }
        Product p = admin.setActive(id, false);
        flash.addFlashAttribute("ok", "Producto " + p.getCode() + " desactivado: ya no se vende en ningún canal");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/activar")
    public String activate(@PathVariable Integer id, RedirectAttributes flash) {
        Product p = admin.setActive(id, true);
        flash.addFlashAttribute("ok", "Producto " + p.getCode() + " activado");
        return "redirect:/admin/productos";
    }
}
