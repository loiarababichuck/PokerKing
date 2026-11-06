package com.example.pokerking;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int STARTING_CHIPS = 1000;
    private static final int BASE_BET = 25;
    private static final int PLAYER_COUNT = 4;
    private static final int CARD_WIDTH_DP = 46;
    private static final int CARD_HEIGHT_DP = 64;
    private static final int ACTION_PASS = 0;
    private static final int ACTION_BET = 1;
    private static final int ACTION_FOLD = 2;
    private static final int ACTION_ALL_IN = 3;

    // Fixed clockwise turn order: Bot 1 (1) -> Bot 3 (3) -> Você (0) -> Bot 2 (2)
    private static final int[] TURN_ORDER = {1, 3, 0, 2};

    private enum Stage {
        PREFLOP, FLOP, TURN, RIVER, SHOWDOWN
    }

    private static class PlayerViewHolder {
        LinearLayout panel;
        TextView nameView;
        TextView chipsView;
        TextView statusView;
        TextView actionView;
        ImageView[] cardBacks = new ImageView[2];
        TextView[] cardTexts = new TextView[2];
    }

    private Stage currentStage = Stage.PREFLOP;
    private final Player[] players = new Player[PLAYER_COUNT];
    private final List<Card> communityCards = new ArrayList<>();
    private Deck deck;
    private int pot = 0;
    private int currentMaxBet = 0;
    private boolean handActive = false;
    private boolean allInShowdownActive = false;
    private int currentTurnPlayer = 1;
    private int currentBlindPlayerIndex = 1;
    private int blindPosIndex = 0;
    private int actionsThisRound = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isBotPending = false;

    private LinearLayout gameScreen;
    private LinearLayout landingScreen;
    private TextView titleText;
    private TextView potText;
    private TextView turnText;
    private TextView statusText;
    private RelativeLayout tableArea;
    private Button newHandButton;
    private Button passButton;
    private Button betButton;
    private Button foldButton;
    private Button allInButton;
    private Button resetButton;
    private Button rankingButton;
    private Button startGameButton;

    private final List<PlayerViewHolder> playerViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupPlayers();
        setupListeners();
        resetTable();
        showLandingScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void bindViews() {
        gameScreen = findViewById(R.id.gameScreen);
        landingScreen = findViewById(R.id.landingScreen);
        titleText = findViewById(R.id.titleText);
        potText = findViewById(R.id.potText);
        turnText = findViewById(R.id.turnText);
        statusText = findViewById(R.id.statusText);
        tableArea = findViewById(R.id.tableArea);
        newHandButton = findViewById(R.id.newHandButton);
        passButton = findViewById(R.id.passButton);
        betButton = findViewById(R.id.betButton);
        foldButton = findViewById(R.id.foldButton);
        allInButton = findViewById(R.id.allInButton);
        resetButton = findViewById(R.id.resetButton);
        rankingButton = findViewById(R.id.rankingButton);
        startGameButton = findViewById(R.id.startGameButton);
    }

    private void setupPlayers() {
        players[0] = new Player("Você", STARTING_CHIPS);
        players[1] = new Player("Bot 1", STARTING_CHIPS);
        players[2] = new Player("Bot 2", STARTING_CHIPS);
        players[3] = new Player("Bot 3", STARTING_CHIPS);
    }

    private void setupListeners() {
        startGameButton.setOnClickListener(v -> startGame());
        newHandButton.setOnClickListener(v -> startNewHand());
        passButton.setOnClickListener(v -> processUserAction(ACTION_PASS));
        betButton.setOnClickListener(v -> processUserAction(ACTION_BET));
        foldButton.setOnClickListener(v -> processUserAction(ACTION_FOLD));
        allInButton.setOnClickListener(v -> processUserAction(ACTION_ALL_IN));
        resetButton.setOnClickListener(v -> resetTable());
        rankingButton.setOnClickListener(v -> showRankingDialog());
    }

    private void showLandingScreen() {
        landingScreen.setVisibility(View.VISIBLE);
        gameScreen.setVisibility(View.GONE);
    }

    private void startGame() {
        landingScreen.setVisibility(View.GONE);
        gameScreen.setVisibility(View.VISIBLE);
        resetTable();
    }

    private void showRankingDialog() {
        List<Player> sortedPlayers = new ArrayList<>();
        for (Player p : players) {
            if (p != null) {
                sortedPlayers.add(p);
            }
        }

        Collections.sort(sortedPlayers, (p1, p2) -> {
            if (p1.getChips() != p2.getChips()) {
                return Integer.compare(p2.getChips(), p1.getChips());
            }
            if (p1.isEliminated() != p2.isEliminated()) {
                return p1.isEliminated() ? 1 : -1;
            }
            return p1.getName().compareTo(p2.getName());
        });

        int totalChipsInGame = STARTING_CHIPS * PLAYER_COUNT;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            int rank = i + 1;
            double percent = (totalChipsInGame > 0) ? (p.getChips() * 100.0 / totalChipsInGame) : 0;

            String statusStr;
            if (p.isEliminated()) {
                statusStr = "❌ ELIMINADO";
            } else if (i == 0 && p.getChips() > 0) {
                statusStr = "🥇 LÍDER";
            } else {
                statusStr = "🎮 Em jogo";
            }

            sb.append(rank).append("º ");
            if (rank == 1 && p.getChips() > 0) sb.append("🏆 ");
            else if (rank == 2) sb.append("🥈 ");
            else if (rank == 3) sb.append("🥉 ");
            else sb.append("👤 ");

            sb.append(p.getName()).append("\n")
              .append("   Fichas: ").append(p.getChips())
              .append(" (").append(String.format(java.util.Locale.getDefault(), "%.1f", percent)).append("%)\n")
              .append("   Status: ").append(statusStr);

            if (i < sortedPlayers.size() - 1) {
                sb.append("\n\n");
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🏆 Classificação do Jogo")
                .setMessage(sb.toString())
                .setPositiveButton("Fechar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private int getNextPlayerIndex(int currentIndex) {
        for (int i = 0; i < TURN_ORDER.length; i++) {
            if (TURN_ORDER[i] == currentIndex) {
                return TURN_ORDER[(i + 1) % TURN_ORDER.length];
            }
        }
        return TURN_ORDER[0];
    }

    private int getNextActivePlayer(int currentIndex) {
        int next = getNextPlayerIndex(currentIndex);
        int count = 0;
        while ((players[next].isFolded() || players[next].isEliminated()) && count < PLAYER_COUNT) {
            next = getNextPlayerIndex(next);
            count++;
        }
        return next;
    }

    private void checkEliminatedPlayers() {
        for (Player p : players) {
            if (p != null && p.getChips() <= 0) {
                p.setEliminated(true);
            }
        }
    }

    private void startNewHand() {
        handler.removeCallbacksAndMessages(null);
        isBotPending = false;

        checkEliminatedPlayers();

        if (players[0].isEliminated()) {
            statusText.setText("FIM DE JOGO: Você foi ELIMINADO! Clique em Reset para reiniciar.");
            return;
        }

        int activeNonEliminated = 0;
        for (Player p : players) {
            if (!p.isEliminated()) {
                activeNonEliminated++;
            }
        }

        if (activeNonEliminated <= 1) {
            if (activeNonEliminated == 1 && !players[0].isEliminated()) {
                statusText.setText("PARABÉNS! Você é o CAMPEÃO do torneio! Clique em Reset para jogar novamente.");
            } else {
                statusText.setText("Não há jogadores suficientes para continuar. Resete a mesa.");
            }
            return;
        }

        // Rotate blind to next active player
        int attempts = 0;
        while (players[TURN_ORDER[blindPosIndex]].isEliminated() && attempts < TURN_ORDER.length) {
            blindPosIndex = (blindPosIndex + 1) % TURN_ORDER.length;
            attempts++;
        }
        currentBlindPlayerIndex = TURN_ORDER[blindPosIndex];
        blindPosIndex = (blindPosIndex + 1) % TURN_ORDER.length;

        deck = new Deck();
        communityCards.clear();
        pot = 0;
        currentMaxBet = 0;
        actionsThisRound = 0;
        handActive = true;
        allInShowdownActive = false;
        currentStage = Stage.PREFLOP;
        updateButtonStates();

        for (Player p : players) {
            p.resetForHand();
            if (p.isEliminated()) {
                p.setFolded(true);
            }
        }

        // Post Blind (25)
        Player blindPlayer = players[currentBlindPlayerIndex];
        int blindAmount = Math.min(blindPlayer.getChips(), BASE_BET);
        if (blindAmount > 0) {
            blindPlayer.betChips(blindAmount);
            currentMaxBet = blindAmount;
            blindPlayer.setLastAction("blind");
        }

        // Deal hole cards to active players
        for (int i = 0; i < PLAYER_COUNT; i++) {
            if (!players[i].isEliminated()) {
                Card first = deck.drawCard();
                Card second = deck.drawCard();
                if (first != null && second != null) {
                    players[i].receiveCards(first, second);
                }
            }
        }

        // First action in Pre-flop starts with the player to the left of the Blind
        currentTurnPlayer = getNextActivePlayer(currentBlindPlayerIndex);

        statusText.setText("Blind (" + blindAmount + ") do " + blindPlayer.getName() + ". Vez do " + players[currentTurnPlayer].getName() + ".");
        updateHeader();
        renderTable();
        processTurnLoop();
    }

    private void processTurnLoop() {
        if (!handActive || isBotPending) {
            return;
        }

        if (getActivePlayerCount() <= 1) {
            endHandByFold();
            return;
        }

        if (isBettingRoundComplete()) {
            if (getActivePlayersWithChipsCount() < 2) {
                allInShowdownActive = true;
            }
            isBotPending = true;
            updateHeader();
            updateButtonStates();
            renderTable();

            handler.postDelayed(() -> {
                if (!handActive) {
                    return;
                }
                advanceStage();
            }, 800);
            return;
        }

        Player current = players[currentTurnPlayer];
        if (current.isFolded() || current.isEliminated() || current.getChips() == 0) {
            currentTurnPlayer = getNextActivePlayer(currentTurnPlayer);
            processTurnLoop();
            return;
        }

        if (currentTurnPlayer == 0) {
            int toCall = Math.max(0, currentMaxBet - players[0].getRoundBet());
            if (toCall == 0) {
                statusText.setText("Sua vez! Mesa em checagem (passar).");
            } else {
                statusText.setText("Sua vez! Aposta atual: " + toCall + " fichas para pagar.");
            }
            updateHeader();
            updateButtonStates();
            renderTable();
            return;
        }

        isBotPending = true;
        statusText.setText("Vez do " + current.getName() + " (pensando...)");
        updateHeader();
        updateButtonStates();
        renderTable();

        handler.postDelayed(() -> {
            isBotPending = false;
            if (!handActive) {
                return;
            }

            Player bot = players[currentTurnPlayer];
            if (!bot.isFolded() && !bot.isEliminated() && bot.getChips() > 0) {
                int toCall = Math.max(0, currentMaxBet - bot.getRoundBet());
                int decision = chooseAiDecision(currentTurnPlayer, toCall);
                applyDecision(currentTurnPlayer, decision);
            }

            currentTurnPlayer = getNextActivePlayer(currentTurnPlayer);
            processTurnLoop();
        }, 800);
    }

    private void processUserAction(int action) {
        if (!handActive || currentTurnPlayer != 0 || players[0].isFolded() || players[0].isEliminated() || isBotPending) {
            statusText.setText("Não é a sua vez.");
            return;
        }

        applyDecision(0, action);
        currentTurnPlayer = getNextActivePlayer(0);

        processTurnLoop();
    }

    private int getActivePlayerCount() {
        int count = 0;
        for (Player p : players) {
            if (!p.isFolded() && !p.isEliminated()) {
                count++;
            }
        }
        return count;
    }

    private int getActivePlayersWithChipsCount() {
        int count = 0;
        for (Player p : players) {
            if (p != null && !p.isFolded() && !p.isEliminated() && p.getChips() > 0) {
                count++;
            }
        }
        return count;
    }

    private boolean isBettingRoundComplete() {
        int activeCount = 0;
        int matchedAndActedCount = 0;

        for (Player p : players) {
            if (!p.isFolded() && !p.isEliminated()) {
                activeCount++;
                if (p.getChips() == 0 || (p.getRoundBet() == currentMaxBet && p.hasActedInRound())) {
                    matchedAndActedCount++;
                }
            }
        }

        if (activeCount <= 1) {
            return true;
        }

        return matchedAndActedCount == activeCount;
    }

    private void endHandByFold() {
        PotCalculationResult calcResult = PotCalculator.calculatePots(Arrays.asList(players));
        for (Map.Entry<Player, Integer> entry : calcResult.getUncalledRefunds().entrySet()) {
            entry.getKey().addChips(entry.getValue());
        }
        List<PotEvaluator.PotAwardDetails> awards = PotEvaluator.awardPots(calcResult, communityCards);

        Player winner = null;
        for (Player p : players) {
            if (!p.isFolded() && !p.isEliminated()) {
                winner = p;
                break;
            }
        }
        int totalWon = 0;
        for (PotEvaluator.PotAwardDetails award : awards) {
            totalWon += award.getPot().getAmount();
        }

        if (winner != null) {
            statusText.setText("Todos desistiram. " + winner.getName() + " venceu " + totalWon + " fichas!");
        }
        handActive = false;
        currentStage = Stage.SHOWDOWN;
        checkEliminatedPlayers();
        checkGameEnd();
        updateHeader();
        updateButtonStates();
        renderTable();
    }

    private void advanceStage() {
        if (!handActive) {
            return;
        }

        if (currentStage == Stage.PREFLOP) {
            currentStage = Stage.FLOP;
            communityCards.add(deck.drawCard());
            communityCards.add(deck.drawCard());
            communityCards.add(deck.drawCard());
            resetStageBets();
            statusText.setText("Flop revelado. Vez do " + players[currentTurnPlayer].getName() + ".");
        } else if (currentStage == Stage.FLOP) {
            currentStage = Stage.TURN;
            communityCards.add(deck.drawCard());
            resetStageBets();
            statusText.setText("Turn revelado. Vez do " + players[currentTurnPlayer].getName() + ".");
        } else if (currentStage == Stage.TURN) {
            currentStage = Stage.RIVER;
            communityCards.add(deck.drawCard());
            resetStageBets();
            statusText.setText("River revelado. Vez do " + players[currentTurnPlayer].getName() + ".");
        } else if (currentStage == Stage.RIVER) {
            showdown();
            return;
        }

        updateHeader();
        renderTable();

        if (getActivePlayersWithChipsCount() < 2) {
            allInShowdownActive = true;
            isBotPending = true;
            updateButtonStates();
            handler.postDelayed(() -> {
                if (handActive) {
                    advanceStage();
                }
            }, 800);
        } else {
            isBotPending = true;
            updateButtonStates();
            handler.postDelayed(() -> {
                isBotPending = false;
                if (handActive) {
                    processTurnLoop();
                }
            }, 800);
        }
    }

    private void resetStageBets() {
        currentMaxBet = 0;
        actionsThisRound = 0;
        for (Player player : players) {
            player.resetStageBets();
        }
        currentTurnPlayer = getNextActivePlayer(currentBlindPlayerIndex);
    }

    private int calculateAiHandStrength(Player player) {
        List<Card> holeCards = player.getHoleCards();
        if (holeCards == null || holeCards.size() < 2) {
            return 0;
        }

        if (communityCards.isEmpty()) {
            Card c1 = holeCards.get(0);
            Card c2 = holeCards.get(1);
            int v1 = c1.getValue();
            int v2 = c2.getValue();

            if (v1 == v2) {
                if (v1 >= 11) return 10;
                if (v1 >= 8) return 8;
                return 6;
            }

            boolean suited = c1.getSuit() == c2.getSuit();
            int high = Math.max(v1, v2);
            int low = Math.min(v1, v2);

            if (high == 14 && low >= 10) return 8;
            if (high >= 12 && low >= 10) return 7;
            if (suited && (high - low == 1 || high - low == 2)) return 6;
            if (high >= 12) return 5;
            if (high >= 10) return 4;

            return 2;
        } else {
            List<Card> combo = new ArrayList<>(holeCards);
            combo.addAll(communityCards);
            HandEvaluator.HandResult result = HandEvaluator.evaluateBestSevenCardHandResult(combo);
            return result.getCategory();
        }
    }

    private int chooseAiDecision(int playerIndex, int toCall) {
        Player player = players[playerIndex];
        if (player.getChips() <= 0) {
            return ACTION_PASS;
        }

        int handStrength = calculateAiHandStrength(player);

        if (toCall == 0) {
            if (handStrength >= 7) {
                return Math.random() < 0.6 ? ACTION_BET : ACTION_PASS;
            } else if (handStrength >= 4) {
                return Math.random() < 0.25 ? ACTION_BET : ACTION_PASS;
            } else {
                return ACTION_PASS;
            }
        } else {
            boolean isAllIn = (toCall >= player.getChips()) || (toCall >= STARTING_CHIPS / 2);

            if (handStrength >= 7) {
                if (Math.random() < 0.3 && player.getChips() > toCall) {
                    return ACTION_ALL_IN;
                }
                return ACTION_PASS;
            } else if (handStrength >= 4) {
                if (isAllIn) {
                    return Math.random() < 0.60 ? ACTION_PASS : ACTION_FOLD;
                } else {
                    return Math.random() < 0.85 ? ACTION_PASS : ACTION_FOLD;
                }
            } else if (handStrength >= 2) {
                if (isAllIn) {
                    return Math.random() < 0.05 ? ACTION_PASS : ACTION_FOLD;
                } else if (toCall <= BASE_BET * 2) {
                    return Math.random() < 0.30 ? ACTION_PASS : ACTION_FOLD;
                } else {
                    return Math.random() < 0.05 ? ACTION_PASS : ACTION_FOLD;
                }
            } else {
                return Math.random() < 0.05 ? ACTION_PASS : ACTION_FOLD;
            }
        }
    }

    private void applyDecision(int playerIndex, int action) {
        Player player = players[playerIndex];
        if (player.isFolded() || player.isEliminated() || !handActive) {
            return;
        }

        player.setHasActedInRound(true);

        int toCall = Math.max(0, currentMaxBet - player.getRoundBet());

        if (action == ACTION_FOLD) {
            player.setFolded(true);
            player.setLastAction("fold");
            statusText.setText(player.getName() + " desistiu.");
            actionsThisRound++;
            return;
        }

        if (action == ACTION_ALL_IN) {
            int amount = player.getChips();
            if (amount > 0) {
                player.betChips(amount);
                if (player.getRoundBet() > currentMaxBet) {
                    currentMaxBet = player.getRoundBet();
                    for (Player p : players) {
                        if (p != player && !p.isFolded() && !p.isEliminated() && p.getChips() > 0) {
                            p.setHasActedInRound(false);
                        }
                    }
                }
                player.setLastAction("all-in");
                statusText.setText(player.getName() + " deu ALL-IN com " + amount + " fichas!");
            } else {
                player.setLastAction("pass");
                statusText.setText(player.getName() + " passou.");
            }
        } else if (action == ACTION_PASS) {
            if (toCall == 0) {
                player.setLastAction("pass");
                statusText.setText(player.getName() + " passou.");
            } else {
                int amount = Math.min(player.getChips(), toCall);
                player.betChips(amount);
                if (player.getChips() == 0) {
                    player.setLastAction("all-in");
                    statusText.setText(player.getName() + " deu ALL-IN pagando " + amount + " fichas!");
                } else {
                    player.setLastAction("call");
                    statusText.setText(player.getName() + " pagou " + amount + " fichas.");
                }
            }
        } else if (action == ACTION_BET) {
            int raiseTarget = (currentMaxBet == 0) ? BASE_BET : currentMaxBet + BASE_BET;
            int needed = raiseTarget - player.getRoundBet();
            int amount = Math.min(player.getChips(), needed);

            player.betChips(amount);

            if (player.getRoundBet() > currentMaxBet) {
                boolean isBet = (currentMaxBet == 0);
                currentMaxBet = player.getRoundBet();
                player.setLastAction(isBet ? "bet" : "raise");
                statusText.setText(player.getName() + (isBet ? " apostou " : " aumentou para ") + currentMaxBet + " fichas.");

                for (Player p : players) {
                    if (p != player && !p.isFolded() && !p.isEliminated() && p.getChips() > 0) {
                        p.setHasActedInRound(false);
                    }
                }
            } else {
                if (player.getChips() == 0) {
                    player.setLastAction("all-in");
                    statusText.setText(player.getName() + " deu ALL-IN (" + amount + " fichas).");
                } else {
                    player.setLastAction("call");
                    statusText.setText(player.getName() + " pagou tudo (" + amount + " fichas).");
                }
            }
        }

        actionsThisRound++;
    }

    private void showdown() {
        if (!handActive) {
            return;
        }

        while (communityCards.size() < 5 && deck != null && !deck.isEmpty()) {
            Card card = deck.drawCard();
            if (card != null) {
                communityCards.add(card);
            }
        }

        PotCalculationResult calcResult = PotCalculator.calculatePots(Arrays.asList(players));

        for (Map.Entry<Player, Integer> entry : calcResult.getUncalledRefunds().entrySet()) {
            entry.getKey().addChips(entry.getValue());
        }

        List<PotEvaluator.PotAwardDetails> awards = PotEvaluator.awardPots(calcResult, communityCards);

        StringBuilder sb = new StringBuilder();

        boolean sameWinners = !awards.isEmpty();
        if (sameWinners) {
            List<Player> firstWinners = awards.get(0).getWinners();
            for (int i = 1; i < awards.size(); i++) {
                List<Player> currentWinners = awards.get(i).getWinners();
                if (firstWinners.size() != currentWinners.size() || !firstWinners.containsAll(currentWinners)) {
                    sameWinners = false;
                    break;
                }
            }
        }

        if (sameWinners) {
            int totalAmount = 0;
            HandEvaluator.HandResult bestHand = null;
            for (PotEvaluator.PotAwardDetails award : awards) {
                totalAmount += award.getPot().getAmount();
                if (bestHand == null || (award.getBestHand() != null && HandEvaluator.compareHandResults(award.getBestHand(), bestHand) > 0)) {
                    bestHand = award.getBestHand();
                }
            }

            List<Player> winners = awards.get(0).getWinners();
            String winnersStr;
            if (winners.size() == 1) {
                winnersStr = winners.get(0).getName();
            } else {
                List<String> names = new ArrayList<>();
                for (Player w : winners) {
                    names.add(w.getName());
                }
                winnersStr = "Empate (" + String.join(", ", names) + ")";
            }

            sb.append("Pote (").append(totalAmount).append(" fichas): ").append(winnersStr);
            if (bestHand != null) {
                sb.append(" com ").append(bestHand.getName());
            }
        } else {
            for (PotEvaluator.PotAwardDetails award : awards) {
                String potLabel = award.getPotName();
                List<Player> winners = award.getWinners();
                String winnersStr;
                if (winners.size() == 1) {
                    winnersStr = winners.get(0).getName();
                } else {
                    List<String> names = new ArrayList<>();
                    for (Player w : winners) {
                        names.add(w.getName());
                    }
                    winnersStr = "Empate (" + String.join(", ", names) + ")";
                }

                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(potLabel).append(" (").append(award.getPot().getAmount()).append(" fichas): ").append(winnersStr);
                if (award.getBestHand() != null) {
                    sb.append(" com ").append(award.getBestHand().getName());
                }
            }
        }

        statusText.setText(sb.toString());

        isBotPending = false;
        handActive = false;
        currentStage = Stage.SHOWDOWN;
        checkEliminatedPlayers();
        checkGameEnd();
        updateHeader();
        updateButtonStates();
        renderTable();
    }

    private void checkGameEnd() {
        String currentStatus = statusText.getText() != null ? statusText.getText().toString() : "";

        if (players[0].isEliminated()) {
            String elimMsg = "FIM DE JOGO: Você foi ELIMINADO! Clique em Reset para reiniciar.";
            if (!currentStatus.isEmpty() && !currentStatus.startsWith("FIM DE JOGO")) {
                statusText.setText(currentStatus + "\n" + elimMsg);
            } else {
                statusText.setText(elimMsg);
            }
            return;
        }

        int activeCount = 0;
        for (Player p : players) {
            if (!p.isEliminated()) {
                activeCount++;
            }
        }

        if (activeCount == 1 && !players[0].isEliminated()) {
            String champMsg = "PARABÉNS! Você é o CAMPEÃO do torneio! Clique em Reset para jogar novamente.";
            if (!currentStatus.isEmpty() && !currentStatus.startsWith("PARABÉNS")) {
                statusText.setText(currentStatus + "\n" + champMsg);
            } else {
                statusText.setText(champMsg);
            }
        }
    }

    private void resetTable() {
        handler.removeCallbacksAndMessages(null);
        isBotPending = false;
        handActive = false;
        allInShowdownActive = false;
        pot = 0;
        currentMaxBet = 0;
        actionsThisRound = 0;
        currentStage = Stage.PREFLOP;
        currentBlindPlayerIndex = 1;
        blindPosIndex = 0; // Starts with Bot 1
        communityCards.clear();

        for (int i = 0; i < PLAYER_COUNT; i++) {
            players[i] = new Player(i == 0 ? "Você" : "Bot " + (i == 1 ? "1" : i == 2 ? "2" : "3"), STARTING_CHIPS);
        }

        statusText.setText("Mesa reiniciada. Inicie uma nova mão.");
        updateHeader();
        updateButtonStates();
        renderTable();
    }

    private void updateHeader() {
        int currentTotalPot = 0;
        for (Player p : players) {
            if (p != null) {
                currentTotalPot += p.getTotalHandContribution();
            }
        }
        potText.setText("Pote: " + currentTotalPot + " fichas");
        turnText.setText(getStageLabel(currentStage));
    }

    private void updateButtonStates() {
        newHandButton.setEnabled(!handActive);

        boolean isUserTurn = handActive && currentTurnPlayer == 0 && !players[0].isFolded() && !players[0].isEliminated() && !isBotPending;

        passButton.setEnabled(isUserTurn);
        betButton.setEnabled(isUserTurn);
        foldButton.setEnabled(isUserTurn);
        allInButton.setEnabled(isUserTurn && players[0].getChips() > 0);

        if (isUserTurn) {
            int toCall = Math.max(0, currentMaxBet - players[0].getRoundBet());
            if (toCall == 0) {
                passButton.setText("Passar");
                betButton.setText("Apostar (" + BASE_BET + ")");
            } else {
                passButton.setText("Pagar (" + toCall + ")");
                betButton.setText("Aumentar (" + (toCall + BASE_BET) + ")");
            }
            foldButton.setText("Desistir");
            allInButton.setText("All In (" + players[0].getChips() + ")");
        } else {
            passButton.setText("Passar");
            betButton.setText("Apostar");
            foldButton.setText("Desistir");
            allInButton.setText("All In");
        }
    }

    private String getStageLabel(Stage stage) {
        if (stage == Stage.PREFLOP) return "Pré-flop";
        if (stage == Stage.FLOP) return "Flop";
        if (stage == Stage.TURN) return "Turn";
        if (stage == Stage.RIVER) return "River";
        return "Showdown";
    }

    private void renderTable() {
        tableArea.removeAllViews();
        playerViews.clear();

        createCommunityCards();

        for (int i = 0; i < PLAYER_COUNT; i++) {
            PlayerViewHolder holder = createPlayerPanel(i);
            playerViews.add(holder);
            tableArea.addView(holder.panel, buildPlayerLayoutParams(i));
        }
    }

    private void createCommunityCards() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        for (int i = 0; i < 5; i++) {
            FrameLayout cardSlot = new FrameLayout(this);
            cardSlot.setClipChildren(true);
            cardSlot.setClipToPadding(true);
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(dpToPx(CARD_WIDTH_DP), dpToPx(CARD_HEIGHT_DP));
            if (i < 4) {
                slotParams.setMarginEnd(dpToPx(10));
            }
            cardSlot.setLayoutParams(slotParams);

            ImageView cardImage = new ImageView(this);
            cardImage.setImageResource(R.drawable.cartas01);
            cardImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            cardImage.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            cardImage.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            TextView cardView = new TextView(this);
            cardView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            cardView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            cardView.setGravity(Gravity.CENTER);
            cardView.setIncludeFontPadding(false);
            cardView.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            cardView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            if (i < communityCards.size()) {
                Card card = communityCards.get(i);
                cardView.setText(card.getDisplayLabel());
                cardView.setBackgroundResource(R.drawable.card_white_bg);
                cardView.setTextColor(card.isRed() ? Color.parseColor("#D32F2F") : Color.parseColor("#111111"));
                cardView.setShadowLayer(0, 0, 0, 0);
                cardView.setVisibility(View.VISIBLE);
                cardImage.setVisibility(View.GONE);
            } else {
                cardImage.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.GONE);
            }

            cardSlot.addView(cardImage);
            cardSlot.addView(cardView);
            row.addView(cardSlot);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin = dpToPx(180);
        params.leftMargin = dpToPx(12);
        params.rightMargin = dpToPx(12);
        tableArea.addView(row, params);
    }

    private PlayerViewHolder createPlayerPanel(int playerIndex) {
        Player player = players[playerIndex];
        PlayerViewHolder holder = new PlayerViewHolder();

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6));
        panel.setBackgroundColor(Color.TRANSPARENT);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);

        boolean isTurn = handActive && !isBettingRoundComplete() && playerIndex == currentTurnPlayer && !player.isFolded() && !player.isEliminated();

        TextView nameView = new TextView(this);
        nameView.setText(player.getName());
        nameView.setTextColor(isTurn ? Color.parseColor("#FFD84D") : Color.WHITE);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        nameView.setGravity(Gravity.CENTER);
        nameView.setShadowLayer(3f, 1f, 1f, Color.BLACK);

        TextView chipsView = new TextView(this);
        chipsView.setText("Fichas: " + player.getChips());
        chipsView.setTextColor(Color.parseColor("#FFD84D"));
        chipsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chipsView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        chipsView.setShadowLayer(3f, 1f, 1f, Color.BLACK);

        TextView statusView = new TextView(this);
        if (player.isEliminated()) {
            statusView.setText("ELIMINADO");
            statusView.setTextColor(Color.parseColor("#9E9E9E"));
        } else if (player.isFolded()) {
            statusView.setText("Fold");
            statusView.setTextColor(Color.parseColor("#FF5252"));
        } else if (isTurn) {
            statusView.setText(playerIndex == 0 ? "SUA VEZ" : "Pensando...");
            statusView.setTextColor(Color.parseColor("#FFD84D"));
        } else {
            statusView.setText("Ativo");
            statusView.setTextColor(Color.parseColor("#CCCCCC"));
        }
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        statusView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusView.setShadowLayer(3f, 1f, 1f, Color.BLACK);

        LinearLayout cardsRow = new LinearLayout(this);
        cardsRow.setOrientation(LinearLayout.HORIZONTAL);
        cardsRow.setGravity(Gravity.CENTER);

        for (int i = 0; i < 2; i++) {
            FrameLayout cardSlot = new FrameLayout(this);
            cardSlot.setClipChildren(true);
            cardSlot.setClipToPadding(true);
            LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(dpToPx(CARD_WIDTH_DP), dpToPx(CARD_HEIGHT_DP));
            if (i == 0) {
                slotParams.setMarginEnd(dpToPx(4));
            }
            cardSlot.setLayoutParams(slotParams);

            ImageView backView = new ImageView(this);
            backView.setImageResource(R.drawable.cartas01);
            backView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            backView.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            backView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            backView.setVisibility(View.VISIBLE);

            TextView cardText = new TextView(this);
            cardText.setGravity(Gravity.CENTER);
            cardText.setIncludeFontPadding(false);
            cardText.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            cardText.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            cardText.setVisibility(View.GONE);

            if (currentStage == Stage.SHOWDOWN) {
                if (!player.getHoleCards().isEmpty() && i < player.getHoleCards().size()) {
                    Card card = player.getHoleCards().get(i);
                    cardText.setText(card.getDisplayLabel());
                    if (player.isFolded()) {
                        cardText.setBackgroundResource(R.drawable.card_folded_bg);
                        cardText.setTextColor(Color.parseColor("#757575"));
                    } else {
                        cardText.setBackgroundResource(R.drawable.card_white_bg);
                        cardText.setTextColor(card.isRed() ? Color.parseColor("#D32F2F") : Color.parseColor("#111111"));
                    }
                    cardText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    cardText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                    cardText.setShadowLayer(0, 0, 0, 0);
                    cardText.setVisibility(View.VISIBLE);
                    backView.setVisibility(View.GONE);
                } else {
                    cardText.setVisibility(View.GONE);
                    backView.setVisibility(View.GONE);
                }
            } else {
                boolean revealHoleCards = (handActive && allInShowdownActive && !player.isFolded() && !player.isEliminated())
                        || (handActive && isBettingRoundComplete() && getActivePlayersWithChipsCount() < 2 && !player.isFolded() && !player.isEliminated());
                if (player.isEliminated()) {
                    cardText.setVisibility(View.GONE);
                    backView.setVisibility(View.GONE);
                } else if (player.isFolded()) {
                    cardText.setText("Fold");
                    cardText.setBackgroundResource(R.drawable.card_folded_bg);
                    cardText.setTextColor(Color.parseColor("#757575"));
                    cardText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    cardText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                    cardText.setShadowLayer(0, 0, 0, 0);
                    cardText.setVisibility(View.VISIBLE);
                    backView.setVisibility(View.GONE);
                } else if (playerIndex == 0 || revealHoleCards) {
                    if (handActive && !player.getHoleCards().isEmpty() && i < player.getHoleCards().size()) {
                        Card card = player.getHoleCards().get(i);
                        cardText.setText(card.getDisplayLabel());
                        cardText.setBackgroundResource(R.drawable.card_white_bg);
                        cardText.setTextColor(card.isRed() ? Color.parseColor("#D32F2F") : Color.parseColor("#111111"));
                        cardText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        cardText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
                        cardText.setShadowLayer(0, 0, 0, 0);
                        cardText.setVisibility(View.VISIBLE);
                        backView.setVisibility(View.GONE);
                    } else {
                        cardText.setVisibility(View.GONE);
                        backView.setVisibility(View.VISIBLE);
                    }
                } else {
                    cardText.setVisibility(View.GONE);
                    backView.setVisibility(View.VISIBLE);
                }
            }

            cardSlot.addView(backView);
            cardSlot.addView(cardText);
            cardsRow.addView(cardSlot);

            holder.cardBacks[i] = backView;
            holder.cardTexts[i] = cardText;
        }

        TextView actionView = new TextView(this);
        String lastAction = player.getLastAction();
        if (!lastAction.isEmpty()) {
            actionView.setText(lastAction.toUpperCase());
            if ("pass".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#81C784"));
            } else if ("call".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#64B5F6"));
            } else if ("bet".equalsIgnoreCase(lastAction) || "raise".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#FFD54F"));
            } else if ("all-in".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#FF9800"));
            } else if ("fold".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#E57373"));
            } else if ("blind".equalsIgnoreCase(lastAction)) {
                actionView.setTextColor(Color.parseColor("#BA68C8"));
            } else {
                actionView.setTextColor(Color.WHITE);
            }
        } else {
            actionView.setText("");
        }
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        actionView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        actionView.setGravity(Gravity.CENTER);
        actionView.setPadding(0, dpToPx(2), 0, 0);

        panel.addView(nameView);
        panel.addView(chipsView);
        panel.addView(statusView);
        panel.addView(cardsRow);
        panel.addView(actionView);

        holder.panel = panel;
        holder.nameView = nameView;
        holder.chipsView = chipsView;
        holder.statusView = statusView;
        holder.actionView = actionView;

        return holder;
    }

    private RelativeLayout.LayoutParams buildPlayerLayoutParams(int playerIndex) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if (playerIndex == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.bottomMargin = dpToPx(24);
        } else if (playerIndex == 1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.topMargin = dpToPx(24);
        } else if (playerIndex == 2) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.leftMargin = dpToPx(18);
            params.topMargin = dpToPx(26);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.rightMargin = dpToPx(18);
            params.topMargin = dpToPx(26);
        }

        return params;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
