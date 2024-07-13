package com.coding.dynasty.systeminfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SystemInfoAdapter extends RecyclerView.Adapter<SystemInfoAdapter.SystemInfoViewHolder> {
    private final List<SystemInfo> systemInfoList;

    public SystemInfoAdapter(List<SystemInfo> systemInfoList) {
        this.systemInfoList = systemInfoList;
    }

    @NonNull
    @Override
    public SystemInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_info, parent, false);
        return new SystemInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SystemInfoViewHolder holder, int position) {
        SystemInfo systemInfo = systemInfoList.get(position);
        holder.infoTitle.setText(systemInfo.getTitle());
        holder.infoDetails.setText(systemInfo.getDetails());
    }

    @Override
    public int getItemCount() {
        return systemInfoList.size();
    }

    public static class SystemInfoViewHolder extends RecyclerView.ViewHolder {
        TextView infoTitle, infoDetails;

        public SystemInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            infoTitle = itemView.findViewById(R.id.info_title);
            infoDetails = itemView.findViewById(R.id.info_details);
        }
    }

}
