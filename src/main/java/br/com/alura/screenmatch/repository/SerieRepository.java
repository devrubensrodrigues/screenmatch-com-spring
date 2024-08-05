package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.model.enums.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeDoAtor, Double avaliacao);

    @Query("select s from Serie s order by s.avaliacao desc limit 5")
    List<Serie> limitaABuscaSeries5();
    //List<Serie> findTop5ByOrderByAvaliacaoDesc();

    @Query("SELECT s FROM Serie s WHERE genero = :categoria")
    List<Serie> buscarSeriePorGenero(Categoria categoria);

    //List<Serie> findByTotalTemporadasAndAvaliacaoGreaterThanEqual(Integer temporadas, Double avaliacao);

    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriesPorTemporadaEAvaliacao(int totalTemporadas, double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.title ILIKE %:trechoEpisodio%")
    List<Episodio> episodiosPorTrecho(String trechoEpisodio);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.assessment DESC LIMIT 5")
    List<Episodio> topEpisodiosPorSerie(Serie serie);
}
