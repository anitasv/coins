package me.asv.coins;

import lombok.Data;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.BigFractionField;
import org.apache.commons.math3.linear.*;

import java.util.Arrays;

/**
 * Problem Statement:
 *
 * Use a 'base' sided fair coin to generate a number in base 'base'. Stop when it reaches
 * a predefined sequence of digits. The question is what is the probability that this is
 * a multiple of 'modulo'.
 *
 * Alternately:
 *
 * void runSimulation() {
 *   num = 0;
 *   while (!num.endsWith(sequence)) { // endsWith in that base.
 *     toss = fairToss(base);
 *     num = num * base + toss;  // num = (num concat toss);
 *   }
 *
 *   if (toss % modulo == 0) {
 *       print YES
 *   } else {
 *       print NO
 *   }
 * }
 * What is the probability YES is printed?
 *
 */
@Data
public class Coins {

    private final int base;

    private final int[] sequence;

    private final int modulo;

    public BigFraction getProbability() {
        int seqLen = sequence.length;
        /**
         * Steps involved:
         *   a. Build KMP Table for input string.
         *   b. Build Aho Corsaic like DFA for input string using KMP.
         *   c. Build transition matrix from DFA.
         *   d. Compute Accepting Criteria vector, in this case modulo = 0.
         *   e. Build initial probability vector.
         * Note: totalProbability = pI + pT + pT^2 .. = p * I / (I-T)
         *   g. Compute (I-T) inverse.
         *   h. Compute totalProbability.
         *   i. Dot-Product to find probability when modulo = 0.
         *   j. RETURN
         */

        // Build KMP Table, O(seqLen)
        Integer[] kmpTable = buildKmpTable(seqLen);

        // Build Aho-Corsaic match for single string. This is not the fastest algorithm out
        // there, but a simple KMP aided fast construction.
        // This even though looks like O(seqLen^2 * base), it is almost as fast as O(seqLen * base)
        // Perhaps it even is :P
        Integer[][] acJumps = buildDfa(seqLen, kmpTable);

        int states = seqLen * modulo;
        FieldMatrix<BigFraction> transition = new Array2DRowFieldMatrix<>(BigFractionField.getInstance(),
                states, states);

        FieldVector<BigFraction> acceptingCriteria = new ArrayFieldVector<>(BigFractionField.getInstance(),
                states);

        // Building transition matrix and accepting criteria together. This is done in
        // O(seqLen * modulo * base) time.
        buildTransitionAndAcceptingCriteria(acJumps, transition, acceptingCriteria);

        FieldMatrix<BigFraction> identity = MatrixUtils.createFieldIdentityMatrix(
                BigFractionField.getInstance(), states);

        FieldMatrix<BigFraction> identityMinusTransition = identity.subtract(transition);

        FieldDecompositionSolver<BigFraction> markovInverse = new FieldLUDecomposition<>(identityMinusTransition)
                .getSolver();

        FieldVector<BigFraction> initialProbability = new ArrayFieldVector<>(BigFractionField.getInstance(),
                states);
        initialProbability.setEntry(0, BigFraction.ONE);

        // The most awesome step, and most time consuming of all too. This computes the cumulative
        // transition probability matrix, using an equation I discovered.
        // Takes O((seqLen * modulo)^2) time.
        FieldVector<BigFraction> totalProbability = markovInverse.solve(initialProbability);

        return totalProbability.dotProduct(acceptingCriteria);
    }

    private void buildTransitionAndAcceptingCriteria(Integer[][] acJumps,
                                                     FieldMatrix<BigFraction> transition,
                                                     FieldVector<BigFraction> acceptingCriteria) {
        // Building transition matrix and accepting criteria together.
        BigFraction frac = new BigFraction(1,  base);
        int seqLen = sequence.length;

        for (int i = 0; i < seqLen; i++) {
            for (int inModulo = 0; inModulo < modulo; inModulo++) {
                int state = i * modulo + inModulo;
                for (int j = 0; j < base; j++) {
                    int to = acJumps[i][j];
                    int outModulo = (inModulo * base + j) % modulo;
                    int outState = to * modulo + outModulo;

                    if (to == seqLen) {
                        if (outModulo == 0) {
                            BigFraction previousValue = acceptingCriteria.getEntry(state);
                            acceptingCriteria.setEntry(state, previousValue.add(frac));
                        }
                    } else {
                        BigFraction previousValue = transition.getEntry(outState, state);
                        transition.setEntry(outState, state, previousValue.add(frac));
                    }
                }
            }
        }
        ;
    }

    private Integer[][] buildDfa(int seqLen, Integer[] kmpTable) {
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
        return acJumps;
    }

    private Integer[] buildKmpTable(int seqLen) {
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
        return kmpTable;
    }
}
