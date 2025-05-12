package br.com.geradorboleto.model;

// Import necessário se Endereco estivesse em outro pacote (não é o caso aqui)
// import br.com.geradorboleto.model.Endereco;

public class Pessoa {
    private String nome;
    private String documento; // CPF ou CNPJ
    private Endereco endereco;

    // Construtor
    public Pessoa(String nome, String documento, Endereco endereco) {
        this.nome = nome;
        this.documento = documento;
        this.endereco = endereco;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        // Formata os dados da pessoa para exibição simples
        String endStr = (endereco != null) ? endereco.toString() : "Endereço não informado";
        return String.format("Nome: %s, Documento: %s\nEndereço: %s",
                nome != null ? nome : "",
                documento != null ? documento : "",
                endStr);
    }
}