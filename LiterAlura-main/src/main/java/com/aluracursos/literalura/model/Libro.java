package com.aluracursos.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;

    @Column(name = "idiomas")
    private String idiomas;// Ejemplo: "en,es,fr"

    private Double numeroDeDescargas;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<Autor> autores = new ArrayList<>();

    public Libro() {
    }

    ;

    public Libro(LibrosDB librosDB) {
        this.titulo = librosDB.titulo();
        this.idiomas = String.join(",", librosDB.idiomas()); // Convierte la lista a un String
        this.numeroDeDescargas = librosDB.numeroDescargas();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getIdiomas() {
        return Arrays.asList(idiomas.split(","));
    }

    public void setIdiomas(List<String> idiomas) {
        this.idiomas = String.join(",", idiomas);
    }

    public Double getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    public void setNumeroDeDescargas(Double numeroDeDescargas) {
        this.numeroDeDescargas = numeroDeDescargas;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    @Override
    public String toString() {
        return """
                ______________________________________________________________________________________
                TITULO: %s
                AUTOR: %s
                IDIOMA: %s
                TOTAL DESCARGAS: %f""".formatted(titulo, autores, idiomas, numeroDeDescargas);// + "\n";
    }
}
