package com.softwareengineering2019.silverkisses;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private ArrayList<Workout> workouts;


        private LayoutInflater mInflater;
        private ItemClickListener mClickListener;

        // data is passed into the constructor
        public ListAdapter(Context context, ArrayList<Workout> userWorkouts) {
                this.mInflater = LayoutInflater.from(context);
                //mFilteredList = new ArrayList<>(events);
                workouts = userWorkouts;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = mInflater.inflate(R.layout.fragment_item, parent, false);
                return new ViewHolder(view);

        }



        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ListAdapter.ViewHolder holder, int i) {
                holder.dateView.setText(workouts.get(i).getDate().substring(0,10));
                holder.contentView.setText("Duration: " + workouts.get(i).getDuration() + " Distance: " + String.format("%.1f", workouts.get(i).getDistance()));
                if(i %2 == 1)
                {
                        holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                else
                {
                        holder.itemView.setBackgroundColor(Color.parseColor("#FFFAF8FD"));
                        //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFAF8FD"));
                }
        }

        // total number of rows
        @Override
        public int getItemCount() {
                return workouts.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                TextView dateView;
                TextView contentView;
                public ViewHolder(View itemView) {
                        super(itemView);

                        dateView = itemView.findViewById(R.id.txtdate);
                        contentView = itemView.findViewById(R.id.txtcontent);
                        itemView.setOnClickListener(this);
                }

                @Override
                public void onClick(View view) {
                        if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
                }
        }

        // convenience method for getting data at click position
        Workout getItem(int id) {
                return workouts.get(id);
        }

        // allows clicks events to be caught
        void setClickListener(ItemClickListener itemClickListener) {
                this.mClickListener = itemClickListener;
        }

        // parent activity will implement this method to respond to click events
        public interface ItemClickListener {
                void onItemClick(View view, int position);
        }


}