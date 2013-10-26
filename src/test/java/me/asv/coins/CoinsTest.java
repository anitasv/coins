package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

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
        BigFraction probability = coins.getProbability();
        System.out.println(probability);
        // Answer should be 10/17
        assertEquals(new BigInteger("10", 10), probability.getNumerator());
        assertEquals(new BigInteger("17", 10), probability.getDenominator());

    }
}
