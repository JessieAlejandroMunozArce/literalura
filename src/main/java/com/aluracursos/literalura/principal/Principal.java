package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private List<Autor> listaAutores;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository){
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar libro por nombre y registrar.
                    2 - Listar libros registrados.
                    3 - Listar autores registrados.
                    4 - Listar autores vivos en un determinado año.
                    5 - Listar libros por idioma.
                                                 
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroWeb();
                    break;
                case 2:
                    mostrarLibrosConsultados();
                    break;
                case 3:
                    mostrarAutoresRegistrados();
                    break;
                case 4:
                    mostrarAutoresVivos();
                    break;
                case 5:
                    mostrarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private void mostrarLibrosPorIdioma() {
        var menu = """
                    Ingresa el idioma buscado: 
                    
                    ES - Español.
                    EN - Ingles.
                    FR - Frances.
                    IT - Italiano.
                    PT - Portugues.
                                                 
                    """;
        System.out.println(menu);
        String idioma = teclado.nextLine();

        try{
            List<Libro> librosPorIdioma = libroRepository.librosPorIdioma(Idioma.fromString(idioma));
            librosPorIdioma.forEach(l -> System.out.println(
                    "Título: " + l.getTitulo() +
                    "\nIdioma: " + l.getIdioma() +
                    "\nAutor: " + l.getAutor().stream().map(Autor::getNombre).collect(Collectors.joining()) +
                    "\nNumero de descargas: " + l.getNumeroDescargas() + "\n"
            ));
        } catch (IllegalArgumentException e){
            System.out.println("Selecciona un idioma válido.\n");
        }

    }

    private void mostrarAutoresVivos() {
        System.out.println("Ingresa el año: ");
        int anio = teclado.nextInt();
        listaAutores = autorRepository.autoresVivos(anio);

        System.out.println("Los autores vivos en ese año son: ");
        listaAutores.stream()
                .forEach(a -> System.out.println("nombre: " + a.getNombre() + "\n"
                        + "Fecha de nacimiento: " + a.getNacimiento() + "\n"
                        + "Fecha de muerte: " + a.getMuerte() + "\n"));
    }

    private void mostrarAutoresRegistrados() {
        listaAutores = autorRepository.findAll();
        listaAutores.stream()
                .forEach(a -> System.out.println("nombre: " + a.getNombre() + "\n"
                    + "Fecha de nacimiento: " + a.getNacimiento() + "\n"
                    + "Fecha de muerte: " + a.getMuerte() + "\n"));

    }

    private DatosLibro buscarLibroEnAPI(){
        System.out.println("Ingresa el nombre del libro: ");
        var tituloBuscado = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloBuscado.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloBuscado.toUpperCase()))
                .findFirst();
        if(libroBuscado.isPresent()){
            System.out.println("Libro encontrado ");
            return libroBuscado.get();
        } else {
            System.out.println(" Libro no encontrado");
            return null;
        }
    }

    private void buscarLibroWeb(){
        Optional<DatosLibro> datos = Optional.ofNullable(buscarLibroEnAPI());
        if (datos.isPresent()){
            DatosLibro datosLibro = datos.get();
            Libro libro = new Libro(datosLibro);
            List<Autor> autores = new ArrayList<>();
            for(DatosAutor datosAutor : datosLibro.autor()){
                Autor autor = new Autor(datosAutor);
                autor.setLibros(libro);
                autores.add(autor);
            }
            libro.setAutor(autores);
            try {
                libroRepository.save(libro);
                System.out.println("Libro " + libro.getTitulo() + " guardado...");
            } catch (DataIntegrityViolationException e) {
                System.out.println("El libro ya existe en tu base de datos... \n");
            }
        }
    }
    private void mostrarLibrosConsultados() {
        List<Libro> librosBaseDeDatos = libroRepository.findAll();
        librosBaseDeDatos.forEach(l -> System.out.println("Título: " + l.getTitulo() +
                " - Idioma: " + l.getIdioma() +
                " - Autor: " + l.getAutor().stream().map(Autor::getNombre).collect(Collectors.joining()) +
                " - Número de descargas: " + l.getNumeroDescargas() + "\n"));
    }
}






















