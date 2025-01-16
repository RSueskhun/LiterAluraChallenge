package com.aluracursos.literalura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String fechaNacimiento;
    private String fechaFallecimiento;

    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    private List<Libro> librosDelAutor = new ArrayList<>();

    public Autor() {
    }

    ;

    public Autor(AutorDB autorDB) {
        this.nombre = autorDB.nombre();
        this.fechaNacimiento = autorDB.fechaNacimiento();
        this.fechaFallecimiento = autorDB.fechaFallecimiento();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public void setFechaFallecimiento(String fechaFallecimiento) {
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public List<Libro> getLibrosDelAutor() {
        return librosDelAutor;
    }

    public void setLibrosDelAutor(List<Libro> librosDelAutor) {
        this.librosDelAutor = librosDelAutor;
    }

    @Override
    public String toString() {
        return """
                       AUTOR
                       ---------------------------------------------------------------
                       AUTOR: %s
                       FECHA DE NACIMIENTO: %s
                       FECHA DE MUERTE: %s
                       LIBROS DE SU AUTORIA: %s
                       """.formatted(nombre, fechaNacimiento, fechaFallecimiento, librosDelAutor) + "\n";
    }
}
