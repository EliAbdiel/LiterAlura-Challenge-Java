
# LiterAlura - Challenge Java

En este desafío de programación, se te invita a construir tu propio catálogo de libros llamado `LiterAlura`. Aprenderás a hacer solicitudes a una API de libros, manipular datos JSON, almacenarlos en una base de datos y, finalmente, filtrar y mostrar libros y autores de interés.

![Insignia Spring](img/badge-literalura.png)


## Feature

Desarrollar un Catálogo de Libros con interacción textual (vía consola) que ofrezca al menos 5 opciones de interacción. Los libros se obtendrán a través de una API específica.

**Pasos para completar el desafío:**

- Configuración del ambiente Java.
- Creación del proyecto.
- Consumo de la API.
- Análisis de la respuesta JSON.
- Inserción y consulta en la base de datos.
- Exhibición de resultados a los usuarios.
## Tech Stack

**Configuración al crear el proyecto en Spring Initializr**

- `Java (versión 17 en adelante)`
- `Maven (Initializr utiliza la versión 4)`
- `Spring Boot (versión 3.2.3)`
- `Proyecto en JAR`

**Dependencias para agregar al crear el proyecto en Spring Initializr**

- `Spring Data JPA`
- `Postgres Driver`

![Postgres](https://img.shields.io/badge/Postgres-316192?style=flat&logo=postgresql&logoColor=white)
 ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
 ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
## Code Review

#### Construyendo el Cliente para Solicitudes (HttpClient)

Empleamos la clase `HttpClient` para realizar solicitudes a la API de libros y obtener datos esenciales.

```java
HttpClient client = HttpClient.newHttpClient();
```

#### Construyendo la Solicitud (HttpRequest)

La clase HttpRequest en Java nos brinda un control detallado sobre los parámetros de nuestras solicitudes, lo que resulta esencial para adaptar la consulta a nuestras necesidades específicas.

```java
HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
```

#### Construyendo la Respuesta (HttpResponse)

La interfaz `HttpResponse` en Java ofrece una estructura que permite analizar y acceder a los diferentes elementos de una respuesta HTTP, que normalmente se presenta en formato JSON.

```java
HttpResponse<String> response = null;

        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String json = response.body();

        return json;
```

#### Persistencia de datos

Implementación de persistencia de datos utilizando Spring Data JPA para la creación de clases de entidad/modelo para Libro y Autor, así como también sus respectivas interfaces de repositorio para manejar inserción y consultas en la base de datos.

```java
@Table(name = "libros")
@Entity(name = "Libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;

    @ManyToOne()
    private Autor autor;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> idiomas;

    private Integer numeroDeDescargas;

    //getters, setters y metodos omitidos . . .
}
```
```java
@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByIdiomasContains(String idioma);
}
```
```java
@Table(name = "autores")
@Entity(name = "Autor")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    private String fechaDeNacimiento;

    private String fechaDeFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libro;

    //getters, setters y metodos omitidos . . .
}
```
```java
@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE a.fechaDeNacimiento <= :fecha AND a.fechaDeFallecimiento >= :fecha")
    List<Autor> autorVivoEnDeterminadoAnio(@Param("fecha") String fecha);
}
```

#### Consultar por título del libro

Permite buscar libros por título utilizando la API de [Gutendex](https://gutendex.com/) y guarda el primer resultado encontrado en la base de datos.

```java
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
            System.out.println("El libro buscado no se encuentra.");
        }
    }
```

####  Listado de todos los libros

Muestra por consola todos los libros guardados en la base de datos.

```java
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
```

#### Listando autores vivos en determinado año

Muestra por consola los autores que estaban vivos en un año específico, basado en la información almacenada en la base de datos.

```java
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
```

#### Listando libros por idiomas

Filtra y muestra por consola los libros según el idioma especificado.

```java
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
```

#### Top 10 libros más descargados

Presenta los datos de los 10 libros más descargados, siendo una consulta directamente hecha en la API o en la base de datos.

```java
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
```
## Run Locally

Clone the project

```bash
  git clone https://github.com/EliAbdiel/LiterAlura-Challenge-Java.git
```

Go to the project directory

```bash
  cd literalura
```


## Authors

- [@Elí Abdiel Chan López](https://www.github.com/EliAbdiel)


## License

Este proyecto está bajo la licencia [MIT](https://choosealicense.com/licenses/mit/)

