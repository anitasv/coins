package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MultiplicativePartition {

    public static void main(String[] args) {

        Polynomial z = new Polynomial(new PolynomialTerm(BigFraction.ONE));
        List<Polynomial> zCache = new ArrayList<>();
        zCache.add(z);

        int k = 2;
        List<Integer> alphas = new ArrayList<>();
        alphas.add(2);
        alphas.add(1);

        for (int m = 1; m <= k; m++) {
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

        // Only number "one" is allowed to repeat, not the rest.

        Polynomial zero = new Polynomial();
        Polynomial one = new Polynomial(new PolynomialTerm(BigFraction.ONE));
        List<Polynomial> application = new ArrayList<>();
        List<Integer> vars = new ArrayList<>();
        vars.add(0);
        application.add(new Polynomial(new PolynomialTerm(BigFraction.ONE, vars)));
        for (int m = 1; m < k; m++) {
            application.add(one);
        }

        System.out.println("Polynomial: " + z);
        System.out.println("Fast Solution All : " + z.accumulate(alphas));

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
        System.out.println("Brute Force All: " + bruteForce(n, BigInteger.ONE, k));
        System.out.println("Brute Force Distinct: " + bruteForceDistinct(n, BigInteger.ONE, k));

    }

    private static BigInteger bruteForce(BigInteger n, BigInteger start, int k) {
        if (k == 1) {
            if (start.compareTo(n) <= 0) {
                return BigInteger.ONE;
            } else {
                return BigInteger.ZERO;
            }
        }
        BigInteger count = BigInteger.ZERO;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            BigInteger j = n.divide(i);
            if (i.compareTo(j) > 0) {
                return count;
            }
            if (n.remainder(i).equals(BigInteger.ZERO)) {
                count = count.add(bruteForce(j, i, k - 1));
            }
        }
        return count;
    }

    private static BigInteger bruteForceDistinct(BigInteger n, BigInteger start, int k) {
        if (k == 1) {
            if (start.compareTo(n) <= 0) {
                return BigInteger.ONE;
            } else {
                return BigInteger.ZERO;
            }
        }
        BigInteger count = BigInteger.ZERO;
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            BigInteger j = n.divide(i);
            if (i.compareTo(j) > 0) {
                return count;
            }
            if (n.remainder(i).equals(BigInteger.ZERO)) {
                if (i.equals(BigInteger.ONE)) {
                    // one is allowed to repeat!
                    count = count.add(bruteForceDistinct(j, i, k - 1));
                } else {
                    count = count.add(bruteForceDistinct(j, i.add(BigInteger.ONE), k - 1));
                }
            }
        }
        return count;
    }

}
