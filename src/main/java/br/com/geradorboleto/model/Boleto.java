package br.com.geradorboleto.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class Boleto {
    private Pessoa sacado;
    private Pessoa beneficiario;
    private Banco banco;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataDocumento;
    private String numeroDocumento;
    private String nossoNumero;
    private String instrucoes;

    private String codigoBarras;
    private String linhaDigitavel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BRAZIL_LOCALE = new Locale("pt", "BR");

    // --- Construtor agora é PUBLIC ---
    public Boleto() {}

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

    // --- Setters agora são PUBLIC ---
    public void setSacado(Pessoa sacado) { this.sacado = sacado; }
    public void setBeneficiario(Pessoa beneficiario) { this.beneficiario = beneficiario; }
    public void setBanco(Banco banco) { this.banco = banco; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public void setDataDocumento(LocalDate dataDocumento) { this.dataDocumento = dataDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public void setNossoNumero(String nossoNumero) { this.nossoNumero = nossoNumero; }
    public void setInstrucoes(String instrucoes) { this.instrucoes = instrucoes; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public void setLinhaDigitavel(String linhaDigitavel) { this.linhaDigitavel = linhaDigitavel; }


    public String formatarLinhaDigitavel(String linha) {
        if (linha == null || linha.length() != 47) {
            return "Linha Digitável Inválida";
        }
        return String.format("%s.%s %s.%s %s.%s %s %s",
                linha.substring(0, 5),
                linha.substring(5, 10),
                linha.substring(10, 15),
                linha.substring(15, 21),
                linha.substring(21, 26),
                linha.substring(26, 32),
                linha.substring(32, 33),
                linha.substring(33)
        );
    }

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
        if (banco != null) sb.append("Agência/Código Beneficiário: ").append(banco.getAgencia()).append(" / ").append(banco.getContaCorrente()).append("\n");
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