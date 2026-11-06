package com.example.pokerking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotCalculationResult {
    private final List<Pot> pots;
    private final Map<Player, Integer> uncalledRefunds;

    public PotCalculationResult(List<Pot> pots, Map<Player, Integer> uncalledRefunds) {
        this.pots = new ArrayList<>(pots);
        this.uncalledRefunds = new HashMap<>(uncalledRefunds);
    }

    public List<Pot> getPots() {
        return new ArrayList<>(pots);
    }

    public Map<Player, Integer> getUncalledRefunds() {
        return new HashMap<>(uncalledRefunds);
    }

    public int getTotalPotAmount() {
        int total = 0;
        for (Pot p : pots) {
            total += p.getAmount();
        }
        return total;
    }

    public int getTotalRefundAmount() {
        int total = 0;
        for (int amount : uncalledRefunds.values()) {
            total += amount;
        }
        return total;
    }
}
