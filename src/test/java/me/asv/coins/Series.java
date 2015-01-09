package me.asv.coins;

/**
 * Created by anita on 4/12/2014.
 */
public class Series {

    public static void main(String[] args) {
        double sum = 0;
        double term = 1;

        for (int n = 1; n <= 100; n++) {
            term = term * (n) / (2 * n - 1);
            sum += term;
            System.out.println(sum);
        }
        System.out.println(sum);
    }

}
