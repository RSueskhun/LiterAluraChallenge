package com.aluracursos.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LibrosDB(
    @JsonAlias("title") String titulo,
    @JsonAlias("authors") List<AutorDB> autor,
    @JsonAlias("languages") List<String> idiomas,
    @JsonAlias("download_count") Double numeroDescargas
) {
}
