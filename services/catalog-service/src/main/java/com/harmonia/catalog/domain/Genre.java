package com.harmonia.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    protected Genre() {
    }

    public static Genre create(String name) {
        return create(UUID.randomUUID(), name);
    }

    public static Genre create(UUID id, String name) {
        Genre genre = new Genre();
        genre.id = id;
        genre.rename(name);
        return genre;
    }

    public void rename(String name) {
        this.name = name;
        this.slug = slugify(name);
    }

    public static String slugify(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return slug.replaceAll("(^-|-$)", "");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}
