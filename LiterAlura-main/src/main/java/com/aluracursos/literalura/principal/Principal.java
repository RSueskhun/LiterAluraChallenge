package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.AutorDatos;
import com.aluracursos.literalura.model.LibroDatos;
import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private List<Libro> libros;
    private List<Autor> autores;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    //MENU
    public void muestraElMenu() {
        var opcion = -1;

        while (opcion != 0) {
            var menu = """
                    **************************************************
                    MENU
                    **************************************************
                    1 - BUSCAR LIBRO X TITULO
                    2 - FILTRO: LIBROS DESCARGADOS O REGISTRADOS
                    3 - FILTRO: AUTORES REGISTRADOS
                    4 - FILTRO: AUTORES POR AÑO
                    5 - FILTRO: LIBROS POR IDIOMA
                    6 - BUSCAR X AUTOR X NOMBRE
                    7 - FILTRO: AUTORES POR FECHA DE NACIMIENTO
                    8 - TOP TEN!!!!!!
                    9 - ESTADISTICAS
                    0 - SALIR
                    """;
            System.out.println(menu);
            System.out.print("OPCION SELECCIONADA: ");
            String opcionMenu = sc.nextLine();
            try {
                opcion = Integer.parseInt(opcionMenu);
            } catch (NumberFormatException e) {
                System.out.println("INGRESE EL NUMERO DE SU ELECCION: ");
                continue;
            }

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosEnAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    buscarAutorPorNombre();
                    break;
                case 7:
                    listarAutoresPorRangoNacimiento();
                    break;
                case 8:
                    top10LibrosMasDescargados();
                    break;
                case 9:
                    estadisticas();
                    break;

                case 0:
                    System.out.println("""
                            --------------------------------------------------
                            GRACIAS POR USAR NUESTRO SISTEMA
                            """);
                    break;
                default:
                    System.out.println("SELECCION INVALIDA");
            }

        }
    }


    private void buscarLibroPorTitulo() {
        LibrosDB librosDB = getDatosLibros();

        if (librosDB == null) {
            System.out.println("""
                    __________________________________________________
                    LIBRO NO ENCONTRADO
                    """);
            pausa();
            return;
        }

        // VERIFICACION LIBRO EXISTENTE
        Optional<Libro> libroExistente = libroRepository.findByTitulo(librosDB.titulo());
        if (libroExistente.isPresent()) {
            System.out.println("""
                    __________________________________________________
                    LIBRO YA EXISTE EN LA BASE DE DATOS
                    """);

            //INFORMACION LIBRO EXISTENTE
            LibroDatos libroDatos = new LibroDatos(
                    libroExistente.get().getId(),
                    libroExistente.get().getTitulo(),
                    libroExistente.get().getAutores().stream().map(autor -> new AutorDatos(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                            .collect(Collectors.toList()),
                    String.join(", ", libroExistente.get().getIdiomas()),
                    libroExistente.get().getNumeroDeDescargas()
            );

          //DETALLES LIBRO REGISTRADO
            System.out.printf(
                    """
                            -------------------TITULO-------------------
                            TITULO: %s
                            AUTOR: %s
                            IDIOMA: %s
                            TOTAL DESCARGAS: %.2f%n""", libroDatos.titulo(),
                    libroDatos.autores().stream().map(AutorDatos::nombre).collect(Collectors.joining(", ")),
                    libroDatos.idiomas(),
                    libroDatos.numeroDeDescargas()
            );
            System.out.println("--------------------------------------------------");
            pausa();
            return;
        }

        //INFORMACION DE AUTORES
        List<Autor> autores = librosDB.autor().stream()
                .map(datosAutor -> autorRepository.findByNombre(datosAutor.nombre())
                        .orElseGet(() -> {
                            // Crear y guardar nuevo autor
                            Autor nuevoAutor = new Autor();
                            nuevoAutor.setNombre(datosAutor.nombre());
                            nuevoAutor.setFechaNacimiento(datosAutor.fechaNacimiento());
                            nuevoAutor.setFechaFallecimiento(datosAutor.fechaFallecimiento());
                            autorRepository.save(nuevoAutor);
                            return nuevoAutor;
                        })
                ).collect(Collectors.toList());

        //CREAR LIBRO Y AÑADIR DATOS
        Libro libro = new Libro(librosDB);
        libro.setAutores(autores);
        libroRepository.save(libro);

        LibroDatos libroDatos = new LibroDatos(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutores().stream().map(autor -> new AutorDatos(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                        .collect(Collectors.toList()),
                String.join(", ", libro.getIdiomas()),
                libro.getNumeroDeDescargas());

        //MOSTRAR INFORMACION LIBRO
        System.out.printf(
                """
                        -------------------TITULO-------------------
                        TITULO: %s
                        AAUTOR: %s
                        IDIOMA: %s
                        TOTAL DESCARGAS: %.2f%n""", libroDatos.titulo(),
                libroDatos.autores().stream().map(AutorDatos::nombre).collect(Collectors.joining(", ")),
                libroDatos.idiomas(),
                libroDatos.numeroDeDescargas()
        );
        System.out.println("--------------------------------------------------");
        pausa();
    }

    private LibrosDB getDatosLibros() {
        System.out.print("INGRESE EL NOMBRE DEL LIBRO A BUSCAR: ");
        var nombreLibro = sc.nextLine();

        // BUSCAR LIBRO CONSUMIENDO LA API
        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibro.replace(" ", "+")); // me trae un json

        // CONVERSION JSON - JAVA
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);

        //RESULTADO
        return datosBusqueda.listaResultados().stream()
                .filter(librosDB -> librosDB.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst()
                .orElse(null); // Devolver null si no se encuentra el libro
    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAllWithAutores();

        if (libros.isEmpty()) {
            System.out.println("""
                    __________________________________________________
                    NO HAY LIBROS REGISTRADOS EN EL SISTEMA
                    """);
            pausa();
            return;
        }
        System.out.printf("""
                __________________________________________________
                %d LIBROS REGISTRADOS
                __________________________________________________%n""", libros.size());
        mostrarLibros(libros);
        pausa();
    }

    private void listarAutoresRegistrados() {
        autores = autorRepository.findAllWithLibros();

        if (autores.isEmpty()) {
            System.out.println("""
                    __________________________________________________
                    NO HAY AUTORES REGISTRADOS EN EL SISTEMA
                    __________________________________________________%n""");
            pausa();
            return;
        }
        System.out.printf("""
                __________________________________________________
                %d AUTORES REGISTRADOS
                __________________________________________________%n""", autores.size());
        mostrarAutores(autores);
        pausa();
    }

    private void listarAutoresVivosEnAnio() {
        var valorValido = false;
        String anioEstaVivo;
        do {
            System.out.print("INGRESE EL AÑO EN EL QUE DESEA BUSCAR: ");
            anioEstaVivo = sc.nextLine();
           if (!validarAnio4Digitos(anioEstaVivo)) {
                anioNoValido();
                continue;
            }
            valorValido = true;
        } while (!valorValido);

        int anio = Integer.parseInt(anioEstaVivo);

        // FILTRO AUTORES POR AÑO
        List<Autor> autoresVivos = autorRepository.findByFechaNacimientoBeforeAndFechaFallecimientoAfterOrFechaFallecimientoIsNullAndFechaNacimientoIsNotNull(String.valueOf(anio), String.valueOf(anio));

        // RESTRICCION DE RANGO
        int anioActual = Year.now().getValue();

        autoresVivos = autoresVivos.stream()
                .filter(autor -> {
                    if (autor.getFechaFallecimiento() == null) {
                        int anioNacimiento = Integer.parseInt(autor.getFechaNacimiento());
                        return anioActual - anioNacimiento <= 100;
                    }
                    return true;
                }).collect(Collectors.toList());

        if (autoresVivos.isEmpty()) {
            System.out.println("""
                    __________________________________________________
                    NO SE ENCONTRARON AUTORES PARA EL AÑO INDICADO
                    __________________________________________________""");
            pausa();
        } else {
            System.out.printf("""
                    ___________________________________________________
                    %d AUTORES DE %d
                    ___________________________________________________%n""", autoresVivos.size(), anio);
            mostrarAutores(autoresVivos);
            pausa();
        }
    }

    private void listarLibrosPorIdioma() {
        var menuIdiomas = """
                LIBROS POR IDIOMA
                -------------------------------------------------------
                es - ESPAÑOL                it - ITALIANO
                en - INGLES                 ja - JAPONES
                fr - FRANCES                pt - PORTUGUES
                ru - RUSO                   zh - CHINO
                de - ALEMAN                 ar - ARABE
                """;
        String idiomaLibro;
        do {
            System.out.println(menuIdiomas);
            System.out.print("INGRESE LAS INICIALES DEL IDIOMA PARA FILTRAR: ");
            idiomaLibro = sc.nextLine().toLowerCase();

            // Validar que el idioma ingresado tenga dos letras y no incluya números
            if (!idiomaLibro.matches("^[a-z]{2}$")) {
                System.out.println("""
                        CODIGO INVALIDO PARA EL IDIOMA SELECCIONADO
                        ------------------------------------------------""");
            }
        } while (!idiomaLibro.matches("^[a-z]{2}$"));

        // MOSTRAR LIBROS EN EL IDIOMA SELECCIONADO
        List<Libro> librosPorIdioma = libroRepository.findByIdiomasContaining(idiomaLibro);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("""
                    NO SE ENCONTRARON LIBROS EN EL IDIOMA SELECCIOANDO
                    ----------------------------------------------------""");
            pausa();
        } else {
            if (librosPorIdioma.size() == 1) {
                System.out.printf("""
                                %d LIBRO EN EL IDIOMA '%s'
                                -------------------------------------------------%n""",
                        librosPorIdioma.size(), idiomaLibro.toUpperCase());
            } else {
                System.out.printf("""
                                %d LIBROS EN EL IDIOMA '%s'
                                --------------------------------------------------%n""",
                        librosPorIdioma.size(), idiomaLibro.toUpperCase());
            }
            mostrarLibros(librosPorIdioma);
            pausa();
        }
    }

    private void mostrarLibros(List<Libro> libroList) {
        for (Libro libro : libroList) {
            List<AutorDatos> autoresDTO = libro.getAutores().stream()
                    .map(autor -> new AutorDatos(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                    .collect(Collectors.toList());

            //FILTRO Y RESTRICCION DE INFORMACION
            LibroDatos libroDatos = new LibroDatos(
                    libro.getId(),
                    libro.getTitulo(),
                    autoresDTO,
                    String.join(", ", libro.getIdiomas()),
                    libro.getNumeroDeDescargas()
            );

            System.out.printf(
                    """
                            -------------------TITULO-------------------
                            TITULO: %s
                            AAUTOR: %s
                            IDIOMA: %s
                            OTAL DESCARGAS: %.2f%n""", libroDatos.titulo(),
                    libroDatos.autores().stream().map(AutorDatos::nombre).collect(Collectors.joining(", ")),
                    String.join(", ", libro.getIdiomas()),
                    libroDatos.numeroDeDescargas()
            );
            System.out.println("--------------------------------------------------");

        }
    }

    private void mostrarAutores(List<Autor> autoresList) {
        for (Autor autor : autoresList) {
            List<String> librosDelAutor = autor.getLibrosDelAutor().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());

            AutorDatos autorDatos = new AutorDatos(
                    autor.getId(),
                    autor.getNombre(),
                    autor.getFechaNacimiento(),
                    autor.getFechaFallecimiento()
            );

            //MOSTRAR INFORMACION
            System.out.printf(
                    """
                            AUTOR
                            ------------------------------------------------------
                            AUTOR: %s
                            FECHA DE NACIMIENTO: %s
                            FECHA DE MUERTE: %s
                            LIBROS DE SU AUTORIA: %s%n""", autorDatos.nombre(),
                    autorDatos.fechaNacimiento() != null ? autorDatos.fechaNacimiento() : "N/A",
                    autorDatos.fechaFallecimiento() != null ? autorDatos.fechaFallecimiento() : "N/A",
                    librosDelAutor
            );
            System.out.println("--------------------------------------------------");
        }
    }

    private void buscarAutorPorNombre() {
        System.out.print("INGRESE EL NOMBRE DEL AUTOR A BUSCAR: ");
        var nombreAutor = sc.nextLine().toLowerCase();

        //BUSQUEDA DB
        List<Autor> autoresBuscados = autorRepository.findByNombreContainingIgnoreCase(nombreAutor);

        System.out.printf("""
                AUTOR POR NOMBRE:'%s'
                --------------------------------------------------%n""", nombreAutor);
        if (autoresBuscados.isEmpty()) {
            System.out.println("""
                    NO SE ENCONTRARON COINCIDENCIAS PARA EL NOMBRE INDICADO
                    ---------------------------------------------------------""");
            pausa();
        } else {
            mostrarAutores(autoresBuscados);
            pausa();
        }

    }

    private void listarAutoresPorRangoNacimiento() {
        String anioInicio;
        String anioFin;

        while (true) {
            System.out.print("INGRESE EL AÑO INICIAL PARA LA BUSQUEDA: ");
            anioInicio = sc.nextLine();

            if (!validarAnio4Digitos(anioInicio)) {
                anioNoValido();
                continue;
            }
            System.out.print("INGRESE EL AÑO FINAL PARA LA BUSQUEDA: ");
            anioFin = sc.nextLine();

            if (!validarAnio4Digitos(anioFin)) {
                anioNoValido();
                continue;
            }

            //RESTRICCION DE RANGO
            if (Integer.parseInt(anioInicio) > Integer.parseInt(anioFin)) {
                System.out.println("""
                        POR FAVOR INGRESE UN RANGO CRONOLOGICAMENTE VALIDO
                        --------------------------------------------------""");
                continue;
            }
            break;
        }


        List<Autor> autoresEnRango = autorRepository.findByFechaNacimientoBetween(anioInicio, anioFin);
        System.out.printf("""
                RESULTADOS DE AUTOR POR RANGO: '%s' Y '%s'
                --------------------------------------------------%n""", anioInicio, anioFin);
        if (autoresEnRango.isEmpty()) {
            System.out.println("""
                    NO SE ENCONTRARON COINCIDENCIAS PARA EL RANGO INDICADO""");
            pausa();
        } else {
            mostrarAutores(autoresEnRango);
            pausa();
        }
    }

    private void top10LibrosMasDescargados() {
        List<Libro> top10List = libroRepository.findTop10ByOrderByNumeroDeDescargasDesc();
        System.out.println("""
                ESTOS SON LOS LIBROS UBICADOS EN EL TOP 10!!!
                ----------------------------------------------""");
        top10List.forEach((libro -> System.out.printf("Título: %s - Descargas: %.0f%n", libro.getTitulo().toUpperCase(), libro.getNumeroDeDescargas())));
        pausa();
    }

    private void estadisticas() {

        System.out.println("""
                ESTADISTICAS GENERALES
                ---------------------------------------------------""");
        mostrarEstadisticasDescargasLibros();
        mostrarEstadisticasEdadesAutores();
        pausa();
    }

    private void mostrarEstadisticasDescargasLibros() {
        DoubleSummaryStatistics estadisticasDescargas = libroRepository.findAll().stream()
                .mapToDouble(Libro::getNumeroDeDescargas)
                .summaryStatistics();

        System.out.println("""
                DESCARGAS
                ------------------------------------------------------------""");
        System.out.printf("TOTAL LIBROS: %d%n", estadisticasDescargas.getCount());
        System.out.printf("TOTAL DESCARGAS: %.2f%n", estadisticasDescargas.getSum());
        System.out.printf("LIBRO MAS DESCARGADO: %.2f%n", estadisticasDescargas.getMax());
        System.out.printf("LIBRO MENOS DESCARGADO: %.2f%n", estadisticasDescargas.getMin());
        System.out.println("--------------------------------------------------");
    }

    private void mostrarEstadisticasEdadesAutores() {
        LocalDate fechaActual = LocalDate.now();

        DoubleSummaryStatistics estadisticasEdad = autorRepository.findAll().stream()
                .filter(autor -> autor.getFechaNacimiento() != null)
                .filter(autor -> {

                    if (autor.getFechaFallecimiento() == null) {
                        int anioNacimiento = Integer.parseInt(autor.getFechaNacimiento().substring(0, 4));
                        int anioActual = fechaActual.getYear();
                        int edad = anioActual - anioNacimiento;
                        return edad < 100;
                    }

                    return true;
                })
                .mapToDouble(autor -> {

                    int anioNacimiento = Integer.parseInt(autor.getFechaNacimiento().substring(0, 4));


                    if (autor.getFechaFallecimiento() != null) {
                        int anioFallecimiento = Integer.parseInt(autor.getFechaFallecimiento().substring(0, 4));
                        return anioFallecimiento - anioNacimiento;
                    } else {

                        int anioActual = fechaActual.getYear();
                        return anioActual - anioNacimiento;
                    }
                }).summaryStatistics();

        System.out.println("""
                CLASIFICACION POR EDADES
                --------------------------------------------------------------""");
        System.out.printf("TOTAL AUTORES: %d%n", estadisticasEdad.getCount());
        System.out.printf("PROMEDIO DE EDAD: %.2f años%n", estadisticasEdad.getAverage());
        System.out.printf("EDAD MAXIMA DEL AUTOR: %.2f años%n", estadisticasEdad.getMax());
        System.out.printf("AUTORES MAS JOVENES: %.2f años%n", estadisticasEdad.getMin());
        System.out.println("--------------------------------------------------");
    }

    private boolean validarAnio4Digitos(String anio) {
        return anio.matches("\\d{4}");
    }

    private void pausa() {
        System.out.println("\nPARA CONTINUAR DIGITE ENTER...");
        sc.nextLine();
    }

    private void anioNoValido() {
        System.out.println("""
                FORMATO DE AÑO INVALIDO (xxxx)
                ---------------------------------------------------------------------""");
    }
}
