package com.maxim.denisov.tranlator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class TranslatedWordArrayAdapter extends ArrayAdapter {

    private static class ViewHolder {
        TextView translatedWordView;
    }

    public TranslatedWordArrayAdapter(Context context, List<TranslatedWord> forecast) {
        super(context, -1, forecast);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TranslatedWord day = (TranslatedWord) getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.translatedWordView = (TextView) convertView.findViewById(R.id.translatedWordTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.translatedWordView.setText(day.translatedWord);

        return convertView;
    }

}
