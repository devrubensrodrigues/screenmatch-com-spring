package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosEpsodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var consumoAPI = new ConsumoAPI();
		var json = consumoAPI.obterDados(
				"https://www.omdbapi.com/?t="
				+ "gilmore+girls&"
				+ "&apikey=53fd5cbd");
		System.out.println(json);

		ConverteDados conversor = new ConverteDados();
		DadosSerie serie1= conversor.obterDados(json, DadosSerie.class);

		System.out.println("Formato convertido em DadosSerie:");
		System.out.println(serie1);
		json = consumoAPI.obterDados(
				"https://www.omdbapi.com/?t="
				+ "gilmore+girls&season=1&episode=2"
				+ "&apikey=53fd5cbd");

		DadosEpsodio dadosEpsodio = conversor.obterDados(json,
				DadosEpsodio.class);
		System.out.println("Formato convertido em DadosEpsodio");
		System.out.println(dadosEpsodio);

		System.out.println("Formato convertido em DadosTemporada");
		List<DadosTemporada> list = new ArrayList<>();
		DadosTemporada dadosTemporada;
		for (int i = 1; i <= serie1.totalTemporadas(); i++){
			json = consumoAPI.obterDados(
					"https://www.omdbapi.com/?t="
							+ "gilmore+girls&season="
							+ i
							+ "&apikey=53fd5cbd");
			list.add(dadosTemporada = conversor.obterDados(json,
					DadosTemporada.class));
		}
		list.forEach(System.out::println);
	}
}
