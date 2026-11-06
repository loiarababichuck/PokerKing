package com.example.pokerking;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HandEvaluatorTest {

    @Test
    public void testCaso1_FlushAceKicker() {
        List<Card> board = Arrays.asList(
                new Card(Card.Suit.HEARTS, 6),
                new Card(Card.Suit.SPADES, 12),
                new Card(Card.Suit.HEARTS, 13),
                new Card(Card.Suit.HEARTS, 2),
                new Card(Card.Suit.HEARTS, 7)
        );

        List<Card> handA = new ArrayList<>(board);
        handA.add(new Card(Card.Suit.HEARTS, 8));
        handA.add(new Card(Card.Suit.SPADES, 3));

        List<Card> handB = new ArrayList<>(board);
        handB.add(new Card(Card.Suit.DIAMONDS, 1));
        handB.add(new Card(Card.Suit.HEARTS, 1));

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Flush", resA.getName());
        assertEquals("Flush", resB.getName());
        assertTrue(HandEvaluator.compareHandResults(resB, resA) > 0);
    }

    @Test
    public void testCaso2_ThreeOfAKindAceKicker() {
        List<Card> board = Arrays.asList(
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 9),
                new Card(Card.Suit.SPADES, 9),
                new Card(Card.Suit.CLUBS, 9),
                new Card(Card.Suit.SPADES, 3)
        );

        List<Card> handA = new ArrayList<>(board);
        handA.add(new Card(Card.Suit.CLUBS, 7));
        handA.add(new Card(Card.Suit.HEARTS, 8));

        List<Card> handB = new ArrayList<>(board);
        handB.add(new Card(Card.Suit.CLUBS, 1));
        handB.add(new Card(Card.Suit.SPADES, 8));

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Three of a Kind", resA.getName());
        assertEquals("Three of a Kind", resB.getName());
        assertTrue(HandEvaluator.compareHandResults(resB, resA) > 0);
    }

    @Test
    public void testCaso3_TwoPairAceHighOverFourHigh() {
        List<Card> board = Arrays.asList(
                new Card(Card.Suit.SPADES, 6),
                new Card(Card.Suit.DIAMONDS, 6),
                new Card(Card.Suit.HEARTS, 5),
                new Card(Card.Suit.CLUBS, 4),
                new Card(Card.Suit.SPADES, 1)
        );

        List<Card> handA = new ArrayList<>(board);
        handA.add(new Card(Card.Suit.DIAMONDS, 4));
        handA.add(new Card(Card.Suit.HEARTS, 10));

        List<Card> handB = new ArrayList<>(board);
        handB.add(new Card(Card.Suit.CLUBS, 3));
        handB.add(new Card(Card.Suit.CLUBS, 1));

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Two Pair", resA.getName());
        assertEquals("Two Pair", resB.getName());
        assertTrue(HandEvaluator.compareHandResults(resB, resA) > 0);
    }

    @Test
    public void testCaso4_TwoPairAcesAndNinesOverKingsAndNines() {
        List<Card> board = Arrays.asList(
                new Card(Card.Suit.HEARTS, 4),
                new Card(Card.Suit.HEARTS, 9),
                new Card(Card.Suit.SPADES, 12),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.CLUBS, 9)
        );

        List<Card> handA = new ArrayList<>(board);
        handA.add(new Card(Card.Suit.DIAMONDS, 13));
        handA.add(new Card(Card.Suit.HEARTS, 13));

        List<Card> handB = new ArrayList<>(board);
        handB.add(new Card(Card.Suit.DIAMONDS, 1));
        handB.add(new Card(Card.Suit.SPADES, 1));

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Two Pair", resA.getName());
        assertEquals("Two Pair", resB.getName());
        assertTrue(HandEvaluator.compareHandResults(resB, resA) > 0);
    }

    @Test
    public void testRoyalFlush() {
        List<Card> cards = Arrays.asList(
                new Card(Card.Suit.SPADES, 1),
                new Card(Card.Suit.SPADES, 13),
                new Card(Card.Suit.SPADES, 12),
                new Card(Card.Suit.SPADES, 11),
                new Card(Card.Suit.SPADES, 10),
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 3)
        );

        HandEvaluator.HandResult res = HandEvaluator.evaluateBestSevenCardHandResult(cards);
        assertEquals("Royal Flush", res.getName());
        assertEquals(10, res.getCategory());
    }

    @Test
    public void testWheelStraightLosesToHigherStraight() {
        List<Card> wheelCards = Arrays.asList(
                new Card(Card.Suit.SPADES, 1),
                new Card(Card.Suit.HEARTS, 2),
                new Card(Card.Suit.CLUBS, 3),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.SPADES, 5),
                new Card(Card.Suit.CLUBS, 9),
                new Card(Card.Suit.HEARTS, 10)
        );

        List<Card> higherCards = Arrays.asList(
                new Card(Card.Suit.HEARTS, 2),
                new Card(Card.Suit.CLUBS, 3),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.SPADES, 5),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.HEARTS, 9),
                new Card(Card.Suit.SPADES, 10)
        );

        HandEvaluator.HandResult wheelRes = HandEvaluator.evaluateBestSevenCardHandResult(wheelCards);
        HandEvaluator.HandResult higherRes = HandEvaluator.evaluateBestSevenCardHandResult(higherCards);

        assertEquals("Straight", wheelRes.getName());
        assertEquals("Straight", higherRes.getName());
        assertEquals(Integer.valueOf(5), wheelRes.getTiebreakers().get(0));
        assertEquals(Integer.valueOf(6), higherRes.getTiebreakers().get(0));

        assertTrue(HandEvaluator.compareHandResults(higherRes, wheelRes) > 0);
    }

    @Test
    public void testAceHighStraight() {
        List<Card> cards = Arrays.asList(
                new Card(Card.Suit.SPADES, 10),
                new Card(Card.Suit.HEARTS, 11),
                new Card(Card.Suit.CLUBS, 12),
                new Card(Card.Suit.DIAMONDS, 13),
                new Card(Card.Suit.SPADES, 1),
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.HEARTS, 3)
        );

        HandEvaluator.HandResult res = HandEvaluator.evaluateBestSevenCardHandResult(cards);
        assertEquals("Straight", res.getName());
        assertEquals(Integer.valueOf(14), res.getTiebreakers().get(0));
    }

    @Test
    public void testPairAcesBeatsPairKings() {
        List<Card> handA = Arrays.asList(
                new Card(Card.Suit.SPADES, 1),
                new Card(Card.Suit.HEARTS, 1),
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.SPADES, 6),
                new Card(Card.Suit.CLUBS, 8),
                new Card(Card.Suit.HEARTS, 10)
        );

        List<Card> handB = Arrays.asList(
                new Card(Card.Suit.SPADES, 13),
                new Card(Card.Suit.HEARTS, 13),
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.SPADES, 6),
                new Card(Card.Suit.CLUBS, 8),
                new Card(Card.Suit.HEARTS, 10)
        );

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Pair", resA.getName());
        assertEquals("Pair", resB.getName());
        assertTrue(HandEvaluator.compareHandResults(resA, resB) > 0);
    }

    @Test
    public void testTieOnSameBoardStraight() {
        List<Card> board = Arrays.asList(
                new Card(Card.Suit.SPADES, 5),
                new Card(Card.Suit.HEARTS, 6),
                new Card(Card.Suit.CLUBS, 7),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.SPADES, 9)
        );

        List<Card> handA = new ArrayList<>(board);
        handA.add(new Card(Card.Suit.CLUBS, 2));
        handA.add(new Card(Card.Suit.DIAMONDS, 3));

        List<Card> handB = new ArrayList<>(board);
        handB.add(new Card(Card.Suit.HEARTS, 2));
        handB.add(new Card(Card.Suit.SPADES, 3));

        HandEvaluator.HandResult resA = HandEvaluator.evaluateBestSevenCardHandResult(handA);
        HandEvaluator.HandResult resB = HandEvaluator.evaluateBestSevenCardHandResult(handB);

        assertEquals("Straight", resA.getName());
        assertEquals("Straight", resB.getName());
        assertEquals(0, HandEvaluator.compareHandResults(resA, resB));
    }
}
