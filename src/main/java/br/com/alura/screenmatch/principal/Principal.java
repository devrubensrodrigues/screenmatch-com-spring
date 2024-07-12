package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpsodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=53fd5cbd";
    public DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                .collect(Collectors.toList());

        System.out.println("Top 10 avaliados");
        dadosEpsodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
                .sorted(Comparator.comparing(DadosEpsodio::avaliacao).reversed())
                .peek(e -> System.out.println("Ordenação " + e))
                .limit(10)
                .peek(e -> System.out.println("Limitando em 10 " + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Mapeamento " + e))
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas
                .stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numeroTemp(), d)))
                .collect(Collectors.toList());
//
        System.out.println("__________________________________");
        episodios.forEach(System.out::println);

        System.out.println("Digite um trecho de um episodio:");
        var trechoTitulo = leitura.nextLine();

//        1- Cria um Stream a partir da lista episodios.
        Optional<Episodio> episodioBuscado = episodios.stream()
        //2- Filtra os elementos do Stream, mantendo apenas os episódios cujo título(convertido para minúsculas) contém o trechoTitulo (também convertido para minúsculas).
                .filter(e -> e.getTitle().toLowerCase().contains(trechoTitulo.toLowerCase()))
        //3- Encontra o primeiro episódio que corresponde ao critério definido pelo filter e o retorna como um Optional<Episodio>. Um Optional é um contêiner que pode ou não conter um valor não nulo.
                .findFirst();
        //4- Verifica se um episódio correspondente foi encontrado (isPresent() retorna true se o Optional contém um valor, e false caso contrário).
        if(episodioBuscado.isPresent()) {
            System.out.println("Episódio encontrado!!");
            System.out.println("Temporada: " + episodioBuscado.get().getSeason());
        } else {
            System.out.println("Episódio não encontrado");
        }

        System.out.println("-------------------Filtrado por ano-----------------------");
        System.out.println();

        System.out.print("A partir de que ano você deseja ver os episodios?");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        episodios.stream()
            .filter(e -> e.getDate() != null && e.getDate().isAfter(dataBusca))
            .forEach(e -> System.out.println(
                    "Temporada: " + e.getSeason()
                    + " | Episódio: " + e.getTitle()
                    + " | Data de lançamento: " + e.getDate().format(formatador)
            ));

//      1- Isso declara uma variável avaliacoesTemporada do tipo Map<Integer, Double>, onde a chave é um Integer representando a temporada e o valor é um Double representando a média das avaliações dos episódios dessa temporada.
        Map<Integer, Double> avaliacoesTemporada = episodios.stream()
//      2- Aplica um filtro para incluir apenas os episódios cuja avaliação (getAssessment()) é maior que 0.0.
                .filter(e -> e.getAssessment() > 0.0)
//      3- .collect(...): Coleta os elementos do Stream em uma estrutura de dados.
//      Collectors.groupingBy(Episodio::getSeason, Collectors.averagingDouble(Episodio::getAssessment)): Agrupa os episódios por temporada (chave) e calcula a média das avaliações (valor) de cada grupo.
                .collect(Collectors.groupingBy(Episodio::getSeason,
                        Collectors.averagingDouble((Episodio::getAssessment))));
        System.out.println(avaliacoesTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAssessment() > 0.0)
//  --O método collect usa um Collector para resumir as estatísticas dos valores de
//  --assessment. O Collector usado é Collectors.summarizingDouble, que aceita uma função
//  --para extrair o valor duplo de cada elemento no stream. Aqui,
//  --Episodio::getAssessment é uma referência de método que pega o valor de assessment de
//  --cada objeto Episodio.
                .collect(Collectors.summarizingDouble(Episodio::getAssessment));
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor ep: " + est.getMax());
        System.out.println("Pior ep: " + est.getMin());
        System.out.println("Total ep avaliados: " + est.getCount());
    }
}
