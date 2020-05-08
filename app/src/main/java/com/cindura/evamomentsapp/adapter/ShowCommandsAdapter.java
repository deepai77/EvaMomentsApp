package com.cindura.evamomentsapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cindura.evamomentsapp.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

//Class to show the Commands list when "Show Commands" is invoked
public class ShowCommandsAdapter extends RecyclerView.Adapter<ShowCommandsAdapter.CustomViewHolder> {
    List<String> voiceCommandsList,evaResponseList;
    Context context;

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView voiceCommand;
        TextView evaResponse;

        public CustomViewHolder(View itemView) {
            super(itemView);
            voiceCommand = (TextView) itemView.findViewById(R.id.voiceCommand);
            evaResponse=(TextView) itemView.findViewById(R.id.evaResponse);
        }
    }

    public ShowCommandsAdapter(List<String> voiceCommandsList, List<String> evaResponseList, Context context) {
        this.voiceCommandsList = voiceCommandsList;
        this.evaResponseList=evaResponseList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.show_commands_item;
    }

    @Override
    public int getItemCount() {
        return evaResponseList.size();
    }

    @Override
    public ShowCommandsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShowCommandsAdapter.CustomViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(ShowCommandsAdapter.CustomViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        holder.voiceCommand.setText(voiceCommandsList.get(position));
        holder.evaResponse.setText(evaResponseList.get(position));

        if(position==0){
            holder.evaResponse.setTypeface(null, Typeface.BOLD);
        }
    }
}