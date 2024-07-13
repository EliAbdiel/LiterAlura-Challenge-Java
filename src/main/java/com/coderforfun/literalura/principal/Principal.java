package com.coderforfun.literalura.principal;

import com.coderforfun.literalura.model.*;
import com.coderforfun.literalura.repository.AutorRepository;
import com.coderforfun.literalura.repository.LibroRepository;
import com.coderforfun.literalura.service.ConsumoApi;
import com.coderforfun.literalura.service.ConvierteDatos;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Scanner;

@Component
public class Principal {

    private ConsumoApi consumoApi = new ConsumoApi();

    private final String URL_BASE = "https://gutendex.com/books/?search=";

    private LibroRepository libroRepository;

    private AutorRepository autorRepository;

    private Scanner teclado = new Scanner(System.in);

    private ConvierteDatos conversor = new ConvierteDatos();

    public Principal(LibroRepository libro, AutorRepository autor) {

        this.libroRepository = libro;
        this.autorRepository = autor;
    }

    public void muestraElMenu() {

        var opcion = -1;

        while(opcion != 0) {

            var menu = """
                    1 - Buscar libros por título
                    2 - Mostrar listado de libros
                    3 - Autores vivos en determinado año
                    4 - Buscar libros por idioma
                    5 - Top 10 libros más descargados
                    
                    0 - Salir
                    """;

            System.out.println(menu);

            while (!teclado.hasNextInt()) {
                System.out.println("Formato inválido, ingrese un número que " +
                        "esté disponible en el menú!");
                teclado.nextLine();
            }

            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {

                case 1 -> busquedaDeLibroPorTitulo();
                case 2 -> listaDeTodosLosLibros();
                case 3 -> listaDeAutoresVivosPorAnio();
                case 4 -> buscarLibroPorIdioma();
                case 5 -> top10LibrosMasDescargados();

                case 0 -> {
                    System.out.println("Saliendo de la aplicación");
                    System.exit(0);
                }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private DatosResultado getDatosLibro() {

        System.out.println("Escribe el nombre del libro que desea buscar: ");

        var libro = teclado.nextLine();

        var json = consumoApi.obtenerDatos(URL_BASE + libro.replace(" ", "+"));

        return conversor.obtenerDatos(json, DatosResultado.class);
    }

    private void busquedaDeLibroPorTitulo() {

        DatosResultado datos = getDatosLibro();

        if (!datos.resultados().isEmpty()) {
            DatosLibro datosLibro = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibro.autor().get(0);
            System.out.println("Título: " + datosLibro.titulo());
            System.out.println("Autor: " + datosAutor.nombre());
            Autor autor = new Autor(datosAutor);
            autorRepository.save(autor);
            libroRepository.save(new Libro(datosLibro, autor));
        } else {
            System.out.println("El libro buscado no se encuentra. Pruebe con otro.");
        }
    }


    @Transactional
    private void listaDeTodosLosLibros() {

        try {
            List<Libro> libro = libroRepository.findAll();
            if (libro.isEmpty()) {
                System.out.println("No hay ningún Libro registrado.");
            } else {
                libro.forEach(l -> {
                    System.out.println("Título: " + l.getTitulo());
                    System.out.println("Autor: " + l.getAutor().getNombre());
                    System.out.println("Número de descargas: " + l.getNumeroDeDescargas());
                    System.out.println("Idiomas: " + String.join(", ", l.getIdiomas()));
                    System.out.println();
                });
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al listar los libros.");
            e.printStackTrace();
        }
    }


    private void listaDeAutoresVivosPorAnio() {

        System.out.println("Escribe el año para buscar autores vivos: ");

        while (!teclado.hasNextInt()) {
            System.out.println("Formato inválido, ingrese un año válido.");
            teclado.nextLine();
        }

        var anio = teclado.nextInt();
        teclado.nextLine();

        String strAnio = String.valueOf(anio);

        List<Autor> autoresVivos = autorRepository.autorVivoEnDeterminadoAnio(strAnio);

        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año especificado.");
        } else {
            System.out.println("Autores vivos en " + anio);
            autoresVivos.forEach(System.out::println);
            System.out.println();
        }
    }

    private void buscarLibroPorIdioma() {

        System.out.println("""
                1 - Español (ES)
                2 - Inglés (EN)
                3 - Regresar al menú principal
                
                Por favor, seleccione un idioma para consultar:
                """);

        var opcion = teclado.nextInt();
        teclado.nextLine();

        List<Libro> libro;

        switch (opcion) {
            case 1:
                libro = libroRepository.findByIdiomasContains("es");
                if (!libro.isEmpty()) {
                    libro.forEach(System.out::println);
                } else {
                    System.out.println("No hay ningún libro registrado en Español.");
                }
                break;
            case 2:
                libro = libroRepository.findByIdiomasContains("en");
                if (!libro.isEmpty()) {
                    libro.forEach(System.out::println);
                } else {
                    System.out.println("No hay ningún libro registrado en Inglés.");
                }
                break;
            case 3:
                muestraElMenu();
                break;
            default:
                System.out.println("La opción seleccionada no es válida.");
        }
    }

    private void top10LibrosMasDescargados() {

        var json = consumoApi.obtenerDatos(URL_BASE + "&sort=download_count&order=desc&limit=10");

        DatosResultado datos = conversor.obtenerDatos(json, DatosResultado.class);
        if (!datos.resultados().isEmpty()) {
            System.out.println("Top 10 Libros Más Descargados.");
            for (int i = 0; i < Math.min(10, datos.resultados().size()); i++) {
                DatosLibro datosLibro = datos.resultados().get(i);
                System.out.println("Título: " + datosLibro.titulo());
                System.out.println("Autor: " + datosLibro.autor().get(0).nombre());
                System.out.println("Idioma: " + datosLibro.idiomas().get(0));
                System.out.println("Número de descargas: " + datosLibro.numeroDeDescargas());
                System.out.println();
            }
        } else {
            System.out.println("No se encontraron libros para el top 10 de descargas.");
        }
    }

}
