package br.com.geradorboleto.calculos;

public class Modulo {

    /**
     * Calcula o Módulo 10.
     * Utilizado para calcular os dígitos verificadores dos campos da linha digitável.
     * Pesos 2 e 1, da direita para a esquerda.
     * @param numero String numérica.
     * @return Dígito verificador (0 a 9).
     */
    public static int calcularModulo10(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("Número para cálculo do Módulo 10 não pode ser vazio.");
        }
        int soma = 0;
        int peso = 2;
        boolean apenasDigitos = true;
        for (int i = numero.length() - 1; i >= 0; i--) {
            char c = numero.charAt(i);
            if (!Character.isDigit(c)) {
                apenasDigitos = false;
                break; // Sai se encontrar não-dígito
            }
            int digito = Character.getNumericValue(c);
            int produto = digito * peso;
            soma += (produto > 9) ? (produto / 10) + (produto % 10) : produto;
            peso = (peso == 2) ? 1 : 2;
        }

        if (!apenasDigitos) {
            throw new IllegalArgumentException("Número para cálculo do Módulo 10 contém caracteres não numéricos: " + numero);
        }

        int resto = soma % 10;
        int dv = (resto == 0) ? 0 : (10 - resto);
        return dv;
    }

    /**
     * Calcula o Módulo 11 com pesos de 2 a 9 (padrão Febraban).
     * Utilizado para calcular o dígito verificador geral do código de barras e DV do Nosso Número de alguns bancos.
     * @param numero String numérica (sem o DV).
     * @return Dígito verificador (0-9). Para DV Geral do Cód Barras: Restos 0, 1 ou 10 resultam em DV 1.
     */
    public static int calcularModulo11(String numero) {
        // Usa a base 9 (pesos 2 a 9) e a regra de fator 1 para restos 0, 1 ou 10 (maior que 9)
        return calcularModulo11Base(numero, 9, true);
    }

    /**
     * Calcula o Módulo 11 com parâmetros de base (peso máximo) e fator especial.
     * @param numero String numérica.
     * @param base Peso máximo (ex: 9 para Febraban, 7 para CNPJ/CPF/BradescoNN).
     * @param fatorEspecialParaResto0ou1 Se true, restos 0, 1 ou >9 (10, 11..) resultam em DV 1 (ou outro fator conforme regra). Se false, DV=0 para resto 0 ou >9.
     * @return Dígito verificador calculado (0-9).
     */
    public static int calcularModulo11Base(String numero, int base, boolean fatorEspecialParaResto0ou1) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("Número para cálculo do Módulo 11 não pode ser vazio.");
        }
        int soma = 0;
        int peso = 2;
        boolean apenasDigitos = true;
        for (int i = numero.length() - 1; i >= 0; i--) {
            char c = numero.charAt(i);
            if (!Character.isDigit(c)) {
                apenasDigitos = false;
                break; // Sai se encontrar não-dígito
            }
            int digito = Character.getNumericValue(c);
            soma += digito * peso;
            peso++;
            if (peso > base) {
                peso = 2;
            }
        }

        if (!apenasDigitos) {
            throw new IllegalArgumentException("Número para cálculo do Módulo 11 contém caracteres não numéricos: " + numero);
        }

        int resto = soma % 11;
        int dv = 11 - resto;

        if (fatorEspecialParaResto0ou1) {
            // Regra Febraban para DV geral Cód Barras
            if (dv == 0 || dv == 1 || dv > 9) { // Mapeia dv 11, 10, 1 para 1
                return 1;
            } else {
                return dv; // dv 2 a 9
            }
        } else {
            // Regra comum para DVs (ex: CPF/CNPJ, DV de Ag/Conta Bradesco P=10)
            // Note: O tratamento específico para 'P' do Bradesco é feito no Builder.
            // Aqui retornamos o DV calculado 0..11.
            if (dv > 9) { // Mapeia dv 11 (resto 0) e 10 (resto 1) para 0 ou valor especial
                // Para CPF/CNPJ DV seria 0. Para Bradesco NN, DV=P (10) ou DV=0 (11).
                // Retornaremos o valor calculado (0 a 11) para ser tratado pelo chamador.
                // Se resto=0, dv=11. Se resto=1, dv=10.
                return dv;
            } else {
                return dv; // dv 0 a 9 (para restos 2 a 11)
            }
        }
    }
}