package me.asv.coins;

import lombok.Data;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.BigFractionField;
import org.apache.commons.math3.linear.*;

import java.util.Arrays;

/**
 */
@Data
public class Coins {

    private final int base;

    private final int[] sequence;

    private final int modulo;

    public BigFraction getProbability() {
        int seqLen = sequence.length;
        /**
         * Combination of KMP pattern matching and Aho Corasick Algorithm.
         */

        // Build KMP Table, O(seqLen)
        Integer[] kmpTable = new Integer[seqLen];
        if (seqLen > 0) { kmpTable[0] = - 1; }
        if (seqLen > 1) { kmpTable[1] = 0; }
        int cnd = 0;
        for (int pos = 2; pos < seqLen;) {
            if (sequence[pos - 1] == sequence[cnd]) {
                cnd++;
                kmpTable[pos] = cnd;
                pos = pos + 1;
            } else if (cnd > 0) {
                cnd = kmpTable[cnd];
            } else {
                kmpTable[pos] = 0;
                pos++;
            }
        }
        System.out.println(Arrays.asList(kmpTable));
        // Build Aho-Corsaic match for single string. This is not the fastest algorithm out
        // there, but a simple KMP aided fast construction.
        // This even though looks like O(seqLen^2 * base), it is almost as fast as O(seqLen * base)
        // Perhaps it even is :P
        Integer[][] acJumps = new Integer[seqLen][base];

        for (int pos = 0; pos < seqLen; pos++) {
            for (int alphabet = 0; alphabet < base; alphabet++) {
                // m is position of current match.
                int m = 0;
                for (; m <= pos;) {
                    // Position of current character in search string.
                    int i = pos - m;
                    if (alphabet == sequence[i]) {
                        // We found a match.
                        break;
                    } else {
                        // Look for a smaller suffix.
                        m = pos - kmpTable[i];
                    }
                }
                acJumps[pos][alphabet] = pos + 1 - m;
            }
        }
        BigFraction frac = new BigFraction(1,  base);

        int states = seqLen * modulo;
        FieldMatrix<BigFraction> transition = new Array2DRowFieldMatrix<>(BigFractionField.getInstance(),
                states, states);

        FieldVector<BigFraction> blackHoleTransition = new ArrayFieldVector<>(BigFractionField.getInstance(),
                states);

        for (int i = 0; i < seqLen; i++) {
            for (int inModulo = 0; inModulo < modulo; inModulo++) {
                int state = i * modulo + inModulo;

                for (int j = 0; j < base; j++) {
                    int to = acJumps[i][j];
                    int outModulo = (inModulo * base + j) % modulo;
                    int outState = to * modulo + outModulo;

                    if (to == seqLen) {
                        if (outModulo == 0) {
                            BigFraction fracOld = blackHoleTransition.getEntry(state);
                            blackHoleTransition.setEntry(state, fracOld.add(frac));
                        }
                    } else {
                        transition.setEntry(outState, state, frac);
                    }
                }
            }
        }

        FieldMatrix<BigFraction> identity = MatrixUtils.createFieldIdentityMatrix(
                BigFractionField.getInstance(), states);

        FieldMatrix<BigFraction> identityMinusTransition = identity.subtract(transition);

        FieldMatrix<BigFraction> markovInverse = new FieldLUDecomposition<>(identityMinusTransition)
                .getSolver().getInverse();

        FieldVector<BigFraction> initialProbability = new ArrayFieldVector<>(BigFractionField.getInstance(),
                states);
        initialProbability.setEntry(0, BigFraction.ONE);

        FieldVector<BigFraction> finalVector = markovInverse.operate(initialProbability);

        return finalVector.dotProduct(blackHoleTransition);
    }
}
