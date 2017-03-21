package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by akain on 24/02/2017.
 */

public class FavouritesViewHolder extends RecyclerView.ViewHolder {
    View mView;
    TextView joiners,dateField,thumbTitle,thumb_pname;
    ImageView thumbImage,delete;


    public FavouritesViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        joiners = (TextView)mView.findViewById(R.id.joiners_field);
        dateField = (TextView)mView.findViewById(R.id.dateField);
        thumbTitle = (TextView)mView.findViewById(R.id.thumb_title);
        thumbImage = (ImageView) mView.findViewById(R.id.thumb_image);
        thumb_pname = (TextView)mView.findViewById(R.id.thumb_pname);
        delete = (ImageView)mView.findViewById(R.id.thumb_delete);


    }

    public void setThumbImage(final Context ctx, final String path){


        Glide.with(ctx)
                .load(path)
                .placeholder(R.drawable.     //da cambiare
                        loading_placeholder) //da cambiare
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_error)
                .crossFade()
                .into(thumbImage);

    }
    public void setThumbTitle(String title){
                    thumbTitle.setText(title);
            }
    public void setJoiners(Integer ejoiners){

            switch (ejoiners){
                case(1):
                    String partecipanti = "Partecipante";
                    String myJoiners = ejoiners.toString() + "\n";
                    String myString = myJoiners + partecipanti;
                    SpannableString string = new SpannableString(myString);
                    string.setSpan(new RelativeSizeSpan(3f), 0, myJoiners.length(), 0);
                    joiners.setText(string);
                    break;

                case(0):
                    joiners.setText("Non ci sono ancora \n partecipanti");
                    break;

                default:
                    String partecipantis = "Partecipanti";
                    String myJoinerss = ejoiners.toString() + "\n";
                    String myStrings = myJoinerss + partecipantis;
                    SpannableString strings = new SpannableString(myStrings);
                    strings.setSpan(new RelativeSizeSpan(3f), 0, myJoinerss.length(), 0);
                    joiners.setText(strings);
            }
    }
    public void setpName(String name){
        thumb_pname.setText(name);
    }
    public void setThumbTime(Long time){
        dateField.setText(fromMillisToStringDate(time)+"  ~  "+fromMillisToStringTime(time));
    }

    //da millisecondi a data
    protected String fromMillisToStringDate(Long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM");
        String[] splittedDate = format.format(date).split("/");
        return splittedDate[0]+" "+splittedDate[1];
    }

    //da millisecondi ad orario
    protected String fromMillisToStringTime(Long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }
}
