package api.client;

import api.dto.BcbSerieItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BcbClient {

    private static final int SERIE_SELIC_META = 432;
    private static final int SERIE_IPCA       = 433;
    private static final int SERIE_DOLAR_PTAX = 1;

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    public double fetchDolar() {
        return fetchUltimoValor(SERIE_DOLAR_PTAX, 1);
    }

    public double fetchIpca12m() {
        List<BcbSerieItem> items = fetchSerie(SERIE_IPCA, 12);
        if (items == null || items.isEmpty()) return 0.0;

        double fatorAcumulado = items.stream()
                .mapToDouble(item -> {
                    double valor = item.valorDouble();
                    return 1.0 + (valor / 100.0);
                })
                .reduce(1.0, (a, b) -> a * b);

        return (fatorAcumulado - 1.0) * 100.0;
    }

    public double fetchSelic() {
        List<BcbSerieItem> items = fetchSerie(SERIE_SELIC_META, 1);
        if (items == null || items.isEmpty()) return 0.0;
        BcbSerieItem last = items.get(items.size() - 1);
        System.out.println("[BCB] SELIC raw — data: " + last.data + " valor: " + last.valor);
        return last.valorDouble();
    }

    private double fetchUltimoValor(int serie, int ultimos) {
        List<BcbSerieItem> items = fetchSerie(serie, ultimos);
        if (items == null || items.isEmpty()) return 0.0;

        return items.get(items.size() - 1).valorDouble();
    }

    public List<BcbSerieItem> fetchSerie(int serie, int ultimos) {
        String url = "https://api.bcb.gov.br/dados/serie/bcdata.sgs."
                + serie + "/dados/ultimos/" + ultimos + "?formato=json";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful())
                throw new IOException("BCB HTTP " + res.code());

            String bodyJson = res.body().string();
            return gson.fromJson(bodyJson, new TypeToken<List<BcbSerieItem>>(){}.getType());
        } catch (IOException e) {
            throw new RuntimeException("Erro BCB (Série " + serie + "): " + e.getMessage(), e);
        }
    }
}
