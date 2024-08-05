package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.model.enums.Categoria;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=53fd5cbd";
    private String json;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;
    private SerieRepository repositorio;
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {

        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                     1- Buscar séries
                     2- Buscar episódios
                     3- Listar séries buscadas
                     4- Buscar série por título
                     5- Buscar séries por ator
                     6- Top 5 séries
                     7- Buscar séries por categoria
                     8- Buscar séries por total de temporada
                     9- Buscar episódio por trecho
                    10- Buscando top 5 episódios de uma série
                   
                    0- Sair
                    """;
            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    listarEpisodiosBuscado();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTotalTemporada();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("opção inválida");
            }
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        //series.add(new Serie(dados));
        repositorio.save(new Serie(dados));
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.print("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine();
        json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void listarEpisodiosBuscado() {
        listarSeriesBuscadas();
        System.out.print("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(x -> x.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();


        if(serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {

                json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo()
                        .replace(" ", "+") + "&Season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numeroTemp(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);

            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void buscarSeriePorTitulo() {
        System.out.print("Digite o nome da série para busca:");
        var nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.print("Qual o nome para busca? ");
        var nomeDoAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor ?");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesEncontradas =
                repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeDoAtor, avaliacao);
        seriesEncontradas.forEach(x ->
                System.out.println("Série: " + x.getTitulo() + " | Avaliação: " + x.getAvaliacao()));
    }

    private void buscarTop5Series() {
        series = repositorio.limitaABuscaSeries5();
        series.forEach(x ->
                System.out.println("Série: " + x.getTitulo() + " | Avaliação: " + x.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que gênero?");
        var categoriaEscolhida = leitura.nextLine();

        series = repositorio.buscarSeriePorGenero(Categoria.fromPortugues(categoriaEscolhida));
        System.out.println("Séries da categoria: " + categoriaEscolhida);
        series.forEach(System.out::println);
    }

    private void buscarSeriesPorTotalTemporada() {
        System.out.print("Digite o total de temporadas: ");
        var totalTemporada = leitura.nextInt();
        leitura.nextLine();

        System.out.print("Digite a avaliação: ");
        var avaliacaoLimite = leitura.nextDouble();
        leitura.nextLine();

        series = repositorio.seriesPorTemporadaEAvaliacao(totalTemporada, avaliacaoLimite);
        series.forEach(System.out::println);
    }

    private void buscarEpisodioPorTrecho() {
        System.out.print("Qual o nome do episódio para busca? ");
        var trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episodio %s - %s\n",
                        e.getSerie().getTitulo(), e.getSeason(), e.getNumberEp(), e.getTitle()));
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episodio %s - %s Avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getSeason(), e.getNumberEp(), e.getTitle(), e.getAssessment()));
        }
    }
}
