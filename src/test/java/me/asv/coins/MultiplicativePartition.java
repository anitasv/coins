package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.Integer;import java.lang.String;import java.lang.System;import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MultiplicativePartition {

    public static void main(String[] args) {

        Polynomial z = new Polynomial(new PolynomialTerm(BigFraction.ONE));
        List<Polynomial> zCache = new ArrayList<>();
        zCache.add(z);

        for (int m = 1; m <= 2; m++) {
            z = new Polynomial();
            for (int l = 1; l <= m; l++) {
                List<Integer> var = new ArrayList<>();
                var.add(l - 1);
                BigFraction frac = new BigFraction(1, m);
                Polynomial temp = new Polynomial(new PolynomialTerm(frac, var));
                Polynomial zM = zCache.get(m - l);
                Polynomial temp2 = zM.multiply(temp);
                z = z.add(temp2);
            }
            zCache.add(z);
        }

        List<Integer> alphas = new ArrayList<>();
        alphas.add(2);
        alphas.add(4);
        alphas.add(8);
        System.out.println(z);
        System.out.println("Fast Solution: " + z.accumulate(alphas));

        List<BigInteger> primes = new ArrayList<>();
        BigInteger nextPrime = new BigInteger("2");
        for (int i = 0; i < alphas.size(); i++) {
            primes.add(nextPrime);
            nextPrime = nextPrime.nextProbablePrime();
        }

        BigInteger n = BigInteger.ONE;
        for (int i = 0; i < alphas.size(); i++) {
            Integer alpha = alphas.get(i);
            BigInteger prime = primes.get(i);
            n = n.multiply(prime.pow(alpha));
        }
        System.out.println("Brute Force: " + bruteForce(n));

    }

    private static BigInteger bruteForce(BigInteger n) {
        BigInteger count = BigInteger.ZERO;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            BigInteger j = n.divide(i);
            if (i.compareTo(j) > 0) {
                return count;
            }
            if (n.remainder(i).equals(BigInteger.ZERO)) {
                count = count.add(BigInteger.ONE);
            }
        }
        return count;
    }
}
