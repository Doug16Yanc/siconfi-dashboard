package api.dto;

public class BcbSerieItem {
    public String data;
    public String valor;

    public double valorDouble() {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
