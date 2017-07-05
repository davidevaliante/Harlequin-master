package com.finder.harlequinapp.valiante.harlequin;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.StringTokenizer;

public class ProposalViewholder extends RecyclerView.ViewHolder {

    View mView;
    ImageView argumentIcon;
    TextView title,description,elapsedTime, peopleInterested, placesNotified,creatorName;
    public RelativeLayout interestButton;
    ImageButton proposalOptions;
    String default_argument;
    TextView buttonInterest;

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
        buttonInterest = (TextView) mView.findViewById(R.id.buttonInterest);
        creatorName = (TextView)mView.findViewById(R.id.creatorName);
    }



    public void setGreyBg(Context ctx){
        interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.light_grey));
        buttonInterest.setText("Sei già interessato a questa proposta");
    }

    public void setArgumentIcon(String argument, Context ctx){
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

    public void colorSetter(String argument, Context ctx){
        switch (argument){
            case "cocktail":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.cocktail_green));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.cocktail_green));
                creatorName.setBackgroundColor(ContextCompat.getColor(ctx,R.color.cocktail_green));
                break;

            case "dance":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.dance_red));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.dance_red));
                creatorName.setBackgroundColor(ContextCompat.getColor(ctx,R.color.dance_red));
                break;

            case "music":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.music_blue));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.music_blue));
                creatorName.setBackgroundColor(ContextCompat.getColor(ctx,R.color.music_blue));
                break;

            case "party":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.party_orange));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.party_orange));
                creatorName.setBackgroundColor(ContextCompat.getColor(ctx,R.color.party_orange));
                break;

            case "themed":
                peopleInterested.setTextColor(ContextCompat.getColor(ctx,R.color.themed_purple));
                interestButton.setBackgroundColor(ContextCompat.getColor(ctx,R.color.themed_purple));
                creatorName.setBackgroundColor(ContextCompat.getColor(ctx,R.color.themed_purple));
                break;
        }
    }

    public void setTheme(String argument, Context ctx){
        buttonInterest.setText("Voglio organizzare questo evento !");
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
            placesNotified.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        }
        if(counter == 1) {
            placesNotified.setText("1 Locale notificato");
            placesNotified.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

        }
        if(counter > 1) {
            placesNotified.setText(counter+" Locali notificati");
            placesNotified.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

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

    public void likingTransition(String arg, Activity activity){
        //transizione da argument color a light_grey
        ObjectAnimator colorFade = ObjectAnimator.ofObject(interestButton,"backgroundColor", new ArgbEvaluator(),colorArgumentSwitcher(arg,activity),ContextCompat.getColor(activity,R.color.light_grey));
        colorFade.setDuration(1500);
        colorFade.setStartDelay(100);
        colorFade.start();
    }

    public void dislikeTransition(String arg,Activity activity){


        //transizione da light_grey a argument color
        ObjectAnimator colorFade = ObjectAnimator.ofObject(interestButton,"backgroundColor", new ArgbEvaluator(),ContextCompat.getColor(activity,R.color.light_grey),colorArgumentSwitcher(arg,activity));
        colorFade.setDuration(1500);
        colorFade.setStartDelay(100);
        colorFade.start();


    }

    //restituisce int color in base all'argomento della proposta
    private Integer colorArgumentSwitcher(String argument,Activity activity){
        switch (argument){
            case "cocktail":
                return ContextCompat.getColor(activity, R.color.cocktail_green);

            case "dance":
                return ContextCompat.getColor(activity, R.color.dance_red);

            case "music":
                return ContextCompat.getColor(activity, R.color.music_blue);

            case "party":
                return ContextCompat.getColor(activity, R.color.party_orange);

            case "themed":
                return ContextCompat.getColor(activity, R.color.themed_purple);

            default:
                return ContextCompat.getColor(activity, R.color.colorPrimary);

        }

    }

    public void setCreatorName(Boolean isAnon,String name){
        if(!isAnon){
            creatorName.setVisibility(View.VISIBLE);
            creatorName.setText("#"+name);
        }else{
            creatorName.setVisibility(View.GONE);
        }
    }
}
