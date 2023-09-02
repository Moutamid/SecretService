package com.moutamid.secretservice.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.secretservice.R;
import com.moutamid.secretservice.listners.ContactDeleteListners;
import com.moutamid.secretservice.models.ContactModel;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactVH> {
    Context context;
    ArrayList<ContactModel> list;
    ContactDeleteListners contactDeleteListners;

    public ContactsAdapter(Context context, ArrayList<ContactModel> list, ContactDeleteListners contactDeleteListners) {
        this.context = context;
        this.list = list;
        this.contactDeleteListners = contactDeleteListners;
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
        holder.remove.setOnClickListener(v -> contactDeleteListners.onClick(list.get(holder.getAdapterPosition())));
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
