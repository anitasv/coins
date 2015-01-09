package me.asv.coins;


import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;

/**
 * http://mathb.in/14298
 */
public class ArbitrageComputer {

    private static int N; /* Maximum number of samples used by MLE estimator */

    private static BigInteger combDp[][] = null;

    private static void initialize() {
        combDp = new BigInteger[N + 1][];

        for (int n = 0; n <= N; n++) {
            combDp[n] = new BigInteger[N + 1];
        }
        for (int k = 0; k <= N; k++) {
            combDp[0][k] = BigInteger.ZERO;
        }
        combDp[0][0] = BigInteger.ONE;

        for (int n = 1; n <= N; n++) {
            combDp[n][0] = BigInteger.ONE;
            for (int k = 1; k <= N; k++) {
                combDp[n][k] = combDp[n-1][k-1].add(combDp[n-1][k]);
            }
        }
    }

    private static BigInteger combination(int n, int k) {
        return combDp[n][k];
    }

    public static void main(String[] args) {
        BigFraction b1 = new BigFraction(3, 1); /* bid of first ad */
        BigFraction p1 = new BigFraction(4, 100); /* true ctr of first ad */
        BigFraction e1 = new BigFraction(2, 100); /* error in ctr of first ad */

        BigFraction b2 = new BigFraction(4, 1); /* bid of second ad */
        BigFraction p2 =  new BigFraction(3, 100); /* true ctr of second ad */
        BigFraction e2 = new BigFraction(2, 100); /* error in ctr of second ad */

        int N1;
        int N2;
        {
            BigFraction mse1 = e1.multiply(e1);
            BigFraction mse2 = e2.multiply(e2);

            // Scoping to avoid local variable issue
            BigFraction n1 = p1.multiply(BigFraction.ONE.subtract(p1)).divide(mse1);
            BigFraction n2 = p2.multiply(BigFraction.ONE.subtract(p2)).divide(mse2);

            N1 = n1.intValue() + 1;
            N2 = n2.intValue() + 1;
        }

        N = Math.max(N1, N2);

        System.out.println("Using " + N1 + ", samples first ad model");
        System.out.println("Using " + N2 + ", samples second ad model");

        initialize();


        BigFraction scale = (b1.divide(b2)).multiply(N2).divide(N1);

        BigFraction p1d = BigFraction.ONE.subtract(p1);
        BigFraction p2d = BigFraction.ONE.subtract(p2);

        BigFraction p1Powers[] = new BigFraction[N1 + 1];
        BigFraction p1dPowers[] = new BigFraction[N1 + 1];
        BigFraction p2Powers[] = new BigFraction[N2 + 1];
        BigFraction p2dPowers[] = new BigFraction[N2 + 1];
        p1Powers[0] = BigFraction.ONE;
        p1dPowers[0] = BigFraction.ONE;
        p2Powers[0] = BigFraction.ONE;
        p2dPowers[0] = BigFraction.ONE;
        for (int i = 1 ; i <= N1; i++) {
            p1Powers[i] = p1Powers[i - 1].multiply(p1);
            p1dPowers[i] = p1dPowers[i - 1].multiply(p1d);
        }
        for (int i = 1 ; i <= N2; i++) {
            p2Powers[i] = p2Powers[i - 1].multiply(p2);
            p2dPowers[i] = p2dPowers[i - 1].multiply(p2d);
        }

        BigFraction p1Binomial[] = new BigFraction[N1 + 1];
        BigFraction p2Binomial[] = new BigFraction[N2 + 1];

        BigFraction verify1 = BigFraction.ZERO;
        BigFraction verify2 = BigFraction.ZERO;

        for (int i = 0 ; i <= N1; i++) {
            p1Binomial[i] = (p1Powers[i].multiply(p1dPowers[N1-i])).multiply(combination(N1, i));
            verify1 = verify1.add(p1Binomial[i]);
        }

        for (int i = 0 ; i <= N2; i++) {
            p2Binomial[i] = (p2Powers[i].multiply(p2dPowers[N2-i])).multiply(combination(N2, i));
            verify2 = verify2.add(p2Binomial[i]);
        }

        if (!BigFraction.ONE.equals(verify1)) {
            System.out.println("Verify 1 failed, returning: " + verify1);
            return;
        }
        if (!BigFraction.ONE.equals(verify2)) {
            System.out.println("Verify 2 failed, returning: " + verify2);
            return;
        }
        BigFraction win1 = BigFraction.ZERO;
        BigFraction win2 = BigFraction.ZERO;

        BigFraction verify = BigFraction.ZERO;

        double meanPublisherPayout = 0;
        double meanAdvertiserPayin = 0;

        for (int k1 = 0; k1 <= N1; k1++) {
            BigFraction p1Cap = new BigFraction(k1, N1);
            BigFraction k2Limit = scale.multiply(k1);

            System.out.print(Math.floor((100.0 * k1) / (N1 + 1)) + "% ");
            System.out.flush();
            for (int k2 = 0; k2 <= N2; k2++) {
                BigFraction p2Cap = new BigFraction(k2, N2);

                int status = k2Limit.compareTo(new BigFraction(k2, 1));
                BigFraction term = p1Binomial[k1].multiply(p2Binomial[k2]);

                verify = verify.add(term);
                switch (status) {
                    case -1 :
                        // k1 < k2 if scale = 1, win = 2
                        win2 = win2.add(term);
                        meanAdvertiserPayin += term.multiply(p2).multiply(b2).doubleValue();
                        meanPublisherPayout += term.multiply(p2Cap).multiply(b2).doubleValue();
                        break;

                    case 0: /* equal CTR, do a coin toss */
                        BigFraction halfTerm = term.divide(2);
                        win1 = win1.add(halfTerm);
                        win2 = win2.add(halfTerm);

                        meanAdvertiserPayin += halfTerm.multiply(p1).multiply(b1).doubleValue();
                        meanPublisherPayout += halfTerm.multiply(p1Cap).multiply(b1).doubleValue();

                        meanAdvertiserPayin += halfTerm.multiply(p2).multiply(b2).doubleValue();
                        meanPublisherPayout += halfTerm.multiply(p2Cap).multiply(b2).doubleValue();

                        break;

                    case 1:
                        // k1 > k2 if scale = 1, win = 1
                        win1 = win1.add(term);

                        meanAdvertiserPayin += term.multiply(p1).multiply(b1).doubleValue();
                        meanPublisherPayout += term.multiply(p1Cap).multiply(b1).doubleValue();


                        break;
                }
            }
        }

        if (!BigFraction.ONE.equals(verify)) {
            System.out.println("Verify Final failed, returning " + verify);
            return;
        }

        double profit = meanAdvertiserPayin  - meanPublisherPayout;

        System.out.println("Model uses MLE estimator for " + N + " samples");
        System.out.println("Ad 1: Bid = " + b1 + ", True CTR = " + p1.doubleValue());
        System.out.println("Ad 2: Bid = " + b2 + ", True CTR = " + p2.doubleValue());

        System.out.println("Expected ecpm1 = " + b1.multiply(p1).doubleValue() );
        System.out.println("Expected ecpm2 = " + b2.multiply(p2).doubleValue() );

        System.out.println("Probability ad1 wins auction: " + 100 * win1.doubleValue() + "%");
        System.out.println("Probability ad2 wins auction: " + 100 * win2.doubleValue() + "%");

        System.out.println("Mean Publisher Pay Out: " +  meanPublisherPayout);
        System.out.println("Mean Advertiser Pay In: " + meanAdvertiserPayin);

        System.out.println("Mean Profit: " + profit);
    }
}
