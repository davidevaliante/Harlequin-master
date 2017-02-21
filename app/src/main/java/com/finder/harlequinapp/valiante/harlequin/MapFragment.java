package com.finder.harlequinapp.valiante.harlequin;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MapFragment extends Fragment {

    CoordinatorLayout map_frag;
    Button basicMap;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        map_frag = (CoordinatorLayout)inflater.inflate(R.layout.fragment_map,container,false);
        basicMap = (Button)map_frag.findViewById(R.id.basic_map_btn);

        basicMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toBasicMap = new Intent (getActivity(),BasicMap.class);
                startActivity(toBasicMap);
            }
        });
        return map_frag;
    }

}
