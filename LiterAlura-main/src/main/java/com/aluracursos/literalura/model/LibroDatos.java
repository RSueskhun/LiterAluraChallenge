package com.aluracursos.literalura.model;


import java.util.List;

public record LibroDatos(
        Long id,
        String titulo,
        List<AutorDatos> autores,
        String idiomas,
        Double numeroDeDescargas
) {

}
