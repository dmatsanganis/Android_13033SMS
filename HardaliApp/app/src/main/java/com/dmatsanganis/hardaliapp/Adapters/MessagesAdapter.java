package com.dmatsanganis.hardaliapp.Adapters;

import android.app.Activity;
import android.content.Context;
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

import java.text.BreakIterator;
import java.util.List;

public class MessagesAdapter extends  RecyclerView.Adapter<MessagesAdapter.ViewHolder>{

    List<DatabaseModel> messages ;
    Context context;
    DatabaseConfiguration databaseConfiguration;

    public MessagesAdapter(List<DatabaseModel> messages, Context context) {
        this.messages = messages;
        this.context = context;
        databaseConfiguration = new DatabaseConfiguration(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.message_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  DatabaseModel databaseModel = messages.get(position);
        holder.textView.setText(databaseModel.getId());
        holder.editText.setText(databaseModel.getReason());
        holder.editText2.setText(databaseModel.getData());
        holder.button_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = holder.textView.getText().toString();
                String reason = holder.editText.getText().toString();
                String data = holder.editText2.getText().toString();
                databaseConfiguration.updatemessage(new DatabaseModel(id,reason,data));
                notifyDataSetChanged();
                ((Activity)context).finish();
                context.startActivity(((Activity) context).getIntent());
            }
        });
        holder.button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseConfiguration.deletemessage(databaseModel.getId());
                messages.remove(position);
                notifyDataSetChanged();
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
        Button button_Edit, button_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_title);
            editText = itemView.findViewById(R.id.edittext_name);
            editText2 = itemView.findViewById(R.id.edittext_data2);
            button_Edit = itemView.findViewById(R.id.button_edit);
            button_delete = itemView.findViewById(R.id.button_delete);

        }
    }


}
