

        package com.softwareengineering2019.silverkisses;
        import android.content.Context;
        import android.support.annotation.NonNull;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import java.util.ArrayList;
        import java.util.Locale;

        public class ListAdapter extends RecyclerView.Adapter<ListAdapter.WorkoutHolder> {

        private Context context;
        private ArrayList<Workout> workouts;

        public ListAdapter(Context context, ArrayList<Workout> workouts) {
        this.context = context;
        this.workouts = workouts;
        }

        @NonNull
        @Override
        public WorkoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_planet, parent, false);
        return new WorkoutHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutHolder holder, int position) {
       Workout Workout = Workouts.get(name);
        holder.setDetails(workout);
        }

        @Override
        public int getItemCount() {
        return Workouts.size();
        }

        class WorkoutHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtDistance, txtGravity, txtDiameter;

        WorkoutHolder(View itemView) {
        super(itemView);
        txtDate = itemView.findViewById(R.id.txtDate);
        txtDistance = itemView.findViewById(R.id.txtDistance);
        txtDuration = itemView.findViewById(R.id.txtDuration);

        }
            private void createListData() {

            }
        void setDetails(Workout workout) {
        txtDistance.setText(String.format(Locale.US, "Date: ", workouts.getDate()));
        txtGravity.setText(String.format(Locale.US, "Distance :", workouts.getDistance()));
        txtDiameter.setText(String.format(Locale.US, "Duration", planets.getDuration()));
        }
        }
        }
