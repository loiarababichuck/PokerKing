package com.example.pokerking;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Session {
    private final String id;
    private final String playerName;
    private final String date;
    private final double buyIn;
    private final double cashOut;
    private final String notes;

    public Session(String id, String playerName, String date, double buyIn, double cashOut, String notes) {
        this.id = id;
        this.playerName = playerName;
        this.date = date;
        this.buyIn = buyIn;
        this.cashOut = cashOut;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDate() {
        return date;
    }

    public double getBuyIn() {
        return buyIn;
    }

    public double getCashOut() {
        return cashOut;
    }

    public String getNotes() {
        return notes;
    }

    public double getProfit() {
        return cashOut - buyIn;
    }

    public boolean isPositiveResult() {
        return getProfit() > 0;
    }

    public String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return "R$ " + formatter.format(value);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("playerName", playerName);
            json.put("date", date);
            json.put("buyIn", buyIn);
            json.put("cashOut", cashOut);
            json.put("notes", notes);
        } catch (JSONException e) {
            return new JSONObject();
        }
        return json;
    }

    public static Session fromJson(JSONObject json) {
        return new Session(
                json.optString("id", ""),
                json.optString("playerName", "Jogador"),
                json.optString("date", "--/--/--"),
                json.optDouble("buyIn", 0.0),
                json.optDouble("cashOut", 0.0),
                json.optString("notes", "")
        );
    }
}
