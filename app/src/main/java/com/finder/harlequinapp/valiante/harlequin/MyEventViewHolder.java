package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.truizlop.fabreveallayout.FABRevealLayout;

import static android.view.Gravity.CENTER;

public class MyEventViewHolder extends RecyclerView.ViewHolder{

    View mView;
    CircularImageView cardProfile;
    TextView cardLikes,cardDate,cardTime,pName;
    TextView event_name,joiners,etaMedia,maleSex,cardPrice,femaleSex;
    FABRevealLayout mFABRevealLayout;
    Button chiudi;
    FloatingActionButton fabLike;


    //costruttore del View Holder personalizzato
    public MyEventViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
        //Elementi UI per la carta evento

        cardPrice = (TextView)mView.findViewById(R.id.cardPrice);
        joiners = (TextView)mView.findViewById(R.id.partecipanti);
        etaMedia = (TextView)mView.findViewById(R.id.eta_media);
        maleSex = (TextView)mView.findViewById(R.id.male_sex_distribution);
        femaleSex = (TextView)mView.findViewById(R.id.female_sex_distribution);
        cardDate = (TextView)mView.findViewById(R.id.cardDay);
        cardTime = (TextView)mView.findViewById(R.id.cardTime);
        mFABRevealLayout = (FABRevealLayout)mView.findViewById(R.id.fab_reveal);
        chiudi = (Button)mView.findViewById(R.id.closeInfo);
        fabLike = (FloatingActionButton)mView.findViewById(R.id.fabLike);
        pName = (TextView)mView.findViewById(R.id.place_name);

    }

    public  void setCardPrice (Float price, Boolean isFree){
        if(isFree){
            cardPrice.setText("Free entry");
        }
        if(!isFree){
            cardPrice.setText("Ingresso "+price+"€");
        }
    }
    public void revealFabInfo(Integer eta,Integer numeroPartecipanti,Integer maleLikes, Integer femaleLikes){
        //se è presente almeno un lik
        if(numeroPartecipanti !=0) {
            if(numeroPartecipanti == 1){
                joiners.setText(1+ " Partecipante");
            }
            else {
                joiners.setText(numeroPartecipanti + " Partecipanti");
            }
            joiners.setVisibility(View.VISIBLE);
            maleSex.setVisibility(View.VISIBLE);
            femaleSex.setVisibility(View.VISIBLE);
            etaMedia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.age_white_16, 0, 0, 0);
            etaMedia.setText("Età media : " + eta);
            maleSex.setText(getMalePercentage(numeroPartecipanti,maleLikes)+"% Uomini");
            femaleSex.setText(getFemalePercentage(numeroPartecipanti,femaleLikes)+"% Donne");
        }
        //se non è presente nessun Like
        else{
            //rimuove la visibilità degli elementi che non servono
            joiners.setVisibility(View.GONE);
            maleSex.setVisibility(View.GONE);
            femaleSex.setVisibility(View.GONE);
            etaMedia.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            etaMedia.setGravity(CENTER);
            etaMedia.setText("Non ci sono ancora partecipanti a questo evento");


        }
    }
    //metodi necessari per visualizzare dinamicamente i dati di ogni EventCard
    public void setThumbUp (){
        fabLike.setImageResource(R.drawable.white_star_empty_24);
    }
    public void setThumbDown (){
        fabLike.setImageResource(R.drawable.white_star_full_24);
    }
    //TODO fa vedere i like correnti, da migliorare

    public void setEventName (String eventName){
        event_name = (TextView)mView.findViewById(R.id.CardViewTitle);
        event_name.setText(eventName);
    }
    /*
    public void setDescription (String description){
        TextView event_desc = (TextView)mView.findViewById(R.id.CardViewDescription);
        event_desc.setText(description);
    }*/
    public void setEventImage (final Context ctx, final String eventImagePath){
        final ImageView event_image = (ImageView)mView.findViewById(R.id.CardViewImage);

        Picasso.with(ctx)
                .load(eventImagePath)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(event_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        //va bene così non deve fare nulla
                    }
                    @Override
                    public void onError() {
                        Picasso.with(ctx).load(eventImagePath).into(event_image);
                    }
                });
    }
    public void setCardDate (String eventDate){
        cardDate.setText(eventDate);
    }
    public void setCardTime (String eventTime) { cardTime.setText(eventTime);}
    public void setPlaceName (String place){pName.setText(place);}


    //Helper methods
    public Float getMalePercentage (Integer totalLikes, Integer maleLikes){
        Float malePercentage ;
        malePercentage = Float.valueOf((100 * maleLikes) / totalLikes);
        return malePercentage;
    }

    public Float getFemalePercentage (Integer totalLikes, Integer femaleLikes){
        Float femalePercentage;
        femalePercentage = Float.valueOf((100*femaleLikes)/totalLikes);
        return femalePercentage;
    }


}//[END]eventViewHolder


