package com.example.pokerking;

public class Card {
    public enum Suit {
        CLUBS("♣"), DIAMONDS("♦"), HEARTS("♥"), SPADES("♠");

        private final String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean isRed() {
            return this == DIAMONDS || this == HEARTS;
        }
    }

    private final Suit suit;
    private final int rank;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public int getValue() {
        return rank == 1 ? 14 : rank;
    }

    public boolean isRed() {
        return suit.isRed();
    }

    public String getDisplayValue() {
        if (rank == 1) {
            return "A";
        }
        if (rank == 11) {
            return "J";
        }
        if (rank == 12) {
            return "Q";
        }
        if (rank == 13) {
            return "K";
        }
        return String.valueOf(rank);
    }

    public String getDisplayLabel() {
        return getDisplayValue() + suit.getSymbol();
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
