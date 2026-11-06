package com.example.pokerking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotEvaluator {

    public static class PotAwardDetails {
        private final Pot pot;
        private final List<Player> winners;
        private final HandEvaluator.HandResult bestHand;
        private final String potName;

        public PotAwardDetails(Pot pot, List<Player> winners, HandEvaluator.HandResult bestHand, String potName) {
            this.pot = pot;
            this.winners = new ArrayList<>(winners);
            this.bestHand = bestHand;
            this.potName = potName;
        }

        public Pot getPot() {
            return pot;
        }

        public List<Player> getWinners() {
            return new ArrayList<>(winners);
        }

        public HandEvaluator.HandResult getBestHand() {
            return bestHand;
        }

        public String getPotName() {
            return potName;
        }
    }

    public static List<PotAwardDetails> awardPots(PotCalculationResult calculationResult, List<Card> communityCards) {
        List<PotAwardDetails> awardDetailsList = new ArrayList<>();
        List<Pot> pots = calculationResult.getPots();

        for (int pIndex = 0; pIndex < pots.size(); pIndex++) {
            Pot pot = pots.get(pIndex);
            String potName;
            if (pIndex == 0) {
                potName = "Pote Principal";
            } else if (pIndex == 1) {
                potName = "Pote Secundário";
            } else if (pIndex == 2) {
                potName = "Pote Terciário";
            } else {
                potName = "Pote Secundário " + pIndex;
            }
            List<Player> eligible = pot.getEligiblePlayers();

            if (eligible.isEmpty()) {
                continue;
            }

            if (eligible.size() == 1) {
                Player winner = eligible.get(0);
                winner.addChips(pot.getAmount());
                awardDetailsList.add(new PotAwardDetails(pot, Collections.singletonList(winner), null, potName));
                continue;
            }

            HandEvaluator.HandResult bestResult = null;
            List<Player> winners = new ArrayList<>();

            for (Player player : eligible) {
                List<Card> combo = new ArrayList<>(player.getHoleCards());
                if (communityCards != null) {
                    combo.addAll(communityCards);
                }
                HandEvaluator.HandResult playerResult = HandEvaluator.evaluateBestSevenCardHandResult(combo);

                if (bestResult == null || HandEvaluator.compareHandResults(playerResult, bestResult) > 0) {
                    bestResult = playerResult;
                    winners.clear();
                    winners.add(player);
                } else if (HandEvaluator.compareHandResults(playerResult, bestResult) == 0) {
                    winners.add(player);
                }
            }

            if (!winners.isEmpty()) {
                int totalAmount = pot.getAmount();
                int splitAmount = totalAmount / winners.size();
                int remainder = totalAmount % winners.size();

                for (int i = 0; i < winners.size(); i++) {
                    Player winner = winners.get(i);
                    int award = splitAmount + (i == 0 ? remainder : 0);
                    winner.addChips(award);
                }

                awardDetailsList.add(new PotAwardDetails(pot, winners, bestResult, potName));
            }
        }

        return awardDetailsList;
    }
}
