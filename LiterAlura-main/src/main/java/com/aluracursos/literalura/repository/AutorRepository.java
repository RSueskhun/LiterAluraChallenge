package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Autor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.librosDelAutor")
    List<Autor> findAllWithLibros();


    @EntityGraph(attributePaths = "librosDelAutor")

    List<Autor> findByFechaNacimientoBeforeAndFechaFallecimientoAfterOrFechaFallecimientoIsNullAndFechaNacimientoIsNotNull(
            String fechaNacimiento,
            String fechaFallecimiento);

    @EntityGraph(attributePaths = "librosDelAutor")
    List<Autor> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "librosDelAutor")
    List<Autor> findByFechaNacimientoBetween(String inicio, String fin);

}
