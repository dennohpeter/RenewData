package com.dennohpeter.renewdata.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dennohpeter.renewdata.MessageModel;
import com.dennohpeter.renewdata.R;

import java.util.ArrayList;

public class LogsAdapter extends RecyclerView.Adapter<LogsViewHolder> {
    private ArrayList<MessageModel> messages;

    LogsAdapter() {
        this.messages = new ArrayList<>();
    }

    void add(MessageModel message) {
        messages.add(message);
    }

    int size() {
        return messages.size();
    }

    @NonNull
    @Override
    public LogsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_card, parent, false);
        return new LogsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LogsViewHolder holder, int position) {
        holder.received_date.setText(messages.get(position).getDate());
        holder.message_body.setText(messages.get(position).getMessage());

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

}
