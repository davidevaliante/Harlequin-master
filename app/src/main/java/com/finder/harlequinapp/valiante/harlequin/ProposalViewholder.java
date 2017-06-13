package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.StringTokenizer;


public class ProposalViewholder extends RecyclerView.ViewHolder {

    View mView;
    ImageView argumentIcon;
    TextView title,description,elapsedTime, peopleInterested, placesNotified;
    RelativeLayout interestButton;
    ImageButton proposalOptions;

    public ProposalViewholder(View itemView) {
        super(itemView);
        mView = itemView;

        //UI
        argumentIcon = (ImageView)mView.findViewById(R.id.argument_icon);
        title = (TextView)mView.findViewById(R.id.proposalTitle);
        description = (TextView)mView.findViewById(R.id.proposalDescription);
        elapsedTime = (TextView)mView.findViewById(R.id.proposalTime);
        peopleInterested = (TextView)mView.findViewById(R.id.numberOfInterest);
        placesNotified = (TextView)mView.findViewById(R.id.placesNotified);
        interestButton = (RelativeLayout)mView.findViewById(R.id.interestButton);
        proposalOptions = (ImageButton)mView.findViewById(R.id.proposalOptions);
    }

    public void setArgumentIcon(String argument, Context ctx){
        switch(argument){
            case "cocktail":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.cocktail_green_46));
                break;

            case "dance":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.dance_red_46));
                break;

            case "music":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.music_blue_icon_46));
                break;

            case "party":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.party_icon_46));
                break;

            case "themed":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.themed_purple_46));
                break;
        }
    }

    public void colorSetter(String argument, Context ctx){
        switch (argument){
            case "cocktail":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.cocktail_green));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.cocktail_green));
                break;

            case "dance":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.dance_red));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.dance_red));
                break;

            case "music":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.music_blue));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.music_blue));
                break;

            case "party":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.party_orange));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.party_orange));
                break;

            case "themed":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.themed_purple));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.themed_purple));
                break;
        }
    }

    public void setTheme(String argument, Context ctx){
        setArgumentIcon(argument,ctx);
        colorSetter(argument,ctx);
    }

    public void setTitle(String pTitle){
        title.setText(pTitle);
    }

    public void setDescription(String pDesc){
        description.setText(pDesc);
    }

    public void setPeopleInterested(Integer likes){
        if(likes > 1) {
            peopleInterested.setText(likes + " Interessati");
        }

        if (likes == 0){
            peopleInterested.setText("Nessuno interessato");
        }

        if(likes == 1){
            peopleInterested.setText(likes+" Interessato");
        }
    }

    public void setPlacesNotified(String places){
        Integer counter = 0;
        StringTokenizer tokens = new StringTokenizer(places,"#");
        while (tokens.hasMoreTokens()){
            counter++;
        }
        if(counter == 0) {
            placesNotified.setText("Nessun locale notificato");
        }
        if(counter == 1) {
            placesNotified.setText("1 Locale notificato");
        }
        if(counter > 1) {
            placesNotified.setText(counter+" Locali notificati");
        }
    }
}
