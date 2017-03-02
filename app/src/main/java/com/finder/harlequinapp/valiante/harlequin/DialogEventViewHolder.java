package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;



public class DialogEventViewHolder extends RecyclerView.ViewHolder {

    View mView;
    ImageView square_avatar;
    TextView square_title;

    public DialogEventViewHolder(View itemView) {
                super(itemView);
        mView = itemView;
        square_avatar = (ImageView)mView.findViewById(R.id.square_image);
        square_title = (TextView)mView.findViewById(R.id.square_title);
    }

    public void setAvatar (final Context ctx, final String path){
        Picasso.with(ctx)
                .load(path)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(square_avatar, new Callback() {
                    @Override
                    public void onSuccess() {
                        //va bene così non deve fare nulla
                    }
                    @Override
                    public void onError() {
                        Picasso.with(ctx).load(path).into(square_avatar);
                    }
                });
    }

    public void setTitle(String title){
        square_title.setText(title);
    }
}
