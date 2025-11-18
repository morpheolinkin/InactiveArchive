package com.escola.inactivearchive.util;

import java.util.Random;

public class GeradorCpf {

    public static String gerarCPF() {
        int soma = 0;
        int resto = 0;
        int[] numeros = new int[9];
        Random numeroAleatorio = new Random();

        for (int i = 0; i < 9; i++) {
            numeros[i] = numeroAleatorio.nextInt(10);
        }

        for (int i = 0; i < 9; i++) {
            soma += (numeros[i] * (10 - i));
        }

        resto = 11 - (soma % 11);
        if (resto == 10 || resto == 11) resto = 0;
        int digito1 = resto;

        soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (numeros[i] * (11 - i));
        }
        soma += digito1 * 2;

        resto = 11 - (soma % 11);
        if (resto == 10 || resto == 11) resto = 0;
        int digito2 = resto;

        return String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                numeros[0], numeros[1], numeros[2],
                numeros[3], numeros[4], numeros[5],
                numeros[6], numeros[7], numeros[8],
                digito1, digito2);
    }
}
