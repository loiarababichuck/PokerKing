package com.example.pokerking;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int chips;
    private int roundBet;
    private int totalHandContribution;
    private boolean folded;
    private boolean eliminated;
    private boolean hasActedInRound;
    private String lastAction = "";
    private final List<Card> holeCards = new ArrayList<>();

    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public void addChips(int amount) {
        chips += amount;
    }

    public void removeChips(int amount) {
        chips = Math.max(0, chips - amount);
    }

    public int betChips(int amount) {
        int actual = Math.min(chips, amount);
        chips -= actual;
        roundBet += actual;
        totalHandContribution += actual;
        return actual;
    }

    public int getRoundBet() {
        return roundBet;
    }

    public void setRoundBet(int roundBet) {
        this.roundBet = roundBet;
    }

    public void addRoundBet(int amount) {
        roundBet += amount;
    }

    public int getTotalHandContribution() {
        return totalHandContribution;
    }

    public void setTotalHandContribution(int totalHandContribution) {
        this.totalHandContribution = totalHandContribution;
    }

    public void addTotalHandContribution(int amount) {
        this.totalHandContribution += amount;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public boolean isAllIn() {
        return !folded && !eliminated && chips == 0 && totalHandContribution > 0;
    }

    public boolean isActive() {
        return !folded && !eliminated && chips > 0;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public boolean hasActedInRound() {
        return hasActedInRound;
    }

    public void setHasActedInRound(boolean hasActedInRound) {
        this.hasActedInRound = hasActedInRound;
    }

    public void resetForHand() {
        folded = false;
        lastAction = "";
        roundBet = 0;
        totalHandContribution = 0;
        hasActedInRound = false;
        holeCards.clear();
    }

    public void resetStageBets() {
        roundBet = 0;
        lastAction = "";
        hasActedInRound = false;
    }

    public void receiveCards(Card first, Card second) {
        holeCards.clear();
        holeCards.add(first);
        holeCards.add(second);
    }

    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }
}
