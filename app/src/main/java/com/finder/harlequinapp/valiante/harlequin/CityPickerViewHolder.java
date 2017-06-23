package com.finder.harlequinapp.valiante.harlequin;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by akain on 23/06/2017.
 */

public class CityPickerViewHolder extends RecyclerView.ViewHolder {
    View mView;
    ImageView check;
    TextView city;

    public CityPickerViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        check = (ImageView)itemView.findViewById(R.id.isCurrentCity);
        city = (TextView)itemView.findViewById(R.id.cityItem);

    }
}
