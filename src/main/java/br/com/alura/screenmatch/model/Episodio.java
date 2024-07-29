package br.com.alura.screenmatch.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "episodios")
public class Episodio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer season;
    private String title;
    private Integer numberEp;
    private Double assessment;
    private LocalDate date;
    @ManyToOne
    private Serie serie;
//Código para revisão
    public Episodio(Integer numeroTemp, DadosEpsodio dadosEpsodio) {
        this.season = numeroTemp;
        this.title = dadosEpsodio.titulo();
        this.numberEp = dadosEpsodio.numeroEp();
        try {
            this.assessment = Double.parseDouble(dadosEpsodio.avaliacao());
        } catch(NumberFormatException ex) {
            this.assessment = 0.0;
        }
        try {
            this.date = LocalDate.parse(dadosEpsodio.dataLancamento());
        } catch(DateTimeParseException ex) {
            this.date = null;
        }
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNumberEp() {
        return numberEp;
    }

    public void setNumberEp(Integer numberEp) {
        this.numberEp = numberEp;
    }

    public Double getAssessment() {
        return assessment;
    }

    public void setAssessment(Double assessment) {
        this.assessment = assessment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    @Override
    public String toString() {
        return "season=" + season +
                ", title='" + title + '\'' +
                ", numberEp=" + numberEp +
                ", assessment=" + assessment +
                ", date=" + date;
    }
}
