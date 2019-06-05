package com.projects.jsheshashankar.stepupapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter" ;
    private List<String> participantNames = new ArrayList<String>();
    private List<String> hobbiesList = new ArrayList<String>();
    private Context mContext;

    public RecyclerViewAdapter() {
    }

    public RecyclerViewAdapter(List<String> participantNames, List<String> hobbiesList, Context mContext) {
        this.participantNames = participantNames;
        this.hobbiesList = hobbiesList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_team_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.participantNameView.setText(participantNames.get(position));
        holder.hobbiesView.setText(hobbiesList.get(position));

    }

    @Override
    public int getItemCount() {
        return participantNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView participantNameView;
        TextView hobbiesView;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            participantNameView  = (TextView) itemView.findViewById(R.id.teamParticipantNameId);
            hobbiesView = (TextView) itemView.findViewById(R.id.hobbiesId);
            parentLayout = (RelativeLayout) itemView.findViewById(R.id.parentLayout);
        }
    }
}
