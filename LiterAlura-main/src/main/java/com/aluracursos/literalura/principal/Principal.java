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


    // Encuentra el primer título con el que se busca
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

        // Verificar si el libro ya existe en la base de datos
        Optional<Libro> libroExistente = libroRepository.findByTitulo(librosDB.titulo());
        if (libroExistente.isPresent()) {
            System.out.println("""
                    __________________________________________________
                    LIBRO YA EXISTE EN LA BASE DE DATOS
                    """);

            // El libro existe en la base de datos y lo muestro:
            // Crear y mostrar DTO del libro guardado
            LibroDatos libroDatos = new LibroDatos(
                    libroExistente.get().getId(),
                    libroExistente.get().getTitulo(),
                    libroExistente.get().getAutores().stream().map(autor -> new AutorDatos(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                            .collect(Collectors.toList()),
                    String.join(", ", libroExistente.get().getIdiomas()),
                    libroExistente.get().getNumeroDeDescargas()
            );

            // Imprimir detalles del libro ya registrado
            System.out.printf(
                    """
                            -------------------TITULO-------------------
                            Título: %s
                            Autor: %s
                            Idioma: %s
                            N° Descargas: %.2f%n""", libroDatos.titulo(),
                    libroDatos.autores().stream().map(AutorDatos::nombre).collect(Collectors.joining(", ")),
                    libroDatos.idiomas(),
                    libroDatos.numeroDeDescargas()
            );
            System.out.println("--------------------------------------------------");
            pausa();
            return;
        }

        // Si el libro existe procesamos los autores
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

        // Crear el libro con los datos de librosDB y añadir los autores
        Libro libro = new Libro(librosDB);
        libro.setAutores(autores);
        // Guardar el libro junto con sus autores en la base de datos
        libroRepository.save(libro);

        // Crear y mostrar DTO del libro guardado
        LibroDatos libroDatos = new LibroDatos(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutores().stream().map(autor -> new AutorDatos(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                        .collect(Collectors.toList()),
                String.join(", ", libro.getIdiomas()),
                libro.getNumeroDeDescargas()
        );

        // Imprimir detalles del libro registrado
        System.out.printf(
                """
                        -------------------TITULO-------------------
                        Título: %s
                        Autor: %s
                        Idioma: %s
                        N° Descargas: %.2f%n""", libroDatos.titulo(),
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
        // Buscar libro en la API
        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibro.replace(" ", "+")); // me trae un json
        // Convierto json a un objeto Java
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);

        // Encontrar el primer libro coincidente en la lista de resultados
        return datosBusqueda.listaResultados().stream()
                .filter(librosDB -> librosDB.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst()
                .orElse(null); // Devolver null si no se encuentra el libro
    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAllWithAutores(); // uso una de las dos formas del LibroRepository
//        libros = libroRepository.findAll();

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
//        autores = autorRepository.findAll();

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

            // Validar que el año ingresado tenga 4 dígitos numéricos
            if (!validarAnio4Digitos(anioEstaVivo)) {
                anioNoValido();
                continue;
            }
            valorValido = true;
        } while (!valorValido);

        int anio = Integer.parseInt(anioEstaVivo);

        // Obtener autores vivos en el año especificado
        List<Autor> autoresVivos = autorRepository.findByFechaNacimientoBeforeAndFechaFallecimientoAfterOrFechaFallecimientoIsNullAndFechaNacimientoIsNotNull(String.valueOf(anio), String.valueOf(anio));

        // Filtrar autores con fechaNacimiento mayor a 100 años desde el año actual si fechaFallecimiento es null
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
                es - Español                it - Italiano
                en - Inglés                 ja - Japonés
                fr - Francés                pt - Portugués
                ru - Ruso                   zh - Chino Mandarín
                de - Alemán                 ar - Árabe
                """;
        String idiomaLibro;
        do {
            System.out.println(menuIdiomas);
            System.out.print("Ingresa el código del idioma del Libro a buscar [2 letras, ej: es]: ");
            idiomaLibro = sc.nextLine().toLowerCase();

            // Validar que el idioma ingresado tenga dos letras y no incluya números
            if (!idiomaLibro.matches("^[a-z]{2}$")) {
                System.out.println("""
                        CODIGO INVALIDO PARA EL IDIOMA SELECCIONADO
                        ------------------------------------------------""");
            }
        } while (!idiomaLibro.matches("^[a-z]{2}$"));

        // Lista de libros en idioma buscado
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

            // Crear el DTO para mostrar solo la información necesaria
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
                            Título: %s
                            Autor: %s
                            Idioma: %s
                            N° Descargas: %.2f%n""", libroDatos.titulo(),
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

            // Mostrar la información en el formato solicitado
            System.out.printf(
                    """
                            AUTOR
                            ------------------------------------------------------
                            Autor: %s
                            Fecha de Nacimiento: %s
                            Fecha de Fallecimiento: %s
                            Libros: %s%n""", autorDatos.nombre(),
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

        // Realizar la búsqueda en la base de datos
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

            // Verificación de que el año de inicio sea menor o igual que el de fin
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
                .filter(autor -> autor.getFechaNacimiento() != null) // filtro si tiene fecha de nacimiento
                .filter(autor -> {
                    // Si el autor no tiene fecha de fallecimiento, calculamos su edad actual
                    if (autor.getFechaFallecimiento() == null) {
                        int anioNacimiento = Integer.parseInt(autor.getFechaNacimiento().substring(0, 4));
                        int anioActual = fechaActual.getYear();
                        int edad = anioActual - anioNacimiento;
                        return edad < 100;
                    }
                    // incluimos autores con fecha fallecimiento
                    return true;
                })
                .mapToDouble(autor -> {
                    // Obtenemos el año de nacimiento
                    int anioNacimiento = Integer.parseInt(autor.getFechaNacimiento().substring(0, 4));

                    // Vemos si tiene fecha fallecimiento y calculamos la edad al fallecer
                    if (autor.getFechaFallecimiento() != null) {
                        int anioFallecimiento = Integer.parseInt(autor.getFechaFallecimiento().substring(0, 4));
                        return anioFallecimiento - anioNacimiento;
                    } else {
                        // si no tiene fecha fallecimiento calculamos edad actual
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
        // Validar que el año ingresado tenga 4 dígitos numéricos
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
