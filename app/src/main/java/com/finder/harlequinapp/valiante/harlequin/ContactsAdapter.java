package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akain on 27/02/2017.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private List<String> names,numbers;
    private Context ctx;


    public static class ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView name;
        TextView number;
        TextView action_call;
        public ViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            name = (TextView)mView.findViewById(R.id.contact_name);
            number = (TextView)mView.findViewById(R.id.contact_number);
            action_call = (TextView)mView.findViewById(R.id.action_call);

        }

    }

    public ContactsAdapter (ArrayList<String> names, ArrayList<String> numbers, Context ctx){
        this.names = names;
        this.numbers = numbers;
        this.ctx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_card,parent,false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.name.setText(names.get(position));
        holder.number.setText(numbers.get(position));
        final String uri = holder.number.getText().toString().trim();
        holder.action_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent (Intent.ACTION_DIAL, Uri.parse("tel:"+uri));
                ctx.startActivity(callIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }


}
