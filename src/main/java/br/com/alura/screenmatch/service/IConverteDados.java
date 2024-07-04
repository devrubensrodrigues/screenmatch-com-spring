package br.com.alura.screenmatch.service;
//Revisar toda a classe
public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
