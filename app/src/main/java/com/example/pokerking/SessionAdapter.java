package com.example.pokerking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
    public interface OnSessionActionListener {
        void onDelete(Session session, int position);
    }

    private final List<Session> sessions;
    private final OnSessionActionListener listener;

    public SessionAdapter(List<Session> sessions, OnSessionActionListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessions.get(position);
        holder.sessionDate.setText(session.getDate());
        holder.sessionPlayer.setText(session.getPlayerName());
        holder.sessionValues.setText(
                holder.itemView.getContext().getString(
                        R.string.session_summary,
                        session.formatCurrency(session.getBuyIn()),
                        session.formatCurrency(session.getCashOut())
                )
        );

        double profit = session.getProfit();
        String formatedProfit = session.formatCurrency(profit);
        holder.sessionProfit.setText(formatedProfit);
        holder.sessionProfit.setTextColor(
                profit >= 0
                        ? holder.itemView.getContext().getColor(R.color.green_gain)
                        : holder.itemView.getContext().getColor(R.color.red_loss)
        );

        if (session.getNotes() == null || session.getNotes().trim().isEmpty()) {
            holder.sessionNotes.setVisibility(View.GONE);
        } else {
            holder.sessionNotes.setVisibility(View.VISIBLE);
            holder.sessionNotes.setText(session.getNotes());
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(session, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView sessionDate;
        TextView sessionPlayer;
        TextView sessionProfit;
        TextView sessionValues;
        TextView sessionNotes;
        Button deleteButton;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionDate = itemView.findViewById(R.id.sessionDate);
            sessionPlayer = itemView.findViewById(R.id.sessionPlayer);
            sessionProfit = itemView.findViewById(R.id.sessionProfit);
            sessionValues = itemView.findViewById(R.id.sessionValues);
            sessionNotes = itemView.findViewById(R.id.sessionNotes);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
