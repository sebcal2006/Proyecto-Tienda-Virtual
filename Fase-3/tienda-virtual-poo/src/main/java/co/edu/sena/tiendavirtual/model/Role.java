package co.edu.sena.tiendavirtual.model;

import java.util.Objects;

/** Rol del sistema (tabla roles). Un usuario puede tener varios roles (tabla user_roles). */
public class Role {

    private Integer id;
    private final RoleName name;
    private String description;

    public Role(RoleName name, String description) {
        this.name = Objects.requireNonNull(name, "El rol es obligatorio");
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public RoleName getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Role other && name == other.name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name.getLabel();
    }
}
