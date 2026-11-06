package com.example.pokerking;

import java.util.ArrayList;
import java.util.List;

public class Pot {
    private final int amount;
    private final List<Player> eligiblePlayers;

    public Pot(int amount, List<Player> eligiblePlayers) {
        this.amount = amount;
        this.eligiblePlayers = new ArrayList<>(eligiblePlayers);
    }

    public int getAmount() {
        return amount;
    }

    public List<Player> getEligiblePlayers() {
        return new ArrayList<>(eligiblePlayers);
    }

    public boolean isEligible(Player player) {
        return eligiblePlayers.contains(player);
    }
}
