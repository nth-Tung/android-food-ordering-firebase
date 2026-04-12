package com.nttung.oufood.Adapter;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nttung.oufood.R;

import java.util.List;

public class AddressSuggetionAdapter extends RecyclerView.Adapter<AddressSuggetionAdapter.ViewHolder> {

    private List<Address> items;
    private Context context;
    private final OnAddressClickListener onAddressClickListener;

    public interface OnAddressClickListener {
        void onAddressClick(Address address);
    }

    public AddressSuggetionAdapter(List<Address> items){
        this(items, null);
    }

    public AddressSuggetionAdapter(List<Address> items, OnAddressClickListener onAddressClickListener) {
        this.items = items;
        this.onAddressClickListener = onAddressClickListener;
    }

    @NonNull
    @Override
    public AddressSuggetionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_address_suggetion, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressSuggetionAdapter.ViewHolder holder, int position) {
        Address address = items.get(position);
        holder.textView_addressSuggestion.setText(address.getAddressLine(0));
        holder.itemView.setOnClickListener(v -> {
            if (onAddressClickListener != null) {
                onAddressClickListener.onAddressClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_addressSuggestion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_addressSuggestion= itemView.findViewById(R.id.textView_addressSuggestion);
        }
    }
}
