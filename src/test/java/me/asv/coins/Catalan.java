package me.asv.coins;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.regex.Pattern;

/**
 */
public class Catalan {

    private static final Pattern NUMBER = Pattern.compile("^\\d++$");

    private static final List<String> SINGLETON = ImmutableList.of("");

    private static final Map<Integer, Iterable<String>> cache = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1 || !NUMBER.matcher(args[0]).matches()) {
            return;
        }
        int n = Integer.valueOf(args[0]);

        for (String seq : getSequences(n)) {
            System.out.println("Seq: " + seq);
        }
    }

    private static Iterable<String> getSequences(int n) {
        if (n == 0) {
            return SINGLETON;
        }
        // This optimization doesn't save much, because iteration over the result
        // will still take exponential amount of time.
        if (cache.containsKey(n)) {
            return cache.get(n);
        }
        List<Iterable<String>> fullList = new ArrayList<>();

        for (int k = 0; k <= n - 1; k++) {
            final Iterable<String> left = getSequences(k);
            final Iterable<String> right = getSequences(n - k - 1);
            fullList.add(new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {

                        Iterator<String> outerLoop = null;
                        Iterator<String> innerLoop = null;

                        private String leftValue = null;

                        @Override
                        public boolean hasNext() {
                            if (innerLoop != null) {
                                if (innerLoop.hasNext()) {
                                    return true;
                                }
                            }
                            if (outerLoop == null) {
                                outerLoop = left.iterator();
                            }
                            if (outerLoop.hasNext()) {
                                leftValue = outerLoop.next();
                            } else {
                                return false;
                            }
                            // Either innerloop is unset, or has nothing more to read.
                            innerLoop = right.iterator();
                            return hasNext();
                        }

                        @Override
                        public String next() {
                            if (hasNext()) {
                                return "(" + leftValue + ")" + innerLoop.next();
                            } else {
                                throw new IllegalStateException();
                            }
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            });
        }

        Iterable<String> list =  Iterables.concat(fullList);
        cache.put(n, list);
        return list;
    }
}
