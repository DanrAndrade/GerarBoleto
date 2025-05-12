package br.com.geradorboleto.model;

public class Endereco {
    private String logradouro;
    private String bairro;
    private String cep;
    private String cidade;
    private String uf;

    // Construtor
    public Endereco(String logradouro, String bairro, String cep, String cidade, String uf) {
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.uf = uf;
    }

    // Getters e Setters (opcionalmente usar Lombok para reduzir código)
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    @Override
    public String toString() {
        // Formata o endereço para exibição simples
        return String.format("%s, %s - CEP: %s - %s/%s",
                logradouro != null ? logradouro : "",
                bairro != null ? bairro : "",
                cep != null ? cep : "",
                cidade != null ? cidade : "",
                uf != null ? uf : "");
    }
}