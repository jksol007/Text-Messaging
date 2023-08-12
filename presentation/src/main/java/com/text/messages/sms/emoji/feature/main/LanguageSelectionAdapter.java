package com.text.messages.sms.emoji.feature.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.text.messages.sms.emoji.R;

import java.util.ArrayList;

public class LanguageSelectionAdapter extends RecyclerView.Adapter<LanguageSelectionAdapter.ImageViewHolder> {

    private Context context;
    private ArrayList<LanguageEntity> languageList;

    public LanguageSelectionAdapter(Context context, ArrayList<LanguageEntity> languageList) {
        this.context = context;
        this.languageList = languageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_language, parent, false);
        return new ImageViewHolder(v1);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        LanguageEntity language = languageList.get(position);
        holder.imgLanguage.setImageResource(language.getColor());
        holder.txtLanguageName.setText(language.getCode());
        holder.languagetextView.setText(language.getName());
        holder.radioSelected.setChecked(language.getSelected());
    }

    @Override
    public int getItemCount() {
        return languageList != null ? languageList.size() : 0;
    }

    public LanguageEntity getSelectedItem() {
        if (languageList != null && languageList.size() > 0) {
            for (LanguageEntity language : languageList) {
                if (language.getSelected()) {
                    return language;
                }
            }
        }
        return null;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imgLanguage;
        private final TextView txtLanguageName;
        private final TextView languagetextView;
        private final RadioButton radioSelected;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imgLanguage = itemView.findViewById(R.id.country_bg);
            txtLanguageName = itemView.findViewById(R.id.country_text);
            languagetextView = itemView.findViewById(R.id.languagetextView);
            radioSelected = itemView.findViewById(R.id.radioButton);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            for (LanguageEntity language : languageList) {
                language.setSelected(false);
            }

            languageList.get(getBindingAdapterPosition()).setSelected(true);
            notifyDataSetChanged();
        }
    }
}