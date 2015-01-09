package me.asv.coins;

import org.apache.commons.math3.util.Pair;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Combinations {

    private static final Map<Pair<Integer, Integer>, BigInteger> DP = new HashMap<>();

    public static BigInteger nCr(int n, int r) {
        if (r == 0) {
            return BigInteger.ONE;
        } else if (n < r) {
            return BigInteger.ZERO;
        } else {
            Pair<Integer, Integer> input = new Pair<>(n, r);
            BigInteger output = DP.get(input);
            if (output == null) {
                output = nCr(n - 1, r - 1).add(nCr(n - 1, r));
                DP.put(input, output);
            }
            return output;
        }
    }
}
