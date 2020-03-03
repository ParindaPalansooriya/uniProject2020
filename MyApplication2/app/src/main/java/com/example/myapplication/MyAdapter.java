package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class MyAdapter extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> T1Array;
    private Context contex;

    public MyAdapter(Context context, ArrayList<ListItem> T11){
        super(context,R.layout.left_massage,R.id.textView2,T11);
        this.T1Array=T11;
        this.contex=context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertViwe, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row ;
        if (T1Array.get(position).isUser()){
            row = inflater.inflate(R.layout.right_massage,parent,false);
        }else {
            row = inflater.inflate(R.layout.left_massage,parent,false);
        }
        TextView myT1 = row.findViewById(R.id.textView2);
        myT1.setText(T1Array.get(position).getText());
        return row;
    }

}
