package com.example.pokerking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class HandEvaluator {
    public static final class HandResult {
        private final String name;
        private final int category;
        private final List<Integer> tiebreakers;

        public HandResult(String name, int category, List<Integer> tiebreakers) {
            this.name = name;
            this.category = category;
            this.tiebreakers = new ArrayList<>(tiebreakers);
        }

        public String getName() {
            return name;
        }

        public int getCategory() {
            return category;
        }

        public List<Integer> getTiebreakers() {
            return new ArrayList<>(tiebreakers);
        }
    }

    public static String evaluateHand(List<Card> cards) {
        HandResult result = evaluateFiveCardHand(cards);
        return result != null ? result.getName() : "High Card";
    }

    public static String evaluateBestSevenCardHand(List<Card> cards) {
        HandResult result = evaluateBestSevenCardHandResult(cards);
        return result != null ? result.getName() : "High Card";
    }

    public static HandResult evaluateBestSevenCardHandResult(List<Card> cards) {
        if (cards == null || cards.size() < 5) {
            return new HandResult("High Card", 1, Collections.singletonList(0));
        }

        HandResult best = null;
        int total = cards.size();
        for (int i = 0; i <= total - 5; i++) {
            for (int j = i + 1; j <= total - 4; j++) {
                for (int k = j + 1; k <= total - 3; k++) {
                    for (int l = k + 1; l <= total - 2; l++) {
                        for (int m = l + 1; m <= total - 1; m++) {
                            List<Card> combo = new ArrayList<>();
                            combo.add(cards.get(i));
                            combo.add(cards.get(j));
                            combo.add(cards.get(k));
                            combo.add(cards.get(l));
                            combo.add(cards.get(m));

                            HandResult candidate = evaluateFiveCardHand(combo);
                            if (best == null || compareHandResults(candidate, best) > 0) {
                                best = candidate;
                            }
                        }
                    }
                }
            }
        }

        return best != null ? best : new HandResult("High Card", 1, Collections.singletonList(0));
    }

    public static int compareHandResults(HandResult left, HandResult right) {
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }

        if (left.getCategory() != right.getCategory()) {
            return Integer.compare(left.getCategory(), right.getCategory());
        }

        List<Integer> leftValues = left.getTiebreakers();
        List<Integer> rightValues = right.getTiebreakers();
        int maxSize = Math.max(leftValues.size(), rightValues.size());

        for (int i = 0; i < maxSize; i++) {
            int leftValue = i < leftValues.size() ? leftValues.get(i) : 0;
            int rightValue = i < rightValues.size() ? rightValues.get(i) : 0;
            if (leftValue != rightValue) {
                return leftValue > rightValue ? 1 : -1;
            }
        }

        return 0;
    }

    public static int getPayout(String handName) {
        switch (handName) {
            case "Royal Flush":
                return 240;
            case "Straight Flush":
                return 180;
            case "Four of a Kind":
                return 120;
            case "Full House":
                return 80;
            case "Flush":
                return 60;
            case "Straight":
                return 40;
            case "Three of a Kind":
                return 25;
            case "Two Pair":
                return 15;
            case "Pair":
                return 10;
            default:
                return 0;
        }
    }

    private static HandResult evaluateFiveCardHand(List<Card> cards) {
        if (cards == null || cards.size() != 5) {
            return new HandResult("High Card", 1, Collections.singletonList(0));
        }

        List<Integer> ranks = new ArrayList<>();
        Map<Integer, Integer> countByRank = new HashMap<>();
        boolean flush = true;
        Card.Suit suit = cards.get(0).getSuit();

        for (Card card : cards) {
            int rank = card.getValue();
            ranks.add(rank);
            countByRank.put(rank, countByRank.getOrDefault(rank, 0) + 1);
            if (card.getSuit() != suit) {
                flush = false;
            }
        }

        List<Integer> sortedRanks = new ArrayList<>(ranks);
        Collections.sort(sortedRanks, Collections.reverseOrder());

        int straightHigh = getStraightHigh(ranks);
        boolean straight = straightHigh > 0;

        if (flush && straight) {
            if (straightHigh == 14) {
                return new HandResult("Royal Flush", 10, Collections.emptyList());
            }
            return new HandResult("Straight Flush", 9, Collections.singletonList(straightHigh));
        }

        int fourRank = getRankByCount(countByRank, 4);
        if (fourRank > 0) {
            int kicker = getHighestRankExcluding(countByRank, fourRank);
            List<Integer> tie = new ArrayList<>();
            tie.add(fourRank);
            tie.add(kicker);
            return new HandResult("Four of a Kind", 8, tie);
        }

        int tripleRank = getRankByCount(countByRank, 3);
        int pairRank = getRankByCount(countByRank, 2);
        if (tripleRank > 0 && pairRank > 0) {
            List<Integer> tie = new ArrayList<>();
            tie.add(tripleRank);
            tie.add(pairRank);
            return new HandResult("Full House", 7, tie);
        }

        if (flush) {
            return new HandResult("Flush", 6, sortedRanks.subList(0, 5));
        }

        if (straight) {
            return new HandResult("Straight", 5, Collections.singletonList(straightHigh));
        }

        if (tripleRank > 0) {
            List<Integer> tie = new ArrayList<>();
            tie.add(tripleRank);
            for (Integer rank : sortedRanks) {
                if (rank != tripleRank) {
                    tie.add(rank);
                    if (tie.size() == 3) {
                        break;
                    }
                }
            }
            return new HandResult("Three of a Kind", 4, tie);
        }

        List<Integer> pairRanks = getRanksByCount(countByRank, 2);
        if (pairRanks.size() == 2) {
            List<Integer> tie = new ArrayList<>();
            Collections.sort(pairRanks, Collections.reverseOrder());
            tie.add(pairRanks.get(0));
            tie.add(pairRanks.get(1));
            int kicker = getHighestRankExcluding(countByRank, pairRanks.get(0), pairRanks.get(1));
            tie.add(kicker);
            return new HandResult("Two Pair", 3, tie);
        }

        if (pairRanks.size() == 1) {
            List<Integer> tie = new ArrayList<>();
            int pRank = pairRanks.get(0);
            tie.add(pRank);
            for (Integer rank : sortedRanks) {
                if (rank != pRank) {
                    tie.add(rank);
                    if (tie.size() == 4) {
                        break;
                    }
                }
            }
            return new HandResult("Pair", 2, tie);
        }

        return new HandResult("High Card", 1, sortedRanks.subList(0, 5));
    }

    private static int getStraightHigh(List<Integer> ranks) {
        List<Integer> unique = new ArrayList<>(new TreeSet<>(ranks));
        if (unique.size() != 5) {
            return -1;
        }

        if (unique.contains(14) && unique.contains(2) && unique.contains(3) && unique.contains(4) && unique.contains(5)) {
            return 5;
        }

        Collections.sort(unique);
        if (unique.get(4) - unique.get(0) == 4) {
            boolean consecutive = true;
            for (int i = 1; i < unique.size(); i++) {
                if (unique.get(i) - unique.get(i - 1) != 1) {
                    consecutive = false;
                    break;
                }
            }
            if (consecutive) {
                return unique.get(4);
            }
        }

        return -1;
    }

    private static int getRankByCount(Map<Integer, Integer> countByRank, int targetCount) {
        for (Map.Entry<Integer, Integer> entry : countByRank.entrySet()) {
            if (entry.getValue() == targetCount) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private static List<Integer> getRanksByCount(Map<Integer, Integer> countByRank, int targetCount) {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countByRank.entrySet()) {
            if (entry.getValue() == targetCount) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private static int getHighestRankExcluding(Map<Integer, Integer> countByRank, int... excluded) {
        List<Integer> excludedList = new ArrayList<>();
        for (int value : excluded) {
            excludedList.add(value);
        }

        List<Integer> ranks = new ArrayList<>(countByRank.keySet());
        Collections.sort(ranks, Collections.reverseOrder());
        for (Integer rank : ranks) {
            if (!excludedList.contains(rank)) {
                return rank;
            }
        }
        return 0;
    }
}
