package com.nttung.oufood.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nttung.oufood.Class.AnAddress;
import com.nttung.oufood.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private List<AnAddress> items;
    private Context context;
    private final OnAddressActionListener listener;
    private final boolean selectMode;

    public interface OnAddressActionListener {
        void onEdit(int position, AnAddress address);
        void onSelect(int position, AnAddress address);
    }

    public AddressAdapter(List<AnAddress> items, boolean selectMode, OnAddressActionListener listener) {
        this.items = items;
        this.selectMode = selectMode;
        this.listener = listener;
    }

    public void setItems(List<AnAddress> items) {
        this.items = items;
    }

    public List<AnAddress> getItems() {
        return items;
    }

    @NonNull
    @Override
    public AddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.ViewHolder holder, int position) {
        AnAddress item = items.get(position);
        holder.textView_typeAddress.setText(item.getTypeAddress() == null ? "OTHER" : item.getTypeAddress().toString());
        holder.textView_address.setText(item.getAddress() == null ? "" : item.getAddress());
        holder.textView_name.setText(item.getName() == null ? "" : item.getName());
        holder.textView_phoneNumber.setText(item.getPhone() == null ? "" : item.getPhone());
        holder.textView_update.setText(selectMode ? "Chọn" : "Sửa");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                if (selectMode) {
                    listener.onSelect(position, item);
                } else {
                    listener.onEdit(position, item);
                }
            }
        });

        holder.textView_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    if (selectMode) {
                        listener.onSelect(position, item);
                    } else {
                        listener.onEdit(position, item);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_typeAddress, textView_update, textView_address, textView_name, textView_phoneNumber;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_typeAddress = itemView.findViewById(R.id.textView_typeAddress);
            textView_update = itemView.findViewById(R.id.textView_update);
            textView_address = itemView.findViewById(R.id.textView_address);
            textView_name = itemView.findViewById(R.id.textView_name);
            textView_phoneNumber = itemView.findViewById(R.id.textView_phoneNumber);
        }
    }
}
