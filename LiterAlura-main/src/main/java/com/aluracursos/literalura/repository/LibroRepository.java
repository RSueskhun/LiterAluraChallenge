package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Libro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    @EntityGraph(attributePaths = "autores")
    Optional<Libro> findByTitulo(String titulo);

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // Tengo dos formas de evitar la excepción: LazyInitializationException
    // Forma 1
    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.autores")
    List<Libro> findAllWithAutores();

    // Forma 2
//    @EntityGraph(attributePaths = "autores")
//    List<Libro> findAll();
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    @EntityGraph(attributePaths = "autores")
    List<Libro> findByIdiomasContaining(String idiomas);

    List<Libro> findTop10ByOrderByNumeroDeDescargasDesc();
}
