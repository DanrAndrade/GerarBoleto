package br.com.geradorboleto.pdf;

// Imports da biblioteca iText 7 (essenciais)
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell; // Import Cell
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment; // Para alinhamento vertical em células

// Imports do seu modelo
import br.com.geradorboleto.model.Boleto;

// Imports Java padrão
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate; // Para data de processamento
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects; // Para validações


public class BoletoPDFExporter {

    // Formatadores e Locale
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BRAZIL_LOCALE = new Locale("pt", "BR");

    /**
     * Exporta os dados de um Boleto para um arquivo PDF.
     * @param boleto O objeto Boleto preenchido.
     * @param caminhoArquivo O caminho completo onde o PDF será salvo (ex: "C:/temp/boleto_final.pdf").
     * @throws IOException Se ocorrer um erro durante a escrita do arquivo.
     */
    public static void exportar(Boleto boleto, String caminhoArquivo) throws IOException {
        Objects.requireNonNull(boleto, "O objeto Boleto não pode ser nulo para exportação.");
        Objects.requireNonNull(caminhoArquivo, "O caminho do arquivo PDF não pode ser nulo.");
        Objects.requireNonNull(boleto.getBanco(), "Dados bancários não podem ser nulos no boleto.");
        Objects.requireNonNull(boleto.getBeneficiario(), "Beneficiário não pode ser nulo no boleto.");
        Objects.requireNonNull(boleto.getSacado(), "Sacado não pode ser nulo no boleto.");
        Objects.requireNonNull(boleto.getDataVencimento(), "Data de vencimento não pode ser nula no boleto.");
        Objects.requireNonNull(boleto.getValor(), "Valor não pode ser nulo no boleto.");


        try (PdfWriter writer = new PdfWriter(caminhoArquivo);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(20, 20, 20, 20); // Margens menores (top, right, bottom, left)

            // --- Cabeçalho com Logo, Banco e Linha Digitável ---
            Table cabecalho = new Table(UnitValue.createPercentArray(new float[]{20f, 15f, 65f})).useAllAvailableWidth();

            // Célula para Logo (Placeholder)
            Cell logoCell = new Cell().add(new Paragraph(boleto.getBanco().getNomeBanco()) // Usando nome do banco como placeholder
                            .setFontSize(10).setBold())
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setHeight(30) // Altura fixa
                    .setBorder(null); // Sem borda
            cabecalho.addCell(logoCell);

            // Célula para Número do Banco
            Cell bancoNumCell = new Cell().add(new Paragraph(boleto.getBanco().getNumeroFormatado())
                            .setFontSize(14).setBold())
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderLeft(null).setBorderTop(null).setBorderBottom(null); // Só borda direita
            cabecalho.addCell(bancoNumCell);

            // Célula para Linha Digitável
            Cell linhaDigitavelCell = new Cell().add(new Paragraph(boleto.formatarLinhaDigitavel(boleto.getLinhaDigitavel()))
                            .setFontSize(10).setBold())
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null); // Sem borda
            cabecalho.addCell(linhaDigitavelCell);
            document.add(cabecalho);

            // --- Tabela Principal com Detalhes ---
            // Definindo larguras relativas das colunas
            float[] widths = {3, 4, 2, 3, 4}; // Ajuste conforme necessário para caber tudo
            Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth().setMarginTop(5);

            // Linha 1: Local Pagamento / Vencimento
            table.addCell(createHeaderCell("Local de Pagamento", 4)); // Colspan 4
            table.addCell(createHeaderCell("Vencimento"));
            table.addCell(createValueCell("Pagável Preferencialmente na Rede Bancária", 4));
            table.addCell(createValueCell(boleto.getDataVencimento().format(DATE_FORMATTER)).setTextAlignment(TextAlignment.RIGHT).setBold());

            // Linha 2: Beneficiário / Agência/Cód. Beneficiário
            table.addCell(createHeaderCell("Beneficiário (Cedente)", 4));
            table.addCell(createHeaderCell("Agência / Código Beneficiário"));
            String beneficiarioDoc = boleto.getBeneficiario().getNome() + " - CPF/CNPJ: " + boleto.getBeneficiario().getDocumento();
            String agenciaConta = boleto.getBanco().getAgencia() + " / " + boleto.getBanco().getContaCorrente(); // Ajustar se necessário
            table.addCell(createValueCell(beneficiarioDoc, 4));
            table.addCell(createValueCell(agenciaConta).setTextAlignment(TextAlignment.RIGHT));

            // Linha 3: Data Doc / Num Doc / Espécie Doc / Aceite / Data Proc. / Nosso Número
            table.addCell(createHeaderCell("Data Documento"));
            table.addCell(createHeaderCell("Nº Documento")); // Ajuste colspan se necessário
            table.addCell(createHeaderCell("Espécie Doc."));
            table.addCell(createHeaderCell("Aceite"));
            table.addCell(createHeaderCell("Nosso Número")); // Movido aqui

            table.addCell(createValueCell(boleto.getDataDocumento() != null ? boleto.getDataDocumento().format(DATE_FORMATTER) : ""));
            table.addCell(createValueCell(boleto.getNumeroDocumento()));
            table.addCell(createValueCell("DM")); // Espécie Documento (Duplicata Mercantil) - Exemplo
            table.addCell(createValueCell("N")); // Aceite (Não) - Exemplo
            table.addCell(createValueCell(boleto.getNossoNumero()).setTextAlignment(TextAlignment.RIGHT));


            // Linha 4: Uso Banco / Carteira / Espécie Moeda / Quant Moeda / Valor Doc
            table.addCell(createHeaderCell("Uso do Banco"));
            table.addCell(createHeaderCell("Carteira"));
            table.addCell(createHeaderCell("Espécie Moeda"));
            table.addCell(createHeaderCell("Quantidade Moeda"));
            table.addCell(createHeaderCell("(=) Valor Documento"));

            table.addCell(createValueCell("")); // Uso Banco
            table.addCell(createValueCell(boleto.getBanco().getCarteira()));
            table.addCell(createValueCell("R$")); // Espécie Moeda (Real)
            table.addCell(createValueCell("")); // Quantidade Moeda
            table.addCell(createValueCell(String.format(BRAZIL_LOCALE, "%,.2f", boleto.getValor()))
                    .setTextAlignment(TextAlignment.RIGHT).setBold());

            // Linha 5: Instruções / (-) Desconto / (+) Juros/Multa / (=) Valor Cobrado
            table.addCell(createHeaderCell("Instruções (Texto de Responsabilidade do Beneficiário)", 4).setHeight(50).setVerticalAlignment(VerticalAlignment.TOP)); // Colspan 4, Altura maior
            table.addCell(createHeaderCell("(-) Desconto / Abatimento"));
            table.addCell(createValueCell(boleto.getInstrucoes() != null ? boleto.getInstrucoes() : " ", 4).setHeight(50).setVerticalAlignment(VerticalAlignment.TOP)); // Colspan 4
            table.addCell(createValueCell("").setTextAlignment(TextAlignment.RIGHT)); // Desconto

            table.addCell(createHeaderCell("", 4)); // Célula vazia para alinhar
            table.addCell(createHeaderCell("(+) Mora / Multa"));
            table.addCell(createValueCell("", 4)); // Célula vazia
            table.addCell(createValueCell("").setTextAlignment(TextAlignment.RIGHT)); // Juros/Multa

            table.addCell(createHeaderCell("", 4)); // Célula vazia
            table.addCell(createHeaderCell("(=) Valor Cobrado"));
            table.addCell(createValueCell("", 4)); // Célula vazia
            table.addCell(createValueCell(String.format(BRAZIL_LOCALE, "%,.2f", boleto.getValor())) // Mostrando valor principal aqui
                    .setTextAlignment(TextAlignment.RIGHT).setBold());

            // Linha 6: Sacado
            table.addCell(createHeaderCell("Sacado", 5).setVerticalAlignment(VerticalAlignment.TOP)); // Colspan 5
            String sacadoDoc = boleto.getSacado().getNome() + " - CPF/CNPJ: " + boleto.getSacado().getDocumento();
            String sacadoEnd = boleto.getSacado().getEndereco() != null ? boleto.getSacado().getEndereco().toString() : "";
            table.addCell(createValueCell(sacadoDoc + "\n" + sacadoEnd, 5).setHeight(40).setVerticalAlignment(VerticalAlignment.TOP)); // Colspan 5

            // Linha 7: Sacador/Avalista e Autenticação Mecânica
            table.addCell(createHeaderCell("Sacador / Avalista", 3)); // Colspan 3
            table.addCell(createHeaderCell("Autenticação Mecânica / FICHA DE COMPENSAÇÃO", 2).setTextAlignment(TextAlignment.RIGHT)); // Colspan 2
            table.addCell(createValueCell("").setHeight(20), 3); // Colspan 3
            table.addCell(createValueCell("", 2).setHeight(20)); // Colspan 2

            document.add(table);

            // --- Código de Barras (Texto ou Imagem) ---
            document.add(new Paragraph("\n")); // Espaço

            // **IMPORTANTE:** Gerar a imagem do código de barras requer biblioteca extra (ex: Barcode4J)
            // e integração com iText. Abaixo, apenas a representação textual.
            Paragraph codBarrasParagraph = new Paragraph(boleto.getCodigoBarras() != null ? boleto.getCodigoBarras() : "Código de Barras Indisponível")
                    .setFontSize(10) // Pode usar fonte específica de código de barras se instalada
                    .setTextAlignment(TextAlignment.LEFT); // Ou Center
            document.add(codBarrasParagraph);


            System.out.println("INFO: Boleto PDF exportado com sucesso para: " + caminhoArquivo);

        } catch (FileNotFoundException e) {
            System.err.println("ERRO: Arquivo não encontrado ou sem permissão de escrita - " + caminhoArquivo);
            throw e; // Re-lança a exceção
        } catch (IOException e) {
            System.err.println("ERRO: Falha de I/O ao gerar PDF: " + e.getMessage());
            throw e; // Re-lança a exceção
        } catch (Exception e) {
            // Captura outras exceções inesperadas durante a geração do PDF
            System.err.println("ERRO inesperado ao gerar PDF: " + e.getMessage());
            e.printStackTrace(); // Imprime o stack trace para depuração
            // Considerar lançar uma exceção específica de aplicação aqui
            throw new IOException("Erro inesperado na geração do PDF.", e);
        }
    }

    // --- Métodos Auxiliares para criar Células da Tabela ---

    // Cria uma célula de cabeçalho (label) com borda padrão
    private static Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "")
                        .setFontSize(7) // Fonte pequena para labels
                )
                .setPadding(1).setMargin(0); // Padding e margem mínimos
    }
    // Sobrecarga para colspan
    private static Cell createHeaderCell(String text, int colspan) {
        return createHeaderCell(text).setColspan(colspan);
    }

    // Cria uma célula de valor (dado) com borda padrão
    private static Cell createValueCell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "")
                        .setFontSize(8)) // Fonte um pouco maior para dados
                .setPadding(1).setMargin(0);
    }
    // Sobrecarga para colspan
    private static Cell createValueCell(String text, int colspan) {
        return createValueCell(text).setColspan(colspan);
    }
}