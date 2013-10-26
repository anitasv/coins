package me.asv.coins;

import org.junit.Before;
import org.junit.Test;

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
        assertEquals(10/17.0, coins.getProbability(), 0.0000001);
    }
}
