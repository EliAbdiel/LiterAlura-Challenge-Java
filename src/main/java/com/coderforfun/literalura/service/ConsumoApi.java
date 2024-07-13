package com.coderforfun.literalura.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsumoApi {

    public String obtenerDatos(String url) {

        //Construyendo el Cliente para Solicitudes a la API
        HttpClient client = HttpClient.newHttpClient();

        //Para configurar y personalizar nuestras solicitudes a la API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        //Para gestionar las respuestas recibidas de la API
        HttpResponse<String> response = null;

        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String json = response.body();

        return json;
    }
}
