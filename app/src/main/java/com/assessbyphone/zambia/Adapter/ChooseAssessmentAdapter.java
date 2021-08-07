package com.assessbyphone.zambia.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assessbyphone.zambia.R;

import java.util.ArrayList;

public class ChooseAssessmentAdapter extends RecyclerView.Adapter<ChooseAssessmentAdapter.RecyclerViewHolder> {
    private final ArrayList<String> assessmentNameALL;
    private onItemClickedInterface onClick;

    public ChooseAssessmentAdapter(ArrayList<String> model) {
        this.assessmentNameALL = model;
    }

    public void onItemClicked(onItemClickedInterface clicked) {
        this.onClick = clicked;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_assessment_recycler_design, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, final int position) {
        String assName = assessmentNameALL.get(position);
        holder.assessmentNameTv.setText(assName);
        switch (position) {
            // Admin Result
            case 0:
                holder.typeIcon.setImageResource(R.drawable.admin_icon);
                break;

            // Phonics Check
            case 1:
                holder.typeIcon.setImageResource(R.drawable.assessment_icon);
                break;

            // English/Comprehension word problem
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                holder.typeIcon.setImageResource(R.drawable.eng_test_icon);
                break;

            // Maths number word problem
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                holder.typeIcon.setImageResource(R.drawable.math_icon);
                break;

            // Basic Math word problem
            case 13:
            case 14:
            case 15:
                holder.typeIcon.setImageResource(R.drawable.adv_icon);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return assessmentNameALL == null ? 0 : assessmentNameALL.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface onItemClickedInterface {

        void getCurrentAssessment(String assessmentName, int position);

    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView typeIcon;
        private final TextView assessmentNameTv;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            RelativeLayout assessmentLay = itemView.findViewById(R.id.assessmentLay_ids);
            typeIcon = itemView.findViewById(R.id.typeIcon_ids);
            assessmentNameTv = itemView.findViewById(R.id.assessmentNameTv_ids);

            assessmentLay.setOnClickListener(this);
        }

        @Override
        public void onClick(View attView) {
            switch (attView.getId()) {
                case R.id.assessmentLay_ids:
                    onClick.getCurrentAssessment(assessmentNameALL.get(getLayoutPosition()), getLayoutPosition());
                    break;
            }
        }
    }
}