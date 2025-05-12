package br.com.geradorboleto.builder;

// Importa as classes do modelo que serão usadas na interface
import br.com.geradorboleto.model.Boleto;
// import br.com.geradorboleto.model.Endereco; // Usado implicitamente nos métodos
// import br.com.geradorboleto.model.Pessoa;   // Usado implicitamente nos métodos

import java.math.BigDecimal;
import java.time.LocalDate;

// Interface Builder: Define os passos para construir um Boleto
public interface BoletoBuilder {

    // Configura o Sacado (quem paga)
    BoletoBuilder comSacado(String nome, String cpfCnpj, String logradouro, String bairro, String cep, String cidade, String uf);

    // Configura o Beneficiário (quem recebe)
    BoletoBuilder comBeneficiario(String nome, String cpfCnpj, String logradouro, String bairro, String cep, String cidade, String uf);

    // Configura os dados bancários (Agência, Conta, Carteira). O código do banco é implícito na implementação.
    BoletoBuilder comBanco(String agencia, String contaCorrente, String carteira);

    // Configura as datas importantes
    BoletoBuilder comDatas(LocalDate dataVencimento, LocalDate dataDocumento);

    // Configura os valores e identificadores do título
    BoletoBuilder comValores(BigDecimal valor, String numeroDocumento, String nossoNumero);

    // Configura instruções adicionais (opcional)
    BoletoBuilder comInstrucoes(String instrucoes);

    // Finaliza a construção e retorna o Boleto pronto
    Boleto build();
}