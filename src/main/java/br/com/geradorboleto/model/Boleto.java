package br.com.geradorboleto.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale; // Para formatação de moeda
import java.util.Objects; // Para validações

public class Boleto {
    private Pessoa sacado;
    private Pessoa beneficiario; // Cedente no exemplo K19
    private Banco banco;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataDocumento;
    private String numeroDocumento; // Número que identifica o boleto para o cedente
    private String nossoNumero; // Número formatado para exibição (pode incluir DV)
    private String instrucoes; // Opcional

    // Campos Calculados
    private String codigoBarras;
    private String linhaDigitavel;

    // Formatter padrão para datas e moeda
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BRAZIL_LOCALE = new Locale("pt", "BR");

    // Construtor protegido para ser usado pelo Builder
    protected Boleto() {}

    // --- Getters ---
    public Pessoa getSacado() { return sacado; }
    public Pessoa getBeneficiario() { return beneficiario; }
    public Banco getBanco() { return banco; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public LocalDate getDataDocumento() { return dataDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNossoNumero() { return nossoNumero; }
    public String getInstrucoes() { return instrucoes; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getLinhaDigitavel() { return linhaDigitavel; }

    // --- Setters (Usados pelo Builder) ---
    protected void setSacado(Pessoa sacado) { this.sacado = sacado; }
    protected void setBeneficiario(Pessoa beneficiario) { this.beneficiario = beneficiario; }
    protected void setBanco(Banco banco) { this.banco = banco; }
    protected void setValor(BigDecimal valor) { this.valor = valor; }
    protected void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    protected void setDataDocumento(LocalDate dataDocumento) { this.dataDocumento = dataDocumento; }
    protected void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    protected void setNossoNumero(String nossoNumero) { this.nossoNumero = nossoNumero; }
    protected void setInstrucoes(String instrucoes) { this.instrucoes = instrucoes; }
    protected void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    protected void setLinhaDigitavel(String linhaDigitavel) { this.linhaDigitavel = linhaDigitavel; }

    // --- Métodos Auxiliares ---

    // Formata a linha digitável com pontos e espaços para exibição
    public String formatarLinhaDigitavel(String linha) {
        if (linha == null || linha.length() != 47) {
            return "Linha Digitável Inválida";
        }
        // Formato: AAAAA.BBBBB CCCCC.DDDDD EEEEE.FFFFF G HHHHHHHHHHHHHH
        return String.format("%s.%s %s.%s %s.%s %s %s",
                linha.substring(0, 5),   // Campo 1 (parte 1)
                linha.substring(5, 10),  // Campo 1 (parte 2 + DV1)
                linha.substring(10, 15), // Campo 2 (parte 1)
                linha.substring(15, 21), // Campo 2 (parte 2 + DV2)
                linha.substring(21, 26), // Campo 3 (parte 1)
                linha.substring(26, 32), // Campo 3 (parte 2 + DV3)
                linha.substring(32, 33), // Campo 4 (DV Geral do Código de Barras)
                linha.substring(33)      // Campo 5 (Fator Vencimento + Valor)
        );
    }

    // Método toString para fácil visualização no console
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------- BOLETO BANCÁRIO --------------------------------\n");
        if (banco != null) sb.append("Banco: ").append(banco.getNomeBanco()).append(" (").append(banco.getNumeroFormatado()).append(")\n");
        if (beneficiario != null) {
            sb.append("Beneficiário: ").append(beneficiario.getNome()).append(" - ").append(beneficiario.getDocumento()).append("\n");
            if (beneficiario.getEndereco() != null) sb.append("Endereço Beneficiário: ").append(beneficiario.getEndereco()).append("\n");
        }
        sb.append("----------------------------------------------------------------------------------\n");
        if (sacado != null) {
            sb.append("Sacado: ").append(sacado.getNome()).append(" - ").append(sacado.getDocumento()).append("\n");
            if (sacado.getEndereco() != null) sb.append("Endereço Sacado: ").append(sacado.getEndereco()).append("\n");
        }
        sb.append("----------------------------------------------------------------------------------\n");
        if (dataVencimento != null) sb.append("Data Vencimento: ").append(dataVencimento.format(DATE_FORMATTER)).append("\t\t");
        if (banco != null) sb.append("Agência/Código Beneficiário: ").append(banco.getAgencia()).append(" / ").append(banco.getContaCorrente()).append("\n"); // Ajustar formato se necessário
        if (dataDocumento != null) sb.append("Data Documento: ").append(dataDocumento.format(DATE_FORMATTER)).append("\t\t");
        if (nossoNumero != null) sb.append("Nosso Número: ").append(nossoNumero).append("\n");
        if (numeroDocumento != null) sb.append("Número Documento: ").append(numeroDocumento).append("\t\t");
        if (banco != null) sb.append("Carteira: ").append(banco.getCarteira()).append("\n");
        if (valor != null) sb.append("Valor Documento: R$ ").append(String.format(BRAZIL_LOCALE, "%,.2f", valor)).append("\n");
        if (instrucoes != null && !instrucoes.isEmpty()) {
            sb.append("Instruções: ").append(instrucoes).append("\n");
        }
        sb.append("----------------------------------------------------------------------------------\n");
        sb.append("Linha Digitável: ").append(formatarLinhaDigitavel(linhaDigitavel)).append("\n");
        sb.append("Código de Barras: ").append(codigoBarras != null ? codigoBarras : "N/A").append("\n");
        sb.append("----------------------------------------------------------------------------------\n");

        return sb.toString();
    }
}