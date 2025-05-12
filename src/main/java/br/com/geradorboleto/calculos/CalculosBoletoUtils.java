package br.com.geradorboleto.calculos;

// Não precisa importar Boleto aqui, pois os métodos são estáticos e recebem os dados necessários
import java.util.Locale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CalculosBoletoUtils {

    // Data base para cálculo do fator de vencimento (07/10/1997)
    private static final LocalDate DATA_BASE_FATOR_VENCIMENTO = LocalDate.of(1997, 10, 7);
    // Código da moeda Real
    private static final String CODIGO_MOEDA = "9";

    /**
     * Calcula o fator de vencimento.
     * Número de dias entre a data base e a data de vencimento.
     * Limita-se a 9999 dias (até 21/02/2025 com esta base).
     * @param dataVencimento Data de vencimento do boleto.
     * @return Fator de vencimento (4 dígitos).
     */
    public static String calcularFatorVencimento(LocalDate dataVencimento) {
        Objects.requireNonNull(dataVencimento, "Data de Vencimento não pode ser nula.");

        // Trata datas anteriores à base como fator 0000 (boleto vencido sem fator)
        if (dataVencimento.isBefore(DATA_BASE_FATOR_VENCIMENTO)) {
            System.err.println("AVISO: Data de vencimento " + dataVencimento + " anterior à data base ("+ DATA_BASE_FATOR_VENCIMENTO +"). Fator será 0000.");
            return "0000";
        }

        long dias = ChronoUnit.DAYS.between(DATA_BASE_FATOR_VENCIMENTO, dataVencimento);

        // Verifica limite do fator (9999 dias)
        if (dias > 9999) {
            // Poderia lançar exceção ou usar um fator fixo conforme regra de negócio
            // para boletos pós 21/02/2025 (que usam nova regra não implementada aqui)
            System.err.println("AVISO: Data de vencimento " + dataVencimento + " excede o limite de 9999 dias da data base. Fator será truncado ou inválido.");
            // Aqui, vamos retornar 9999 como exemplo, mas o ideal seria tratar a nova regra
            // return "9999";
            // Ou lançar uma exceção:
            throw new IllegalArgumentException("Data de vencimento ("+dataVencimento+") excede o limite para cálculo do fator padrão (9999 dias após "+DATA_BASE_FATOR_VENCIMENTO+"). Nova regra necessária.");
        }

        return String.format("%04d", dias);
    }

    /**
     * Formata o valor do boleto para o código de barras (10 dígitos, sem separadores, com centavos).
     * @param valor Valor do boleto.
     * @return String formatada com 10 dígitos.
     */
    public static String formatarValorParaCodigoBarras(BigDecimal valor) {
        Objects.requireNonNull(valor, "Valor não pode ser nulo.");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor do boleto não pode ser negativo.");
        }
        // Garante duas casas decimais e remove o ponto
        String valorFormatado = String.format(Locale.US, "%.2f", valor).replace(".", ""); // Usa Locale.US para garantir ponto decimal
        // Preenche com zeros à esquerda até ter 10 dígitos
        return zeroEsquerda(valorFormatado, 10);
    }

    /**
     * Monta a string base do código de barras (44 posições), incluindo o DV geral.
     * @param codigoBanco Código do banco (3 dígitos).
     * @param fatorVencimento Fator de vencimento (4 dígitos).
     * @param valorFormatado Valor formatado (10 dígitos).
     * @param campoLivre Campo livre específico do banco (25 dígitos).
     * @return Código de barras completo com 44 dígitos.
     */
    public static String montarCodigoBarras(String codigoBanco, String fatorVencimento, String valorFormatado, String campoLivre) {
        // Validações de entrada
        Objects.requireNonNull(codigoBanco, "Código do Banco não pode ser nulo.");
        Objects.requireNonNull(fatorVencimento, "Fator de Vencimento não pode ser nulo.");
        Objects.requireNonNull(valorFormatado, "Valor Formatado não pode ser nulo.");
        Objects.requireNonNull(campoLivre, "Campo Livre não pode ser nulo.");

        if (!codigoBanco.matches("\\d{3}")) throw new IllegalArgumentException("Código do banco inválido (deve ter 3 dígitos): " + codigoBanco);
        if (!fatorVencimento.matches("\\d{4}")) throw new IllegalArgumentException("Fator de vencimento inválido (deve ter 4 dígitos): " + fatorVencimento);
        if (!valorFormatado.matches("\\d{10}")) throw new IllegalArgumentException("Valor formatado inválido (deve ter 10 dígitos): " + valorFormatado);
        if (!campoLivre.matches("[0-9]{25}")) throw new IllegalArgumentException("Campo Livre inválido (deve ter 25 dígitos numéricos): " + campoLivre);

        // Monta a base sem o DV geral (posições 1-4 e 6-44)
        String baseCodigoBarrasSemDV = codigoBanco + CODIGO_MOEDA + fatorVencimento + valorFormatado + campoLivre;
        if (baseCodigoBarrasSemDV.length() != 43) {
            throw new IllegalStateException("Erro interno: Base do código de barras sem DV deveria ter 43 posições, mas tem " + baseCodigoBarrasSemDV.length());
        }

        // Calcula o DV geral (Módulo 11, padrão Febraban)
        int dvGeral = Modulo.calcularModulo11(baseCodigoBarrasSemDV);

        // Insere o DV na 5ª posição (índice 4)
        return baseCodigoBarrasSemDV.substring(0, 4) + dvGeral + baseCodigoBarrasSemDV.substring(4);
    }

    /**
     * Monta a linha digitável (47 dígitos) a partir do código de barras (44 dígitos).
     * @param codigoBarras Código de barras completo (44 dígitos).
     * @return Linha digitável formatada (47 dígitos numéricos).
     */
    public static String montarLinhaDigitavel(String codigoBarras) {
        Objects.requireNonNull(codigoBarras, "Código de Barras não pode ser nulo.");
        if (!codigoBarras.matches("\\d{44}")) {
            throw new IllegalArgumentException("Código de barras inválido (deve ter 44 dígitos numéricos): " + codigoBarras);
        }

        // Extrai partes do código de barras
        String ccc = codigoBarras.substring(0, 3);      // Código Banco
        String m = codigoBarras.substring(3, 4);        // Moeda
        String dvGeral = codigoBarras.substring(4, 5);    // DV Geral (Campo 4 da linha digitável)
        String ffff = codigoBarras.substring(5, 9);     // Fator Vencimento
        String vvvv = codigoBarras.substring(9, 19);    // Valor
        String campoLivre = codigoBarras.substring(19); // Campo Livre (25 dígitos)

        // Campo 1: CCC + M + Campo Livre[pos 1 a 5] + DV1 (Módulo 10)
        String campo1Base = ccc + m + campoLivre.substring(0, 5);
        String campo1 = campo1Base + Modulo.calcularModulo10(campo1Base); // 9 + 1 = 10 dígitos

        // Campo 2: Campo Livre[pos 6 a 15] + DV2 (Módulo 10)
        String campo2Base = campoLivre.substring(5, 15);
        String campo2 = campo2Base + Modulo.calcularModulo10(campo2Base); // 10 + 1 = 11 dígitos

        // Campo 3: Campo Livre[pos 16 a 25] + DV3 (Módulo 10)
        String campo3Base = campoLivre.substring(15, 25);
        String campo3 = campo3Base + Modulo.calcularModulo10(campo3Base); // 10 + 1 = 11 dígitos

        // Campo 4: DV Geral do Código de Barras (já extraído)
        String campo4 = dvGeral; // 1 dígito

        // Campo 5: Fator Vencimento + Valor
        String campo5 = ffff + vvvv; // 4 + 10 = 14 dígitos

        // Total: 10 + 11 + 11 + 1 + 14 = 47 dígitos
        return campo1 + campo2 + campo3 + campo4 + campo5;
    }

    /**
     * Completa uma string à esquerda com zeros até o tamanho desejado.
     * Se a string for maior, retorna os últimos 'tamanho' caracteres.
     * @param input String original.
     * @param tamanho Tamanho final desejado.
     * @return String completada com zeros ou truncada.
     */
    public static String zeroEsquerda(String input, int tamanho) {
        if (input == null) input = "";
        if (input.length() >= tamanho) return input.substring(input.length() - tamanho);
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = input.length(); i < tamanho; i++) {
            sb.append('0');
        }
        sb.append(input);
        return sb.toString();
    }

    /**
     * Completa uma string à direita com zeros até o tamanho desejado.
     * Se a string for maior, retorna os primeiros 'tamanho' caracteres.
     * @param input String original.
     * @param tamanho Tamanho final desejado.
     * @return String completada com zeros ou truncada.
     */
    public static String zeroDireita(String input, int tamanho) {
        if (input == null) input = "";
        if (input.length() >= tamanho) return input.substring(0, tamanho);
        StringBuilder sb = new StringBuilder(tamanho);
        sb.append(input);
        for (int i = input.length(); i < tamanho; i++) {
            sb.append('0');
        }
        return sb.toString();
    }
}