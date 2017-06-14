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
import java.util.concurrent.TimeUnit;


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

    private void setArgumentIcon(String argument, Context ctx){
        switch(argument){
            case "cocktail":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.cocktail_62));
                break;

            case "dance":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.red_dance_62));
                break;

            case "music":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.music_blue_icon_46));
                break;

            case "party":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.party_62));
                break;

            case "themed":
                argumentIcon.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.themed_party_62));
                break;
        }
    }

    private void colorSetter(String argument, Context ctx){
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

        Integer tokenCount = tokens.countTokens();
        for(int i =0;i<tokenCount;i++){
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

    public void setElapsedTime(Long currentTime, Long pastTime){
        elapsedTime.setText(timeDifference(currentTime,pastTime));
    }

    public String timeDifference(Long currentTime,Long pastTime){
        Long timeDifference = currentTime - pastTime;
        Integer differenceSeconds = (int)(timeDifference/1000);
        if(differenceSeconds <=60){
            return differenceSeconds+" s";
        }

        if(differenceSeconds>60 && differenceSeconds<=3600){
            Integer minutes = (int) ((timeDifference/1000)/60);
            return  minutes+" m";
        }

        if(differenceSeconds>3600 && differenceSeconds<=86400){
            Integer hours = (int) ((timeDifference/1000)/3600);
            return hours+" h";
        }

        if(differenceSeconds>86400){
            Integer days = (int)((((timeDifference)/1000)/3600)/24);
            return days+ "g ";
        }



        return "3h";
    }
}
