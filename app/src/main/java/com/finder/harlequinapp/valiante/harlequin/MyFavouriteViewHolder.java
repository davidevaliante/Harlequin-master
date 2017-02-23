package com.finder.harlequinapp.valiante.harlequin;


import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MyFavouriteViewHolder extends RecyclerView.ViewHolder{



        View mView;
        CircularImageView eventAvatar;
        TextView eventName,eventDate,eventTime,eventJoiners,maleNumber,femaleNumber,singlePercentage,engagedPercentage,price;
        FloatingActionButton cardLikeButton;


        public MyFavouriteViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            cardLikeButton = (FloatingActionButton)mView.findViewById(R.id.like_button_thumb);
            eventAvatar = (CircularImageView) mView.findViewById(R.id.image_thumb);
            eventName   = (TextView)mView.findViewById(R.id.name_thumb);
            eventDate   = (TextView)mView.findViewById(R.id.date_thumb);
            eventTime   = (TextView)mView.findViewById(R.id.time_thumb);
            eventJoiners= (TextView)mView.findViewById(R.id.like_thumb);
            maleNumber = (TextView)mView.findViewById(R.id.male_thumb);
            femaleNumber = (TextView)mView.findViewById(R.id.female_thumb);
            singlePercentage = (TextView)mView.findViewById(R.id.single_thumb);
            engagedPercentage = (TextView)mView.findViewById(R.id.engaged_thumb);
            price = (TextView)mView.findViewById(R.id.price_thumb);

        }

        public void setAvatar (final Context avatarctx, final String avatarUrl){
            Picasso.with(avatarctx)
                    .load(avatarUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(eventAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            //va bene così non deve fare nulla
                        }
                        @Override
                        public void onError() {
                            Picasso.with(avatarctx).load(avatarUrl).into(eventAvatar);
                        }
                    });

        }
        public void setName (String name){
            eventName.setText(name);
        }
        public void setTime (String time){
            eventTime.setText(time);
        }
        public void setEventDate (String date){
            eventDate.setText(date);
        }
        public void setEventJoiners (Integer joiners){
            if(joiners==1)
            eventJoiners.setText(joiners+" partecipante");
            else{
                eventDate.setText(joiners+" partecipanti");
            }
        }
        public void setMaleNumber (Integer males){
            if(males==0) {
                maleNumber.setText(males + " Uomo");
            }
            else{
                maleNumber.setText(males + " Uomini");
            }
        }
        public void setFemaleNumber(Integer females){
            if(females==0) {
                femaleNumber.setText(females + " Donna");
            }
            else{
                femaleNumber.setText(females+" Donne");
            }
        }
        public void setSinglePercentage (Integer likes,Integer singles){
            singlePercentage.setText(getSinglesPercentage(likes,singles)+" singles");
        }
        public void setEngagedPercentage(Integer likes,Integer engaged){
            engagedPercentage.setText(getEngagedPercentage(likes,engaged)+" impegnati");
        }
        public void setPrice (Float priceValue){
            if(priceValue==0){
                price.setText("Free Entry");
            }
            else{
                price.setText(priceValue + " €");
            }
        }

        //Helper methods
        public Float getSinglesPercentage (Integer totalLikes, Integer singlesLikes){

            Float singlesPercentage ;
            if(totalLikes!=0) {
                singlesPercentage = Float.valueOf((100 * singlesLikes) / totalLikes);
                return singlesPercentage;
            }else{
                singlesPercentage = Float.valueOf(0);
                return  singlesPercentage;
            }
        }

        public Float getEngagedPercentage (Integer totalLikes, Integer engagedLikes){
            Float engagedPercentage;
            if(totalLikes!=0) {
                engagedPercentage = Float.valueOf((100 * engagedLikes) / totalLikes);
                return engagedPercentage;
            }else{
                engagedPercentage = Float.valueOf(0);
                return engagedPercentage;
            }
        }

}
    //[FINE FavouritesEventViewHolder

