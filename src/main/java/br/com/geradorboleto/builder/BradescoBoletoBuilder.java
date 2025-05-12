package br.com.geradorboleto.builder;

import br.com.geradorboleto.calculos.CalculosBoletoUtils;
import br.com.geradorboleto.calculos.Modulo; // Precisa do Módulo 11
import br.com.geradorboleto.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// Builder Concreto para Bradesco (Código 237)
public class BradescoBoletoBuilder implements BoletoBuilder {

    private static final String CODIGO_BANCO = "237";
    private static final String NOME_BANCO = "Banco Bradesco S.A.";

    private Pessoa sacado;
    private Pessoa beneficiario;
    private Banco banco;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataDocumento;
    private String numeroDocumento;
    private String nossoNumeroBase; // NN com 11 dígitos (sem DV)
    private String instrucoes;

    public BradescoBoletoBuilder() {
        this.banco = new Banco(CODIGO_BANCO, NOME_BANCO, "", "", "");
    }

    @Override
    public BoletoBuilder comSacado(String nome, String cpfCnpj, String logradouro, String bairro, String cep, String cidade, String uf) {
        Endereco endereco = new Endereco(logradouro, bairro, cep, cidade, uf);
        this.sacado = new Pessoa(nome, cpfCnpj, endereco);
        return this;
    }

    @Override
    public BoletoBuilder comBeneficiario(String nome, String cpfCnpj, String logradouro, String bairro, String cep, String cidade, String uf) {
        Endereco endereco = new Endereco(logradouro, bairro, cep, cidade, uf);
        this.beneficiario = new Pessoa(nome, cpfCnpj, endereco);
        return this;
    }

    @Override
    public BoletoBuilder comBanco(String agencia, String contaCorrente, String carteira) {
        // Bradesco: Agência 4 (sem DV), Conta 7 (sem DV), Carteira 2
        String agenciaLimpa = agencia != null ? agencia.replaceAll("[^0-9]", "") : "";
        String contaLimpa = contaCorrente != null ? contaCorrente.replaceAll("[^0-9]", "") : "";

        this.banco.setAgencia(CalculosBoletoUtils.zeroEsquerda(agenciaLimpa, 4));
        this.banco.setContaCorrente(CalculosBoletoUtils.zeroEsquerda(contaLimpa, 7));
        this.banco.setCarteira(CalculosBoletoUtils.zeroEsquerda(carteira, 2));
        return this;
    }

    @Override
    public BoletoBuilder comDatas(LocalDate dataVencimento, LocalDate dataDocumento) {
        this.dataVencimento = dataVencimento;
        this.dataDocumento = dataDocumento;
        return this;
    }

    @Override
    public BoletoBuilder comValores(BigDecimal valor, String numeroDocumento, String nossoNumero) {
        this.valor = valor;
        this.numeroDocumento = numeroDocumento;
        // Bradesco usa Nosso Número com 11 dígitos (sem DV)
        this.nossoNumeroBase = CalculosBoletoUtils.zeroEsquerda(nossoNumero, 11);
        return this;
    }

    @Override
    public BoletoBuilder comInstrucoes(String instrucoes) {
        this.instrucoes = instrucoes;
        return this;
    }

    @Override
    public Boleto build() {
        // --- Validações ---
        Objects.requireNonNull(sacado, "Sacado é obrigatório");
        Objects.requireNonNull(beneficiario, "Beneficiário é obrigatório");
        // ... outras validações ...
        if (nossoNumeroBase.length() != 11) throw new IllegalStateException("Nosso Número base Bradesco deve ter 11 dígitos.");
        if (banco.getAgencia().length() != 4) throw new IllegalStateException("Agência Bradesco deve ter 4 dígitos.");
        if (banco.getContaCorrente().length() != 7) throw new IllegalStateException("Conta Bradesco deve ter 7 dígitos (sem DV).");
        if (banco.getCarteira().length() != 2) throw new IllegalStateException("Carteira Bradesco deve ter 2 dígitos.");


        // --- Cria e Preenche Boleto (dados básicos) ---
        Boleto boleto = new Boleto();
        boleto.setSacado(sacado);
        boleto.setBeneficiario(beneficiario);
        boleto.setBanco(banco);
        boleto.setDataVencimento(dataVencimento);
        boleto.setDataDocumento(dataDocumento != null ? dataDocumento : LocalDate.now());
        boleto.setValor(valor);
        boleto.setNumeroDocumento(numeroDocumento != null ? numeroDocumento : this.nossoNumeroBase);
        boleto.setInstrucoes(instrucoes);

        // --- Lógica Específica Bradesco ---
        String carteiraF = banco.getCarteira();
        String agenciaF = banco.getAgencia();
        String contaF = banco.getContaCorrente();
        String nn11 = nossoNumeroBase;

        // Calcular DV do Nosso Número (Módulo 11, pesos 2 a 7)
        String baseDvNN = carteiraF + nn11;
        int dvCalculado = calcularModulo11BradescoNN(baseDvNN); // Retorna 0-9, 10(P), 11(0)

        // Mapeamento do resultado para o dígito (0-9 ou P)
        String dvNN;
        if (dvCalculado == 10) { // Código 10 representa 'P'
            dvNN = "P";
        } else if (dvCalculado == 11) { // Código 11 representa '0'
            dvNN = "0";
        } else {
            dvNN = String.valueOf(dvCalculado); // 0 a 9
        }

        // Formata o Nosso Número para exibição no boleto (Carteira / Numero - DV)
        boleto.setNossoNumero(String.format("%s/%s-%s", carteiraF, nn11, dvNN));

        // Montagem do Campo Livre Bradesco
        // Formato: Agencia(4) + Carteira(2) + NossoNumero(11) + Conta(7) + 0 = 25 posições
        String campoLivre = agenciaF + carteiraF + nn11 + contaF + "0";
        if (campoLivre.length() != 25) {
            throw new IllegalStateException("Erro crítico ao gerar Campo Livre Bradesco. Tamanho incorreto: " + campoLivre.length());
        }

        // --- Cálculos Finais ---
        String fatorVencimento = CalculosBoletoUtils.calcularFatorVencimento(boleto.getDataVencimento());
        String valorFormatadoBC = CalculosBoletoUtils.formatarValorParaCodigoBarras(boleto.getValor());

        String codigoBarras = CalculosBoletoUtils.montarCodigoBarras(CODIGO_BANCO, fatorVencimento, valorFormatadoBC, campoLivre);
        String linhaDigitavel = CalculosBoletoUtils.montarLinhaDigitavel(codigoBarras);

        boleto.setCodigoBarras(codigoBarras);
        boleto.setLinhaDigitavel(linhaDigitavel);

        return boleto;
    }

    /**
     * Calcula o Módulo 11 específico para o Nosso Número do Bradesco.
     * Pesos de 2 a 7, da direita para a esquerda.
     * @param numero String numérica (Carteira + NN11).
     * @return Dígito verificador calculado (0 a 9, ou 10 para 'P', ou 11 para '0').
     */
    private int calcularModulo11BradescoNN(String numero) {
        // Usa a base 7 (pesos 2 a 7) e a regra de não ter fator especial para resto 0 ou 1.
        // A classe Modulo retorna o DV calculado (0-11) que será mapeado aqui.
        int dvCalculado = Modulo.calcularModulo11Base(numero, 7, false);
        return dvCalculado; // Retorna 0..11
    }
}