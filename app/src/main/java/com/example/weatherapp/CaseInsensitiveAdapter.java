package com.example.weatherapp;
import android.content.Context;
import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

public class CaseInsensitiveAdapter extends ArrayAdapter<String>{
    private List<String> tumSehirler;
    private List<String> filtrelenmis;

    public CaseInsensitiveAdapter(Context context, int resource, String[] sehirler){
        super(context, resource);
        tumSehirler = new ArrayList<>();
        filtrelenmis = new ArrayList<>();

        for (String sehir : sehirler){
            tumSehirler.add(sehir);
        }
    }

    @Override
    public int getCount(){
        return filtrelenmis.size();
    }

    @Override
    public String getItem(int position){
        return filtrelenmis.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> sonuclar = new ArrayList<>();
                if (constraint != null && constraint.length() > 0) {

                    String aranan = turkceKucukHarf(constraint.toString());
                    for (String sehir : tumSehirler){
                        String sehirKucuk = turkceKucukHarf(sehir);
                        if (sehirKucuk.contains(aranan)){
                            sonuclar.add(sehir);
                        }
                    }

                }
                results.values = sonuclar;
                results.count= sonuclar.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtrelenmis.clear();
                if (results.values != null){
                    filtrelenmis.addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }
    private String turkceKucukHarf(String text){
        text = text.replace("I", "i");
        text = text.replace("İ", "i");
        return text.toLowerCase(new Locale("tr", "TR"));
    }
}
