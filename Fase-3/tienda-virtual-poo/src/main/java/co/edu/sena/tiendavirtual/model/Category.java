package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

/** Categoría de productos (tabla categories). */
public class Category {

    private Integer id;
    private String name;

    public Category(String name) {
        setName(name);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("RF-005", "La categoría es obligatoria");
        }
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return name;
    }
}
