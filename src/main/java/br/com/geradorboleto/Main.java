package br.com.geradorboleto;

// Imports dos Builders
import br.com.geradorboleto.builder.BancoDoBrasilBoletoBuilder;
import br.com.geradorboleto.builder.BoletoBuilder;
import br.com.geradorboleto.builder.BradescoBoletoBuilder;
import br.com.geradorboleto.builder.ItauBoletoBuilder;

// Import do Modelo
import br.com.geradorboleto.model.Boleto;

// Import do Exportador PDF
import br.com.geradorboleto.pdf.BoletoPDFExporter;

// Imports Java padrão
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        System.out.println("--- Gerando Boleto Banco do Brasil (Exemplo Convênio 6 / Carteira 18) ---");
        try {
            BoletoBuilder bbBuilder = new BancoDoBrasilBoletoBuilder();
            // **Ajuste os dados de Agência, Conta, Carteira e Nosso Número conforme seu convênio REAL!**
            Boleto boletoBB = bbBuilder
                    .comSacado("Cliente Sacado Exemplo Ltda", "11.222.333/0001-44", "Rua Teste Sacado, 123", "Centro", "12345-000", "Cidade Sacado", "SP")
                    .comBeneficiario("K19 Treinamentos (BB)", "99.999.999/0001-99", "Av. Principal Benef, 456", "Jardins", "54321-000", "Cidade Benef", "MG")
                    .comBanco("1234", "56789012", "18") // Ag 4, Conta 8 (sem DV), Cart 2 -> Exemplo Conv 6
                    .comDatas(LocalDate.now().plusDays(15), LocalDate.now()) // Vencimento em 15 dias
                    .comValores(new BigDecimal("199.99"), "DOC-BB-001", "98765432101") // Valor, Num Doc, Nosso Número (11 dígitos base)
                    .comInstrucoes("Pagável preferencialmente na rede bancária.\nApós vencimento, cobrar multa de 2% e juros de 0,033% ao dia.")
                    .build();

            System.out.println("\n--- [BOLETO BB - CONSOLE] ---");
            System.out.println(boletoBB); // Imprime no console
            BoletoPDFExporter.exportar(boletoBB, "boleto_bb.pdf"); // Gera PDF

        } catch (Exception e) {
            System.err.println("ERRO ao gerar boleto BB: " + e.getMessage());
            e.printStackTrace(); // Mostra detalhes do erro no console de erro
        }

        System.out.println("\n--- Gerando Boleto Itaú (Exemplo Carteira 109) ---");
        try {
            BoletoBuilder itauBuilder = new ItauBoletoBuilder();
            // **Ajuste Ag, Conta (com DV), Cart e NN**
            Boleto boletoItau = itauBuilder
                    .comSacado("Outro Cliente S.A.", "88.777.666/0001-55", "Av. Teste Sacado, 987", "Bairro Novo", "98765-111", "Outra Cidade", "RJ")
                    .comBeneficiario("Empresa Beneficiária Itaú", "11.111.111/0001-11", "Rua Faria Lima, 1000", "Itaim Bibi", "04538-000", "São Paulo", "SP")
                    .comBanco("5678", "12345-6", "109") // Ag 4, Conta 5+DV, Cart 3
                    .comDatas(LocalDate.now().plusDays(30), LocalDate.now().minusDays(1)) // Vencimento em 30 dias
                    .comValores(new BigDecimal("550.75"), "DOC-ITAU-XYZ", "12345678") // Valor, Num Doc, Nosso Número (8 dígitos base)
                    .comInstrucoes("Não receber após 30 dias do vencimento.")
                    .build();

            System.out.println("\n--- [BOLETO ITAÚ - CONSOLE] ---");
            System.out.println(boletoItau);
            BoletoPDFExporter.exportar(boletoItau, "boleto_itau.pdf");

        } catch (Exception e) {
            System.err.println("ERRO ao gerar boleto Itaú: " + e.getMessage());
            e.printStackTrace();
        }


        System.out.println("\n--- Gerando Boleto Bradesco (Exemplo Carteira 09) ---");
        try {
            BoletoBuilder bradescoBuilder = new BradescoBoletoBuilder();
            // **Ajuste Ag, Conta, Cart e NN**
            Boleto boletoBradesco = bradescoBuilder
                    .comSacado("Comércio Varejista ABC", "44.555.666/0001-77", "Praça Central, 10", "Centro", "77777-000", "Cidade Bradesco", "BA")
                    .comBeneficiario("Indústria XYZ Ltda (Bradesco)", "22.222.222/0001-22", "Rodovia Industrial, km 5", "Distrito Ind.", "88888-000", "Cidade Industrial", "PR")
                    .comBanco("9876", "1234567", "09") // Ag 4 (s/DV), Conta 7 (s/DV), Cart 2
                    .comDatas(LocalDate.now().plusDays(10), LocalDate.now()) // Vencimento em 10 dias
                    .comValores(new BigDecimal("1234.56"), "DOC-BRAD-999", "11223344556") // Valor, Num Doc, Nosso Número (11 dígitos base)
                    .comInstrucoes("Boleto referente à NF 12345.")
                    .build();

            System.out.println("\n--- [BOLETO BRADESCO - CONSOLE] ---");
            System.out.println(boletoBradesco);
            BoletoPDFExporter.exportar(boletoBradesco, "boleto_bradesco.pdf");

        } catch (Exception e) {
            System.err.println("ERRO ao gerar boleto Bradesco: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- Geração de boletos concluída. Verifique os arquivos PDF na raiz do projeto. ---");
    }
}