package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
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
                    7- Buscar 
                   
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
        Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
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
        series = repositorio.findTop5ByOrderByAvaliacaoDesc();
        series.forEach(x ->
                System.out.println("Série: " + x.getTitulo() + " | Avaliação: " + x.getAvaliacao()));
    }
}
