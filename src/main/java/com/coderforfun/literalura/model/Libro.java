package com.coderforfun.literalura.model;

import jakarta.persistence.*;

import java.util.List;

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

    public Libro() {}

    public Libro(DatosLibro datosLibro, Autor autor) {
        this.titulo = datosLibro.titulo();
        this.autor = autor;
        this.idiomas = datosLibro.idiomas();
        this.numeroDeDescargas = datosLibro.numeroDeDescargas();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public List<String> getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(List<String> idiomas) {
        this.idiomas = idiomas;
    }

    public Integer getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    public void setNumeroDeDescargas(Integer numeroDeDescargas) {
        this.numeroDeDescargas = numeroDeDescargas;
    }

    @Override
    public String toString() {
        return "Libro: " + '\n' +
                "titulo= " + titulo + '\n' +
                "autor= " + autor.getNombre() + '\n' +
                "idiomas= " + idiomas + '\n' +
                "numeroDeDescargas= " + numeroDeDescargas + '\n';
    }
}
