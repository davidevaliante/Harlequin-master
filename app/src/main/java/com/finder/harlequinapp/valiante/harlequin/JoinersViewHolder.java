package com.finder.harlequinapp.valiante.harlequin;

import android.content.Context;
import android.media.Image;
import android.opengl.Visibility;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

/**
 * Created by akain on 27/02/2017.
 */

public class JoinersViewHolder extends RecyclerView.ViewHolder {
    View mView;
    TextView nome,status,city,age;
    ImageView avatar,sex;
    LinearLayout mainLayout;
    ImageView main_underline;
    ImageView acceptBtn;
    public JoinersViewHolder(View itemView) {
        super(itemView);
        mView=itemView;

        age = (TextView)mView.findViewById(R.id.joinerage);
        nome = (TextView)mView.findViewById(R.id.nomeutente);
        status = (TextView)mView.findViewById(R.id.relazione);
        city = (TextView)mView.findViewById(R.id.citta);
        mainLayout = (LinearLayout)mView.findViewById(R.id.main_layout);
        avatar = (ImageView)mView.findViewById(R.id.thumb_avatar);
        sex = (ImageView)mView.findViewById(R.id.sexindicator);
        main_underline = (ImageView)mView.findViewById(R.id.main_underline);
        acceptBtn = (ImageView)mView.findViewById(R.id.acceptRequestButton);
        acceptBtn.setVisibility(View.GONE);


    }

    public void setAge(String userage){
        age.setText(getAge(userage)+" anni");
    }

    public  void setAvatar(final String path, final Context ctx){


        Glide.with(ctx)
                .load(path)
                .placeholder(ContextCompat.getDrawable(ctx,R.drawable.loading_placeholder)) //da cambiare
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(ContextCompat.getDrawable(ctx,R.drawable.ic_error))
                .crossFade()
                .into(avatar);
    }

    public void setNome(String name, String surname){
        nome.setText(name+" "+surname);
    }

    public void setSex(Boolean isMale, Context ctx){
        if(isMale){
            sex.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.male_white_16));
            //sex.setBackgroundColor(ContextCompat.getColor(ctx ,R.color.shaded_maled));
            sex.setBackgroundColor(ContextCompat.getColor(ctx ,R.color.shaded_20));
            main_underline.setBackground(ContextCompat.getDrawable(ctx,R.drawable.bottom_matteline));
        }
        else{
            sex.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.female_white_16));
            //sex.setBackgroundColor(ContextCompat.getColor(ctx ,R.color.shaded_female));
            sex.setBackgroundColor(ContextCompat.getColor(ctx ,R.color.shaded_20));
            main_underline.setBackground(ContextCompat.getDrawable(ctx,R.drawable.top_bottom_primaryline));
        }
    }

    public void setCity(String userCity){
        city.setText(userCity);
    }

    public void setStatus(Boolean isSingle,Boolean isMale){
        if (isSingle){
            status.setText("Single");
        }
        else{
            if(isMale) {
                status.setText("Impegnato");
            }else{
                status.setText("Impegnata");
            }
        }
    }

    //calcola l'età da String a Integer
    public Integer getAge (String birthdate){
        //estrae i numeri dalla stringa
        String parts [] = birthdate.split("/");
        //li casta in interi
        Integer day = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        Integer year = Integer.parseInt(parts[2]);

        //oggetto per l'anno di nascita
        Calendar dob = Calendar.getInstance();
        //oggetto per l'anno corrente
        Calendar today = Calendar.getInstance();

        //setta anno di nascita in formato data
        dob.set(year,month,day);
        //calcola l'anno
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        //controlla che il giorno attuale sia minore del giorno del compleanno
        //nel caso in cui fosse vero allora il compleanno non è ancora passato e il conteggio degli anni viene diminuito
        if (today.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        //restituisce l'età sotto forma numerica utile per calcolare l'età media dei partecipanti ad un evento
        return age;

    }
}
