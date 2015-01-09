package me.asv.coins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class Solver {
    public static final int DIM = 4;
    public static final int SIZE = DIM * DIM;

    public interface Move {
        public Board applyMove(Board b);
    }

    public static enum AlphaMove implements Move {
        LEFT(-1,0),
        RIGHT(+1, 0),
        TOP(0, 1),
        BOTTOM(0, -1);

        private final int xd;
        private final int yd;

        private AlphaMove(int xd, int yd) {
            this.xd = xd;
            this.yd = yd;
        }

        public Board applyMove(Board b) {
            return b.makeAlphaMove(this);
        }
    }

    private static class BetaMove implements Move {
        private final int pos;
        private final int val;

        BetaMove(int pos, int val) {
            this.pos = pos;
            this.val = val;
        }

        public Board applyMove(Board b) {
            return b.makeBetaMove(this);
        }
    }

    public static class Board {

        private final int b[];

        private final boolean alphaTurn;

        private Board(int b[], boolean alphaTurn) {
            this.b = b;
            this.alphaTurn = alphaTurn;
        }

        public static class Builder {

            private int b[];

            private boolean alphaTurn;

            public Builder fresh() {
                this.b = new int[SIZE];
                return this;
            }

            public Builder copy(int[] b) {
                this.b = Arrays.copyOf(b, SIZE);
                return this;
            }

            public Builder turn(boolean alphaTurn) {
                this.alphaTurn = alphaTurn;
                return this;
            }

            public Builder at(int pos, int val) {
                this.b[pos] = val;
                return this;
            }

            public Board build() {
                return new Board(this.b, this.alphaTurn);
            }

        }

        public Board makeAlphaMove(AlphaMove alphaMove) {
            int startX = alphaMove.xd < 0 ? 0 : DIM - 1;
            int startY = alphaMove.yd < 0 ? 0 : DIM - 1;
            boolean moved = false;
            Board.Builder builder = new Builder().fresh();

            for (int x = startX; isBound(x); x -= alphaMove.xd) {
                for (int y = startY; isBound(y); y -= alphaMove.yd) {
                    int x2 = x + alphaMove.xd;
                    int y2 = y + alphaMove.yd;
                    if (isBound(x2) && isBound(y2)) {
                        if (b[pos(x2, y2)] == b[pos(x, y)]) {
                            builder.at(pos(x, y), b[pos(x2, y2) + b[pos(x,y)]]);
                            moved = true;
                        } else {
                            builder.at(pos(x, y), b[pos(x2, y2) + b[pos(x,y)]]);
                        }
                    }
                }
            }
            if (!moved) {
                throw new IllegalStateException();
            }
            return builder.build();
        }



        public boolean isBound(int p) {
            return p >= 0 && p < DIM;
        }

        public List<Move> moveList() {
            List<Move> moveList = new ArrayList<>();

            if (alphaTurn) {
                for (AlphaMove move : AlphaMove.values()) {
                    boolean accept = false;
                    for (int i = 0; i < DIM; i++) {
                        for (int j = 0; j < DIM; j++) {
                            int ii = i + move.xd;
                            int jj = j + move.yd;
                            if (isBound(ii) && isBound(jj)) {
                                if (b[pos(ii, jj)] == b[pos(i, j)]) {
                                    accept = true;
                                    break;
                                }
                            }
                        }
                        if (accept) {
                            break;
                        }
                    }
                    if (accept) {
                        moveList.add(move);
                    }
                }
            } else {
                for (int i = 0; i < SIZE; i++) {
                    if (b[i] == 0) {
                        BetaMove lowMove = new BetaMove(i, 2);
                        BetaMove highMove = new BetaMove(i, 2);
                        moveList.add(lowMove);
                        moveList.add(highMove);
                    }
                }
            }

            return moveList;
        }

        public int score() {
            int sum = 0;
            for (int i = 0; i < SIZE; i++) {
                sum += b[i] * b[i];
            }
            return alphaTurn ? -sum : sum;
        }

        public Board makeBetaMove(BetaMove betaMove) {
            int pos = betaMove.pos;
            if (b[pos] == 0) {
                return new Builder().copy(b).at(pos, betaMove.val).build();
            } else {
                throw new IllegalStateException(toString() + " has position " + pos + " occupied");
            }
        }
    }

    public static int pos(int x, int y) {
        return x * DIM + y;
    }

    public static class Engine {

        public int score(Board board, int depth) {
            if (depth == 0) {
                return board.score();
            }

            List<Move> moveList = board.moveList();

            int bestScore = Integer.MIN_VALUE;
            for (Move betaMove : moveList) {
                Board b = betaMove.applyMove(board);
                int score = score(b, depth - 1);
                if (bestScore < score) {
                    bestScore = score;
                }
            }
            return -bestScore;
        }

        public AlphaMove bestAlphaMove(Board board, int depth) {
            int bestScore = Integer.MIN_VALUE;
            AlphaMove bestMove = null;
            for (AlphaMove alphaMove : AlphaMove.values()) {
                Board b = board.makeAlphaMove(alphaMove);
                if (b != null) {
                    int sc = score(board, depth - 1);
                    if (sc > bestScore) {
                        bestScore = sc;
                        bestMove = alphaMove;
                    }
                }
            }
            return bestMove;
        }

        public AlphaMove bestAlphaMove(Board board) {
            return bestAlphaMove(board, 10);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(System.in));

        Board board = new Board.Builder()
                .at(pos(3, 0), 2)
                .at(pos(1, 2), 2)
                .build();

        Engine engine = new Engine();

        while (true) {
            AlphaMove alphaMove = engine.bestAlphaMove(board);
            System.out.println("Make Move: " + alphaMove);
            board = board.makeAlphaMove(alphaMove);
            int m = Integer.parseInt(inputStreamReader.readLine());
            int n = Integer.parseInt(inputStreamReader.readLine());
            int v = Integer.parseInt(inputStreamReader.readLine());
//            BetaMove betaMove = new BetaMove(m, n);
        }
    }

}
