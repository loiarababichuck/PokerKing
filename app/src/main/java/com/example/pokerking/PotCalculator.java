package com.example.pokerking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class PotCalculator {

    public static PotCalculationResult calculatePots(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return new PotCalculationResult(Collections.emptyList(), Collections.emptyMap());
        }

        Map<Player, Integer> contributions = new HashMap<>();
        TreeSet<Integer> levelsSet = new TreeSet<>();
        int originalTotalContributions = 0;

        for (Player p : players) {
            int contrib = p.getTotalHandContribution();
            if (contrib > 0) {
                contributions.put(p, contrib);
                levelsSet.add(contrib);
                originalTotalContributions += contrib;
            }
        }

        if (contributions.isEmpty()) {
            return new PotCalculationResult(Collections.emptyList(), Collections.emptyMap());
        }

        List<Integer> levels = new ArrayList<>(levelsSet);
        Map<Player, Integer> uncalledRefunds = new HashMap<>();

        while (!levels.isEmpty()) {
            int topLevel = levels.get(levels.size() - 1);
            List<Player> topContributors = new ArrayList<>();
            for (Map.Entry<Player, Integer> entry : contributions.entrySet()) {
                if (entry.getValue() == topLevel) {
                    topContributors.add(entry.getKey());
                }
            }

            if (topContributors.size() == 1) {
                Player highestBettor = topContributors.get(0);
                int secondHighestLevel = levels.size() > 1 ? levels.get(levels.size() - 2) : 0;
                int uncalledAmount = topLevel - secondHighestLevel;

                if (uncalledAmount > 0) {
                    Integer prevRefund = uncalledRefunds.get(highestBettor);
                    int currentRef = prevRefund != null ? prevRefund : 0;
                    uncalledRefunds.put(highestBettor, currentRef + uncalledAmount);
                    contributions.put(highestBettor, secondHighestLevel);
                }

                levels.remove(levels.size() - 1);
                if (secondHighestLevel == 0) {
                    contributions.remove(highestBettor);
                }
            } else {
                break;
            }
        }

        List<Pot> pots = new ArrayList<>();
        int previousLevel = 0;

        for (int level : levels) {
            int diff = level - previousLevel;
            if (diff <= 0) continue;

            int potAmount = 0;
            List<Player> eligible = new ArrayList<>();

            for (Player p : players) {
                Integer contribVal = contributions.get(p);
                int contrib = contribVal != null ? contribVal : 0;
                if (contrib >= level) {
                    potAmount += diff;
                    if (!p.isFolded() && !p.isEliminated()) {
                        eligible.add(p);
                    }
                }
            }

            if (potAmount > 0) {
                if (!eligible.isEmpty()) {
                    pots.add(new Pot(potAmount, eligible));
                } else if (!pots.isEmpty()) {
                    Pot lastPot = pots.remove(pots.size() - 1);
                    pots.add(new Pot(lastPot.getAmount() + potAmount, lastPot.getEligiblePlayers()));
                }
            }

            previousLevel = level;
        }

        int totalInPots = 0;
        for (Pot pot : pots) {
            totalInPots += pot.getAmount();
        }
        int totalRefunds = 0;
        for (int ref : uncalledRefunds.values()) {
            totalRefunds += ref;
        }

        if (totalInPots + totalRefunds != originalTotalContributions) {
            throw new IllegalStateException("Integridade financeira violada: total das contribuições ("
                    + originalTotalContributions + ") diferente da soma dos potes (" + totalInPots
                    + ") e reembolsos (" + totalRefunds + ").");
        }

        return new PotCalculationResult(pots, uncalledRefunds);
    }
}
