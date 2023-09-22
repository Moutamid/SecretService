package com.moutamid.secretservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.util.ArrayList;

public class AngelsAdapter extends RecyclerView.Adapter<AngelsAdapter.ContactVH> {
    Context context;
    ArrayList<ContactModel> list;

    public AngelsAdapter(Context context, ArrayList<ContactModel> list ) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ContactVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(context).inflate(R.layout.contacts_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactVH holder, int position) {
        ContactModel model = list.get(holder.getAdapterPosition());
        holder.name.setText(model.getContactName() + " (" + model.getContactNumber() +")");
        holder.remove.setOnClickListener(v -> {
            list.remove(model);
            Stash.put(Constants.ANGELS_LIST, list);
            notifyItemRemoved(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ContactVH extends RecyclerView.ViewHolder{

        TextView name;
        ImageView remove;

        public ContactVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            remove = itemView.findViewById(R.id.remove);
        }
    }

}
