package me.asv.coins;

import lombok.Data;
import org.apache.commons.math3.linear.*;

import java.util.Arrays;

/**
 */
@Data
public class Coins {

    private final int base;

    private final int[] sequence;

    private final int modulo;

    public double getProbability() {
        int seqLen = sequence.length;
        /**
         * http://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_string_matching_algorithm
         * Brute force generation, even though it can be done in O(seqLen). Lazy to code
         * full version, because this is not the expensive operation anyway.
         */
        Integer[][] jumps = new Integer[seqLen][base];

        for (int i = 0; i < seqLen; i++) {
            for (int j = 0; j < base; j++) {
                if (sequence[i] == j) {
                    // If correct one is found, you may go one step further.
                    jumps[i][j] = i + 1;
                    continue;
                }
                for (int substrLen = i + 1; substrLen >= 0; substrLen--) {
                    boolean found = true;
                    if (substrLen > 0) {
                        if (sequence[substrLen - 1] == j) {
                            for (int l = 0; l < substrLen - 1; l++) {
                                if (sequence[l] != sequence[i + 1 - substrLen + l]) {
                                    found = false;
                                    break;
                                }
                            }
                        } else {
                            found = false;
                        }
                        if (found) {
                            jumps[i][j] = substrLen;
                            break;
                        }
                    } else {
                        jumps[i][j] = substrLen; // = 0;
                        break; // last loop anyway
                    }
                }
            }
        }
        double frac = 1.0 / base;

        int states = seqLen * modulo;
        Array2DRowRealMatrix transition = new Array2DRowRealMatrix(states, states);

        ArrayRealVector blackHoleTransition = new ArrayRealVector(states);

        for (int i = 0; i < seqLen; i++) {
            for (int inModulo = 0; inModulo < modulo; inModulo++) {
                int state = i * modulo + inModulo;

                for (int j = 0; j < base; j++) {
                    int to = jumps[i][j];
                    int outModulo = (inModulo * base + j) % modulo;
                    int outState = to * modulo + outModulo;

                    if (to == seqLen) {
                        if (outModulo == 0) {
                            blackHoleTransition.addToEntry(state, frac);
                        }
                    } else {
                      transition.setEntry(outState, state, frac);
                    }
                }
            }
        }

        DiagonalMatrix identity = new DiagonalMatrix(states);
        for (int i = 0; i < states; i++) {
            identity.setEntry(i, i, 1.0);
        }

        RealVector initialProbability = new ArrayRealVector(states);
        initialProbability.setEntry(0, 1.0);

        RealMatrix identityFull = new Array2DRowRealMatrix(identity.getData());

        RealMatrix identityMinusTransition = identityFull.subtract(transition);
        RealMatrix markovInverse = new LUDecomposition(identityMinusTransition).getSolver().getInverse();
        RealVector finalVector = markovInverse.operate(initialProbability);

        return finalVector.dotProduct(blackHoleTransition);
    }
}
