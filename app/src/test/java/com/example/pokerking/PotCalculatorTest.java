package com.example.pokerking;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PotCalculatorTest {

    @Test
    public void test1_AllInEveryoneCallsShowdown() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 100);
        Player pC = new Player("C", 100);

        pA.betChips(100);
        pB.betChips(100);
        pC.betChips(100);

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));

        assertEquals(1, result.getPots().size());
        Pot mainPot = result.getPots().get(0);
        assertEquals(300, mainPot.getAmount());
        assertEquals(3, mainPot.getEligiblePlayers().size());
        assertTrue(mainPot.isEligible(pA));
        assertTrue(mainPot.isEligible(pB));
        assertTrue(mainPot.isEligible(pC));
        assertEquals(0, result.getTotalRefundAmount());
    }

    @Test
    public void test2_AllInTwoPlayersContinueBetting() {
        Player pA = new Player("A", 200);
        Player pB = new Player("B", 1000);
        Player pC = new Player("C", 1000);

        pA.betChips(200);
        pB.betChips(200);
        pC.betChips(200);

        // Later rounds: B and C bet 300 more each
        pB.betChips(300);
        pC.betChips(300);

        assertEquals(200, pA.getTotalHandContribution());
        assertEquals(500, pB.getTotalHandContribution());
        assertEquals(500, pC.getTotalHandContribution());

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));

        List<Pot> pots = result.getPots();
        assertEquals(2, pots.size());

        Pot mainPot = pots.get(0);
        assertEquals(600, mainPot.getAmount()); // 200 * 3
        assertEquals(3, mainPot.getEligiblePlayers().size());

        Pot sidePot = pots.get(1);
        assertEquals(600, sidePot.getAmount()); // (500-200) * 2
        assertEquals(2, sidePot.getEligiblePlayers().size());
        assertFalse(sidePot.isEligible(pA));
        assertTrue(sidePot.isEligible(pB));
        assertTrue(sidePot.isEligible(pC));
    }

    @Test
    public void test3_AllInWinsMainPotIneligibleForSide() {
        Player pA = new Player("A", 200);
        Player pB = new Player("B", 500);
        Player pC = new Player("C", 500);

        pA.betChips(200);
        pB.betChips(500);
        pC.betChips(500);

        // Hole cards
        // A: Royal Flush combo
        pA.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.SPADES, 13));
        // B: Pocket Aces (Three Aces with J, Q on board)
        pB.receiveCards(new Card(Card.Suit.HEARTS, 14), new Card(Card.Suit.CLUBS, 14));
        // C: Pocket Sevens (Pair of Sevens)
        pC.receiveCards(new Card(Card.Suit.HEARTS, 7), new Card(Card.Suit.CLUBS, 7));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.SPADES, 12),
                new Card(Card.Suit.SPADES, 11),
                new Card(Card.Suit.SPADES, 10),
                new Card(Card.Suit.DIAMONDS, 2),
                new Card(Card.Suit.CLUBS, 3)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));
        List<PotEvaluator.PotAwardDetails> awards = PotEvaluator.awardPots(result, community);

        assertEquals(2, awards.size());

        // Main pot awarded to A
        PotEvaluator.PotAwardDetails mainAward = awards.get(0);
        assertEquals(600, mainAward.getPot().getAmount());
        assertEquals(1, mainAward.getWinners().size());
        assertEquals(pA, mainAward.getWinners().get(0));

        // Side pot awarded to B
        PotEvaluator.PotAwardDetails sideAward = awards.get(1);
        assertEquals(600, sideAward.getPot().getAmount());
        assertEquals(1, sideAward.getWinners().size());
        assertEquals(pB, sideAward.getWinners().get(0));

        // Final chips
        assertEquals(600, pA.getChips());
        assertEquals(600, pB.getChips());
        assertEquals(0, pC.getChips());
    }

    @Test
    public void test4_OneWinsMainOtherWinsSide() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 300);
        Player pC = new Player("C", 300);

        pA.betChips(100);
        pB.betChips(300);
        pC.betChips(300);

        // A has Pair of Aces
        pA.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.HEARTS, 14));
        // B has Pair of Kings
        pB.receiveCards(new Card(Card.Suit.SPADES, 13), new Card(Card.Suit.HEARTS, 13));
        // C has Pair of Queens
        pC.receiveCards(new Card(Card.Suit.SPADES, 12), new Card(Card.Suit.HEARTS, 12));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.SPADES, 9)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));
        PotEvaluator.awardPots(result, community);

        assertEquals(300, pA.getChips()); // Main pot (300)
        assertEquals(400, pB.getChips()); // Side pot (400)
        assertEquals(0, pC.getChips());
    }

    @Test
    public void test5_SamePlayerWinsMainAndSide() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 300);
        Player pC = new Player("C", 300);

        pA.betChips(100);
        pB.betChips(300);
        pC.betChips(300);

        // B has Aces
        pA.receiveCards(new Card(Card.Suit.SPADES, 10), new Card(Card.Suit.HEARTS, 10));
        pB.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.HEARTS, 14));
        pC.receiveCards(new Card(Card.Suit.SPADES, 12), new Card(Card.Suit.HEARTS, 12));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.SPADES, 9)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));
        PotEvaluator.awardPots(result, community);

        assertEquals(0, pA.getChips());
        assertEquals(700, pB.getChips()); // 300 + 400
        assertEquals(0, pC.getChips());
    }

    @Test
    public void test6_TwoPlayersTieMainPot() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 100);

        pA.betChips(100);
        pB.betChips(100);

        // Both A and B have Ace-King
        pA.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.SPADES, 13));
        pB.receiveCards(new Card(Card.Suit.HEARTS, 14), new Card(Card.Suit.HEARTS, 13));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.CLUBS, 9)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB));
        PotEvaluator.awardPots(result, community);

        assertEquals(100, pA.getChips());
        assertEquals(100, pB.getChips());
    }

    @Test
    public void test7_TwoPlayersTieSidePot() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 300);
        Player pC = new Player("C", 300);

        pA.betChips(100);
        pB.betChips(300);
        pC.betChips(300);

        // A has Triple Aces (wins main pot)
        pA.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.HEARTS, 14));
        // B and C both have Kings (tie for side pot)
        pB.receiveCards(new Card(Card.Suit.SPADES, 13), new Card(Card.Suit.CLUBS, 2));
        pC.receiveCards(new Card(Card.Suit.HEARTS, 13), new Card(Card.Suit.DIAMONDS, 3));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.CLUBS, 14),
                new Card(Card.Suit.DIAMONDS, 13),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.SPADES, 9)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));
        PotEvaluator.awardPots(result, community);

        assertEquals(300, pA.getChips()); // Main pot (300)
        assertEquals(200, pB.getChips()); // Side pot split (400 / 2 = 200)
        assertEquals(200, pC.getChips()); // Side pot split (400 / 2 = 200)
    }

    @Test
    public void test8_FoldedPlayerContributionKeptButIneligible() {
        Player pA = new Player("A", 300);
        Player pB = new Player("B", 300);
        Player pC = new Player("C", 100);

        pC.betChips(100);
        pC.setFolded(true); // C folds after contributing 100

        pA.betChips(300);
        pB.betChips(300);

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));

        assertEquals(2, result.getPots().size());
        Pot mainPot = result.getPots().get(0);
        assertEquals(300, mainPot.getAmount()); // 100 * 3
        assertEquals(2, mainPot.getEligiblePlayers().size());
        assertFalse(mainPot.isEligible(pC));

        Pot sidePot = result.getPots().get(1);
        assertEquals(400, sidePot.getAmount()); // (300-100) * 2
        assertEquals(2, sidePot.getEligiblePlayers().size());
        assertFalse(sidePot.isEligible(pC));
    }

    @Test
    public void test9_MultipleSidePotsThreeDistinctContributionLevels() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 250);
        Player pC = new Player("C", 500);
        Player pD = new Player("D", 500);

        pA.betChips(100);
        pB.betChips(250);
        pC.betChips(500);
        pD.betChips(500);

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC, pD));

        List<Pot> pots = result.getPots();
        assertEquals(3, pots.size());

        // Level 1: 100 * 4 = 400
        assertEquals(400, pots.get(0).getAmount());
        assertEquals(4, pots.get(0).getEligiblePlayers().size());

        // Level 2: 150 * 3 = 450
        assertEquals(450, pots.get(1).getAmount());
        assertEquals(3, pots.get(1).getEligiblePlayers().size());
        assertFalse(pots.get(1).isEligible(pA));

        // Level 3: 250 * 2 = 500
        assertEquals(500, pots.get(2).getAmount());
        assertEquals(2, pots.get(2).getEligiblePlayers().size());
        assertTrue(pots.get(2).isEligible(pC));
        assertTrue(pots.get(2).isEligible(pD));
    }

    @Test
    public void test10_UncalledExcessBetReturned() {
        Player pA = new Player("A", 300);
        Player pB = new Player("B", 1000);

        pA.betChips(300);
        pB.betChips(800);

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB));

        // Uncalled refund of 500 to B
        Map<Player, Integer> refunds = result.getUncalledRefunds();
        assertEquals(1, refunds.size());
        assertEquals(Integer.valueOf(500), refunds.get(pB));

        // Process refund
        Integer ref = refunds.get(pB);
        if (ref != null) {
            pB.addChips(ref);
        }

        List<Pot> pots = result.getPots();
        assertEquals(1, pots.size());
        assertEquals(600, pots.get(0).getAmount()); // 300 * 2
    }

    @Test
    public void test11_EveryoneFoldsExceptOne() {
        Player pA = new Player("A", 100);
        Player pB = new Player("B", 100);
        Player pC = new Player("C", 100);

        pA.betChips(100);
        pA.setFolded(true);

        pB.betChips(100);
        pB.setFolded(true);

        pC.betChips(100); // C is sole remaining player

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC));
        PotEvaluator.awardPots(result, null);

        assertEquals(0, pA.getChips());
        assertEquals(0, pB.getChips());
        assertEquals(300, pC.getChips()); // 0 remaining + 300 pot = 300
    }

    @Test
    public void test12_ContributionsAccumulateAcrossRounds() {
        Player pA = new Player("A", 1000);

        pA.betChips(50);  // Preflop
        assertEquals(50, pA.getRoundBet());
        assertEquals(50, pA.getTotalHandContribution());

        pA.resetStageBets(); // Reset for Flop
        assertEquals(0, pA.getRoundBet());
        assertEquals(50, pA.getTotalHandContribution());

        pA.betChips(100); // Flop
        assertEquals(100, pA.getRoundBet());
        assertEquals(150, pA.getTotalHandContribution());

        pA.resetStageBets(); // Reset for Turn
        pA.betChips(150); // Turn
        assertEquals(150, pA.getRoundBet());
        assertEquals(300, pA.getTotalHandContribution());
    }

    @Test
    public void test13_FinancialIntegrityNoChipsCreatedOrLost() {
        Player pA = new Player("A", 1000);
        Player pB = new Player("B", 1000);
        Player pC = new Player("C", 1000);
        Player pD = new Player("D", 1000);

        int startingTotalChips = pA.getChips() + pB.getChips() + pC.getChips() + pD.getChips();

        pA.betChips(100);
        pB.betChips(250);
        pC.betChips(600);
        pD.betChips(1000);

        pA.receiveCards(new Card(Card.Suit.SPADES, 2), new Card(Card.Suit.HEARTS, 3));
        pB.receiveCards(new Card(Card.Suit.SPADES, 4), new Card(Card.Suit.HEARTS, 5));
        pC.receiveCards(new Card(Card.Suit.SPADES, 10), new Card(Card.Suit.HEARTS, 10));
        pD.receiveCards(new Card(Card.Suit.SPADES, 14), new Card(Card.Suit.HEARTS, 14));

        List<Card> community = Arrays.asList(
                new Card(Card.Suit.CLUBS, 2),
                new Card(Card.Suit.DIAMONDS, 4),
                new Card(Card.Suit.CLUBS, 6),
                new Card(Card.Suit.DIAMONDS, 8),
                new Card(Card.Suit.CLUBS, 9)
        );

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC, pD));

        for (Map.Entry<Player, Integer> entry : result.getUncalledRefunds().entrySet()) {
            entry.getKey().addChips(entry.getValue());
        }

        PotEvaluator.awardPots(result, community);

        int endingTotalChips = pA.getChips() + pB.getChips() + pC.getChips() + pD.getChips();

        assertEquals(startingTotalChips, endingTotalChips);
    }

    @Test
    public void test14_Section63ThreeLevelExample() {
        Player pA = new Player("A", 300);
        Player pB = new Player("B", 700);
        Player pC = new Player("C", 1200);
        Player pD = new Player("D", 1200);

        pA.betChips(300);
        pB.betChips(700);
        pC.betChips(1200);
        pD.betChips(1200);

        PotCalculationResult result = PotCalculator.calculatePots(Arrays.asList(pA, pB, pC, pD));

        List<Pot> pots = result.getPots();
        assertEquals(3, pots.size());

        // Level 1 (0 to 300): 300 * 4 = 1200
        assertEquals(1200, pots.get(0).getAmount());
        assertEquals(4, pots.get(0).getEligiblePlayers().size());
        assertTrue(pots.get(0).getEligiblePlayers().containsAll(Arrays.asList(pA, pB, pC, pD)));

        // Level 2 (300 to 700): (700-300) * 3 = 1200
        assertEquals(1200, pots.get(1).getAmount());
        assertEquals(3, pots.get(1).getEligiblePlayers().size());
        assertTrue(pots.get(1).getEligiblePlayers().containsAll(Arrays.asList(pB, pC, pD)));

        // Level 3 (700 to 1200): (1200-700) * 2 = 1000
        assertEquals(1000, pots.get(2).getAmount());
        assertEquals(2, pots.get(2).getEligiblePlayers().size());
        assertTrue(pots.get(2).getEligiblePlayers().containsAll(Arrays.asList(pC, pD)));

        assertEquals(3400, result.getTotalPotAmount());
    }
}
