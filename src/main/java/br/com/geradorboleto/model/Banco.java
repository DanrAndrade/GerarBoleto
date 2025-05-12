package br.com.geradorboleto.model;

public class Banco {
    private String codigoBanco; // Ex: "001", "341", "237"
    private String nomeBanco;
    private String agencia; // Com dígito verificador, se houver, conforme formatação do banco
    private String contaCorrente; // Com dígito verificador, conforme formatação do banco
    private String carteira;

    // Construtor
    public Banco(String codigoBanco, String nomeBanco, String agencia, String contaCorrente, String carteira) {
        this.codigoBanco = codigoBanco;
        this.nomeBanco = nomeBanco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
        this.carteira = carteira;
    }

    // Getters
    public String getCodigoBanco() { return codigoBanco; }
    public String getNomeBanco() { return nomeBanco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public String getCarteira() { return carteira; }

    // Setters (usados pelos Builders para formatar/ajustar)
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }
    public void setCarteira(String carteira) { this.carteira = carteira; }

    // Retorna Código do Banco com Dígito Verificador (simples)
    public String getNumeroFormatado() {
        return codigoBanco + "-" + digitoVerificadorCodigoBanco(codigoBanco);
    }

    // Simples cálculo de dígito verificador para o código do banco (exemplo didático)
    private String digitoVerificadorCodigoBanco(String codigo) {
        if ("001".equals(codigo)) return "9";
        if ("341".equals(codigo)) return "7";
        if ("237".equals(codigo)) return "2";
        // Adicione outros bancos se necessário
        return "X"; // Padrão ou erro
    }
}