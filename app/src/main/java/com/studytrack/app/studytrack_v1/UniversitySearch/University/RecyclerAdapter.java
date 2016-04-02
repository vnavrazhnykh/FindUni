package com.studytrack.app.studytrack_v1.UniversitySearch.University;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.studytrack.app.studytrack_v1.R;
import com.studytrack.app.studytrack_v1.UniversitySearch.University.RecyclerHolder.ContactViewHolder;
import com.studytrack.app.studytrack_v1.UniversitySearch.University.RecyclerHolder.OptionsViewHolder;

import Entities.University;


/**
 * Created by vadim on 29.01.16.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    University data;
    Activity activity;
    RecyclerAdapter(University data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int page) {

        return RecyclerHolder.newInstance(page, parent);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int page) {
        switch (page) {
            case RecyclerHolder.OPTIONS:
                final OptionsViewHolder options_holder = (OptionsViewHolder) viewHolder;
                options_holder.web.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(data.getSite()));
                        activity.startActivity(intent);
                    }
                });

                options_holder.favourites.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.inverseLiked();

                        if(data.getLiked() == 1) {
                            Drawable img = activity.getApplicationContext().getResources().getDrawable(R.drawable.star_checked_dark);
                            options_holder.favourites.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                        } else {
                            Drawable img = activity.getApplicationContext().getResources().getDrawable(R.drawable.star_unchecked_dark);
                            options_holder.favourites.setCompoundDrawablesWithIntrinsicBounds(null,img,null,null);

                        }
                    }
                });

                options_holder.map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + data.getAddress());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        activity.startActivity(mapIntent);
                    }
                });
                break;

            case RecyclerHolder.SCORES:
                ((RecyclerHolder.ScoresViewHolder) viewHolder).setValues((float)data.getMeanPoints()
                        ,(float) data.getMeanPrice());
                ((RecyclerHolder.ScoresViewHolder) viewHolder).animate();
                break;

            case RecyclerHolder.CONTACTS:
                ContactViewHolder holder = (ContactViewHolder) viewHolder;
                holder.address.setText(data.getAddress());
                holder.phone.setText(data.getPhone());
                holder.site.setText(data.getSite());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return RecyclerHolder.PAGES_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}