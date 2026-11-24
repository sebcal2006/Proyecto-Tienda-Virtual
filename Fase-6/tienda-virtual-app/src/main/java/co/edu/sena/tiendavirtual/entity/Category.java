package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

/** Tabla categories. */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    public Integer getId() { return id; }
    public String getName() { return name; }
}
