package me.asv.coins;

import java.math.BigInteger;

/**
 * Created by anita on 1/9/2015.
 */
public class MultiplicativeBruteForce {
    static BigInteger bruteForce(BigInteger n, BigInteger start, int k) {
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

    static BigInteger bruteForceDistinct(BigInteger n, BigInteger start, int k) {
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
