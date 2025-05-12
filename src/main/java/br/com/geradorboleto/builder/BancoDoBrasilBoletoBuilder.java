package br.com.geradorboleto.builder;

import br.com.geradorboleto.calculos.CalculosBoletoUtils;
import br.com.geradorboleto.model.*; // Importa todas as classes do model

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// Builder Concreto para Banco do Brasil (Código 001)
public class BancoDoBrasilBoletoBuilder implements BoletoBuilder {

    // Constantes específicas do banco
    private static final String CODIGO_BANCO = "001";
    private static final String NOME_BANCO = "Banco do Brasil S.A.";

    // Atributos para armazenar os dados durante a construção
    private Pessoa sacado;
    private Pessoa beneficiario;
    private Banco banco;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataDocumento;
    private String numeroDocumento;
    private String nossoNumeroBase; // Nosso número sem formatação/DV, como recebido
    private String instrucoes;

    // Construtor inicializa o objeto Banco
    public BancoDoBrasilBoletoBuilder() {
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
        // Remove caracteres não numéricos e formata com zeros à esquerda
        // **IMPORTANTE:** A formatação exata (tamanho, DV) depende do convênio BB. Ajuste se necessário.
        String agenciaLimpa = agencia != null ? agencia.replaceAll("[^0-9]", "") : "";
        String contaLimpa = contaCorrente != null ? contaCorrente.replaceAll("[^0-9]", "") : ""; // Pode conter DV

        // Exemplo: Agência 4 dígitos, Conta 8 dígitos (sem DV), Carteira 2 dígitos
        this.banco.setAgencia(CalculosBoletoUtils.zeroEsquerda(agenciaLimpa, 4));
        this.banco.setContaCorrente(CalculosBoletoUtils.zeroEsquerda(contaLimpa, 8)); // Ajuste se a conta recebida tiver DV
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
        this.nossoNumeroBase = nossoNumero; // Armazena o NN base fornecido
        return this;
    }

    @Override
    public BoletoBuilder comInstrucoes(String instrucoes) {
        this.instrucoes = instrucoes;
        return this;
    }

    @Override
    public Boleto build() {
        // --- Validações Iniciais ---
        Objects.requireNonNull(sacado, "Sacado é obrigatório");
        Objects.requireNonNull(beneficiario, "Beneficiário é obrigatório");
        Objects.requireNonNull(dataVencimento, "Data de Vencimento é obrigatória");
        Objects.requireNonNull(valor, "Valor é obrigatório");
        Objects.requireNonNull(nossoNumeroBase, "Nosso Número (base) é obrigatório");
        // Adicionar validações de formato/tamanho para agência, conta, carteira, nossoNumeroBase
        // conforme as regras do convênio específico do Banco do Brasil.

        // --- Cria e Preenche o Boleto (dados básicos) ---
        Boleto boleto = new Boleto();
        boleto.setSacado(sacado);
        boleto.setBeneficiario(beneficiario);
        boleto.setBanco(banco);
        boleto.setDataVencimento(dataVencimento);
        boleto.setDataDocumento(dataDocumento != null ? dataDocumento : LocalDate.now()); // Default data documento
        boleto.setValor(valor);
        // Define um número de documento padrão se não for fornecido
        boleto.setNumeroDocumento(numeroDocumento != null ? numeroDocumento : this.nossoNumeroBase);
        boleto.setInstrucoes(instrucoes);

        // --- Lógica Específica do Banco do Brasil (Exemplo Convênio 6 dígitos / Carteira 18) ---
        // **CRÍTICO:** Esta parte DEVE ser adaptada ao seu CONVÊNIO e CARTEIRA específicos!
        // Consulte o manual do BB para o layout correto do Nosso Número e Campo Livre.

        // Exemplo para Convênio de 6 dígitos e Carteira 18 (Nosso Número com 11 dígitos + DV opcional)
        // String convenio = "123456"; // SEU CONVÊNIO DE 6 DÍGITOS AQUI! -> Não usado diretamente no CL deste exemplo
        String nn11 = CalculosBoletoUtils.zeroEsquerda(this.nossoNumeroBase, 11); // Garante 11 dígitos para o CL

        // Define o Nosso Número formatado para exibição no boleto (pode variar)
        // Exemplo: para convênio 6 dig, pode ser só o NN de 11 digitos ou Convenio+NN
        boleto.setNossoNumero(nn11); // Ajuste conforme a necessidade de exibição

        // Montagem do Campo Livre (Exemplo para Convênio 6 dígitos / Carteiras 11, 16, 18...)
        // Formato: "000000" + NossoNumero(11) + Agencia(4) + Conta(8) + Carteira(2) = 25 posições
        String agenciaF = CalculosBoletoUtils.zeroEsquerda(banco.getAgencia(), 4);
        String contaF = CalculosBoletoUtils.zeroEsquerda(banco.getContaCorrente(), 8); // Conta sem DV
        String carteiraF = CalculosBoletoUtils.zeroEsquerda(banco.getCarteira(), 2);

        // Valida se a carteira é compatível com este layout de exemplo
        // Adicione outras carteiras válidas para este layout se necessário
        if (!"18".equals(carteiraF) && !"16".equals(carteiraF) && !"11".equals(carteiraF) ) {
            System.err.println("AVISO: Layout Campo Livre BB para carteira " + carteiraF + " (convênio 6) não validado. Usando layout padrão [000000+NN11+AG4+CTA8+CART2].");
        }

        String campoLivre = "000000" + nn11 + agenciaF + contaF + carteiraF;
        if (campoLivre.length() != 25) {
            throw new IllegalStateException("Erro crítico ao gerar Campo Livre BB (Convênio 6). Tamanho incorreto: " + campoLivre.length());
        }

        // --- Cálculos Finais (Comuns a todos os bancos) ---
        String fatorVencimento = CalculosBoletoUtils.calcularFatorVencimento(boleto.getDataVencimento());
        String valorFormatadoBC = CalculosBoletoUtils.formatarValorParaCodigoBarras(boleto.getValor());

        String codigoBarras = CalculosBoletoUtils.montarCodigoBarras(CODIGO_BANCO, fatorVencimento, valorFormatadoBC, campoLivre);
        String linhaDigitavel = CalculosBoletoUtils.montarLinhaDigitavel(codigoBarras);

        // Define os campos calculados no boleto
        boleto.setCodigoBarras(codigoBarras);
        boleto.setLinhaDigitavel(linhaDigitavel);

        return boleto;
    }
}