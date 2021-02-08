package com.dmatsanganis.hardaliapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dmatsanganis.hardaliapp.Database.DatabaseConfiguration;
import com.dmatsanganis.hardaliapp.Database.DatabaseModel;
import com.dmatsanganis.hardaliapp.R;
import com.dmatsanganis.hardaliapp.SendSMS;

import java.util.List;

public class ViewEditedMessagesAdapter extends  RecyclerView.Adapter<ViewEditedMessagesAdapter.ViewHolder>{

    List<DatabaseModel> messages ;
    Context context;
    DatabaseConfiguration databaseConfiguration;

    public ViewEditedMessagesAdapter(List<DatabaseModel> messages, Context context) {
        this.messages = messages;
        this.context = context;
        databaseConfiguration = new DatabaseConfiguration(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.view_edited_message_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  DatabaseModel databaseModel = messages.get(position);
        holder.textView.setText(databaseModel.getId());
        holder.editText.setText(databaseModel.getReason());
        holder.editText2.setText(databaseModel.getData());
        holder.button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to SendSMS, in order to send the selected sms,
                // which will be provided via putExtra.
                Intent intent = new Intent(context, SendSMS.class);
                intent.putExtra("codeID", databaseModel.getId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public  class ViewHolder extends  RecyclerView.ViewHolder{
        EditText editText, editText2;
        TextView textView;
        Button button_send;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_title);
            editText = itemView.findViewById(R.id.viewedittext_name);
            editText2 = itemView.findViewById(R.id.viewedittext_data2);
            button_send = itemView.findViewById(R.id.button_send);
        }
    }


}
