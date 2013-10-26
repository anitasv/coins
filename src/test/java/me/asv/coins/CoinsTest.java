package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 */
public class CoinsTest {

    private Coins coins;

    @Before
    public void setUp() {
        coins = new Coins(2, new int[]{ 1, 1 }, 3);
    }

    @Test
    public void test() {
        // The O((seqLen * modulo)^3) algorithm.
        BigFraction probability = coins.getProbability();
        System.out.println(probability);
        // Answer should be 10/17
        assertEquals(new BigInteger("10", 10), probability.getNumerator());
        assertEquals(new BigInteger("17", 10), probability.getDenominator());

        double asDouble = probability.doubleValue();
        // The brute force simulation algorithm, cross checking the result.
        double fromSimulation = getProbabilityBySimulation(coins);

        assertEquals(asDouble, fromSimulation, 0.001);
    }

    private double getProbabilityBySimulation(Coins coins) {
        int modulo = coins.getModulo();
        int[] sequence = coins.getSequence();
        int base = coins.getBase();

        Random random = new Random();

        int[] count = new int[modulo];
        // Number of simulations.
        for (int i = 0; i < 10000000; i++) {

            int[] suffix = new int[sequence.length];

            int remainder = 0;
            for (int j = 0; j < sequence.length; j++) {
                int nextDigit = random.nextInt(base);
                remainder = (base * remainder + nextDigit) % modulo;
                suffix[j] = nextDigit;
            }

            int nextDigitPos = 0;

            // Use suffix like a cyclic array queue.
            while (true) {
                boolean found = true;
                for (int j = 0; j < sequence.length; j++) {
                    if (sequence[j] != suffix[(nextDigitPos + j) % sequence.length]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    break;
                }
                int nextDigit = random.nextInt(base);
                remainder = (base * remainder + nextDigit) % modulo;
                suffix[nextDigitPos] = nextDigit;
                nextDigitPos = (nextDigitPos + 1) % sequence.length;
            }
            count[remainder]++;
        }

        int total = 0;
        for (int r = 0; r < modulo; r++) {
            total += count[r];
        }
        return (count[0] + 0.0) / total;
    }}
