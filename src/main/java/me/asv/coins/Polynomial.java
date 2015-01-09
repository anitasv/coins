package me.asv.coins;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.*;

public class Polynomial {

    private final List<PolynomialTerm> terms;
    private final Map<List<Integer>, PolynomialTerm> termMap;

    public Polynomial(List<PolynomialTerm> terms) {
        this.terms = filter(terms);
        this.termMap = new HashMap<>();
        for (PolynomialTerm term : this.terms) {
            termMap.put(term.getVariables(), term);
        }
    }

    public Polynomial() {
        this(new ArrayList<PolynomialTerm>());
    }

    public static List<PolynomialTerm>  newTermList(PolynomialTerm single) {
        List<PolynomialTerm> terms = new ArrayList<>();
        terms.add(single);
        return terms;
    }

    public Polynomial(PolynomialTerm single) {
        this(newTermList(single));
    }

    private static List<PolynomialTerm> filter(List<PolynomialTerm> input) {
        List<PolynomialTerm> output = new ArrayList<>();
        for (PolynomialTerm in : input) {
            if (!in.isZero()) {
                output.add(in);
            }
        }
        return output;
    }

    public Polynomial add(Polynomial other) {
        Set<List<Integer>> accounted = new HashSet<>();
        List<PolynomialTerm> outputTerms = new ArrayList<>();
        for (PolynomialTerm oterm : other.terms) {
            if (termMap.containsKey(oterm.getVariables())) {
                accounted.add(oterm.getVariables());
                outputTerms.add(oterm.add(termMap.get(oterm.getVariables())));
            } else {
                outputTerms.add(oterm);
            }
        }
        for (PolynomialTerm mterm : terms) {
            if (!accounted.contains(mterm.getVariables())) {
                outputTerms.add(mterm);
            }
        }
        return new Polynomial(outputTerms);
    }

    public Polynomial multiply(Polynomial other) {
       Map<List<Integer>, PolynomialTerm> newTermMap = new HashMap<>();

        for (PolynomialTerm oterm : other.terms) {
            for (PolynomialTerm mterm : terms) {
                PolynomialTerm temp = oterm.multiply(mterm);
                if (newTermMap.containsKey(temp.getVariables())) {
                    PolynomialTerm prev = newTermMap.remove(temp.getVariables());
                    newTermMap.put(temp.getVariables(), prev.add(temp));
                } else {
                    newTermMap.put(temp.getVariables(), temp);
                }
            }
        }
        List<PolynomialTerm> outputTerms = new ArrayList<>();
        outputTerms.addAll(newTermMap.values());
        return new Polynomial(outputTerms);
    }

    public Polynomial apply(List<Polynomial> other) {
        Polynomial output = new Polynomial(new ArrayList<PolynomialTerm>());

        for (PolynomialTerm term : terms) {
            output = output.add(term.apply(other));
        }
        return output;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (PolynomialTerm term : terms) {
            if (!first) {
                builder.append(" + ");
            } else {
                first = false;
            }
            builder.append(term);
        }
        return builder.toString();
    }

    public BigFraction accumulate(List<Integer> alphas) {
        BigFraction sum = BigFraction.ZERO;
        for (PolynomialTerm term : terms) {
            sum = sum.add(term.accumulate(alphas));
        }
        return sum;
    }
    public BigFraction accumulate2(List<Integer> alphas) {
        BigFraction sum = BigFraction.ZERO;
        for (PolynomialTerm term : terms) {
            sum = sum.add(term.accumulate2(alphas));
        }
        return sum;
    }
}

