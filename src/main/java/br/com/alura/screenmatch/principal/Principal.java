package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpsodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=53fd5cbd";
    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine()
                .replace(" ", "+");

        var json = consumo.obterDados(
        ENDERECO + nomeSerie
                + API_KEY);

        DadosSerie dados= conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
        DadosTemporada dadosTemporada;
        for (int i = 1; i <= dados.totalTemporadas(); i++){
            json = consumo.obterDados(
                    ENDERECO
                            + nomeSerie.replace(" ", "+")
                            + "&season="
                            + i
                            + API_KEY);
            dadosTemporada = conversor.obterDados(json,
                    DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t
                .episodios()
                .forEach(e -> System.out.println(e.titulo())));
        //Código para Revisão
        List<DadosEpsodio> dadosEpsodios = temporadas
                .stream()
                .flatMap(t -> t.episodios().stream())
                .toList();

        dadosEpsodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpsodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas
                .stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numeroTemp(), d)))
                .collect(Collectors.toList());

        System.out.println("__________________________________");
        episodios.forEach(System.out::println);
    }
}
