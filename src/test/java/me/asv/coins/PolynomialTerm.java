package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.*;

public class PolynomialTerm {

    private final BigFraction coeff;

    private final List<Integer> variables;

    private final Map<Integer, Integer> compressed;

    public PolynomialTerm(BigFraction coeff, List<Integer> variables) {
        this.coeff = coeff;
        this.variables = variables;
        this.compressed = compress(variables);
    }

    public PolynomialTerm(BigFraction coeff) {
        this(coeff, new ArrayList<Integer>());
    }

    public List<Integer> getVariables() {
        return variables;
    }

    public PolynomialTerm multiply(PolynomialTerm other) {
        List<Integer> newVars = new ArrayList<>();
        newVars.addAll(variables);
        newVars.addAll(other.variables);
        Collections.sort(newVars);
        return new PolynomialTerm(coeff.multiply(other.coeff), newVars);
    }

    public boolean matches(PolynomialTerm other) {
        return variables.equals(other.variables);
    }

    public PolynomialTerm add(PolynomialTerm other) {
        if (!matches(other)) {
            throw new IllegalArgumentException();
        }
        return new PolynomialTerm(other.coeff.add(coeff), variables);
    }

    public Polynomial apply(List<Polynomial> other) {
        PolynomialTerm constantTerm = new PolynomialTerm(coeff, new ArrayList<Integer>());

        Polynomial output = new Polynomial(constantTerm);
        for (Integer var : variables) {
            if (var >= other.size()) {
                throw new ArrayIndexOutOfBoundsException(var);
            }
            Polynomial applicant = other.get(var);
            output = output.multiply(applicant);
        }
        return output;
    }

    @Override
    public java.lang.String toString() {
        StringBuilder builder = new StringBuilder();
        if (variables.size() == 0 || !coeff.equals(BigFraction.ONE)) {
            builder.append(coeff);
        }
        Integer prevVar = -1;
        int exponent = 0;
        for (Integer var : variables) {
            if (!var.equals(prevVar)) {
                if (exponent != 0) {
                    builder.append(" x").append(prevVar);
                }

                if (exponent != 1 && exponent != 0) {
                    builder.append("^").append(exponent);
                }
                prevVar = var;
                exponent = 1;
            } else {
                exponent++;
            }
        }
        if (exponent != 0) {
            builder.append(" x").append(prevVar);
        }

        if (exponent != 1 && exponent != 0) {
            builder.append("^").append(exponent);
        }
        return builder.toString();
    }

    private static Map<Integer, Integer> compress(List<Integer> variables) {
        Map<Integer, Integer> map = new TreeMap<>();
        for (Integer var : variables) {
            if (map.containsKey(var)) {
                map.put(var, map.get(var) + 1);
            } else {
                map.put(var, 1);
            }
        }
        return map;
    }

    public BigFraction accumulate(List<Integer> alphas) {
        BigFraction output = coeff;
        for (Map.Entry<Integer, Integer> var : compressed.entrySet()) {

            Integer b = var.getKey() + 1;
            Integer e = var.getValue();

            for (Integer a : alphas) {
                if (a % b == 0) {
                    output = output.multiply(Combinations.nCr(e + a / b - 1, a / b));
                } else {
                    return BigFraction.ZERO;
                }
            }
        }
        return output;
    }

    public boolean isZero() {
        return coeff.equals(BigFraction.ZERO);
    }
}
