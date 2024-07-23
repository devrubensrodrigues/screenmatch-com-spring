package br.com.alura.screenmatch.model;

import br.com.alura.screenmatch.model.enums.Categoria;

import java.util.OptionalDouble;

public class Serie {

    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;
    private String atores;
    private String sinopse;
    private Categoria genero;
    private String poster;

    public Serie(DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        this.avaliacao = OptionalDouble.of(Double.valueOf(dadosSerie.avaliacao())).orElse(0);
        this.atores = dadosSerie.atores();
        this.sinopse = dadosSerie.sinopse();
        this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        this.poster = dadosSerie.poster();
    }
}
