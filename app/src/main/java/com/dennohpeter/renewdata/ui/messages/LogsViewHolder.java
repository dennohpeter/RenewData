package com.dennohpeter.renewdata.ui.messages;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dennohpeter.renewdata.R;

public class LogsViewHolder extends RecyclerView.ViewHolder {
    TextView received_date, message_body;


    public LogsViewHolder(View itemView) {
        super(itemView);
        received_date = itemView.findViewById(R.id.received_date);
        message_body = itemView.findViewById(R.id.message_body);

    }
}
