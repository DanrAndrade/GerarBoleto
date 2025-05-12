package br.com.geradorboleto.builder;

import br.com.geradorboleto.calculos.CalculosBoletoUtils;
import br.com.geradorboleto.calculos.Modulo; // Precisa do Módulo 10
import br.com.geradorboleto.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// Builder Concreto para Itaú (Código 341)
public class ItauBoletoBuilder implements BoletoBuilder {

    private static final String CODIGO_BANCO = "341";
    private static final String NOME_BANCO = "Banco Itaú S.A.";

    private Pessoa sacado;
    private Pessoa beneficiario;
    private Banco banco;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataDocumento;
    private String numeroDocumento;
    private String nossoNumeroBase; // NN com 8 dígitos (sem DV)
    private String instrucoes;

    public ItauBoletoBuilder() {
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
        // Itaú: Agência 4 dígitos, Conta 5 dígitos + DV (total 6 ou 7 se tiver zero à esquerda), Carteira 3 dígitos
        String agenciaLimpa = agencia != null ? agencia.replaceAll("[^0-9]", "") : "";
        String contaLimpa = contaCorrente != null ? contaCorrente.replaceAll("[^0-9]", "") : ""; // Pode conter DV

        this.banco.setAgencia(CalculosBoletoUtils.zeroEsquerda(agenciaLimpa, 4));
        // A conta no Itaú pode ter 5 ou 6 dígitos + DV. Armazenamos como recebido, formatando para o campo livre depois.
        this.banco.setContaCorrente(contaLimpa); // Armazena como veio (ex: "12345-6" ou "012345-6")
        this.banco.setCarteira(CalculosBoletoUtils.zeroEsquerda(carteira, 3));
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
        // Itaú usa Nosso Número com 8 dígitos (sem DV)
        this.nossoNumeroBase = CalculosBoletoUtils.zeroEsquerda(nossoNumero, 8);
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
        Objects.requireNonNull(dataVencimento, "Data de Vencimento é obrigatória");
        Objects.requireNonNull(valor, "Valor é obrigatório");
        Objects.requireNonNull(nossoNumeroBase, "Nosso Número (base 8 dígitos) é obrigatório");
        if (nossoNumeroBase.length() != 8) throw new IllegalStateException("Nosso Número base Itaú deve ter 8 dígitos.");
        if (banco.getCarteira().length() != 3) throw new IllegalStateException("Carteira Itaú deve ter 3 dígitos.");
        if (banco.getAgencia().length() != 4) throw new IllegalStateException("Agência Itaú deve ter 4 dígitos.");
        // Adicionar validação para o formato da conta recebida se necessário

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

        // --- Lógica Específica Itaú ---
        String carteiraF = banco.getCarteira();
        String agenciaF = banco.getAgencia(); // Já formatado com 4 zeros à esquerda
        String nn8 = nossoNumeroBase; // Já formatado com 8 zeros à esquerda

        // Extrair conta (5 dígitos) do início da contaCorrente (que pode ter DV)
        String contaCompletaLimpa = banco.getContaCorrente().replaceAll("[^0-9]", "");
        String conta5;
        if (contaCompletaLimpa.length() >= 5) {
            // Pega os 5 primeiros dígitos para o campo livre
            conta5 = contaCompletaLimpa.substring(0, 5);
            // Guarda a conta completa formatada (com DV) para exibição, se necessário
            // banco.setContaCorrente(contaCompletaLimpa); // Ou mantém como veio se já tiver DV
        } else {
            throw new IllegalStateException("Conta corrente Itaú inválida ou curta demais: " + banco.getContaCorrente());
        }

        // Calcular DAC (DV) da Agência/Conta para o Campo Livre (Módulo 10)
        int dacAgConta = Modulo.calcularModulo10(agenciaF + conta5);

        // Calcular DAC (DV) da Carteira/NossoNúmero (Módulo 10 - padrão, verificar manual para exceções como carteira 109)
        int dacCartNN = Modulo.calcularModulo10(carteiraF + nn8);

        // Formata o Nosso Número para exibição no boleto (Carteira/Numero-DV)
        boleto.setNossoNumero(String.format("%s/%s-%d", carteiraF, nn8, dacCartNN));

        // Montagem do Campo Livre Itaú
        // Formato: Carteira(3) + NossoNumero(8) + DAC(Cart/NN)(1) + Agencia(4) + Conta(5) + DAC(Ag/Conta)(1) + 000 = 25 posições
        String campoLivre = carteiraF + nn8 + dacCartNN + agenciaF + conta5 + dacAgConta + "000";
        if (campoLivre.length() != 25) {
            throw new IllegalStateException("Erro crítico ao gerar Campo Livre Itaú. Tamanho incorreto: " + campoLivre.length());
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
}