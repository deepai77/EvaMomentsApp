package com.cindura.evamomentsapp.helper;

//Interface helps when a recyclerview item has been clicked and also determines which type of click
public interface IRecyclerViewClickListener {
    void onLongClicked(int position);

    void onMenuClicked(int position);

    void onSingleClick(int position);
}
