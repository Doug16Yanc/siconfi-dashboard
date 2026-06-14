package api.dto;

import com.google.gson.annotations.SerializedName;

public class RreoItem {
    @SerializedName("exercicio")    public int    an_exercicio;
    @SerializedName("periodo")      public int    nr_periodo;
    @SerializedName("anexo")        public String no_anexo;
    @SerializedName("esfera")       public String co_esfera;
    @SerializedName("uf")           public String co_uf;
    @SerializedName("instituicao")  public String no_uf;
    @SerializedName("conta")        public String no_conta;
    @SerializedName("cod_conta")    public String co_conta;
    @SerializedName("valor")        public double vl_resultado;
    @SerializedName("periodicidade")public String no_periodo;
    @SerializedName("cod_ibge")     public String id_ente;
    @SerializedName("coluna")       public String coluna;
    @SerializedName("rotulo")       public String rotulo;
    @SerializedName("populacao") public long populacao;
}
