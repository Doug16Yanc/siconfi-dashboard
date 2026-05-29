package api.client;

import api.dto.RreoResponse;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SiconfiClient {

    private static final String BASE =
            "https://apidatalake.tesouro.gov.br/ords/siconfi/tt";

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final Gson gson = new Gson();

    /**
     * Busca RREO de um ente específico.
     * @param ano       ex: 2025
     * @param periodo   bimestre: 1 a 6
     * @param anexo     ex: "RREO-Anexo 01"
     * @param idEnte    código IBGE: "23" = Ceará
     * @param esfera    "E" = Estado, "U" = União, "M" = Município
     */
    public RreoResponse fetchRreo(int ano, int periodo,
                                  String anexo, String idEnte, String esfera) {
        String url = BASE + "/rreo"
                + "?an_exercicio=" + ano
                + "&nr_periodo="   + periodo
                + "&co_tipo_demonstrativo=RREO"
                + "&no_anexo="     + URLEncoder.encode(anexo, StandardCharsets.UTF_8)
                + "&id_ente="      + idEnte
                + "&co_esfera="    + esfera;

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful())
                throw new IOException("Siconfi HTTP " + res.code());
            String body = res.body().string();

            System.out.println("[API] JSON bruto (" + anexo + "): "
                    + body.substring(0, Math.min(500, body.length())));

            return gson.fromJson(body, RreoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Erro Siconfi: " + e.getMessage(), e);
        }
    }

    public RreoResponse fetchAnexo01CE(int periodo) {
        return fetchRreo(2025, periodo, "RREO-Anexo 01", "23", "E");
    }

    public RreoResponse fetchAnexo02CE(int periodo) {
        return fetchRreo(2025, periodo, "RREO-Anexo 02", "23", "E");
    }

    public RreoResponse fetchAnexo03CE(int periodo) {
        return fetchRreo(2025, periodo, "RREO-Anexo 03", "23", "E");
    }

    public RreoResponse fetchAnexo06CE(int periodo) {
        return fetchRreo(2025, periodo, "RREO-Anexo 06", "23", "E");
    }
}