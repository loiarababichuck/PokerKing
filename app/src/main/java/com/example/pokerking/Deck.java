package com.example.pokerking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private int nextCardIndex = 0;

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                cards.add(new Card(suit, rank));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
        nextCardIndex = 0;
    }

    public Card drawCard() {
        if (nextCardIndex >= cards.size()) {
            return null;
        }
        return cards.get(nextCardIndex++);
    }

    public List<Card> drawCards(int amount) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Card card = drawCard();
            if (card != null) {
                drawn.add(card);
            }
        }
        return drawn;
    }

    public boolean isEmpty() {
        return nextCardIndex >= cards.size();
    }
}
