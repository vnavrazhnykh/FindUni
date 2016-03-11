package com.studytrack.app.studytrack_v1.UniversitySearch.University;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.studytrack.app.studytrack_v1.R;
import com.studytrack.app.studytrack_v1.StudyTrackApplication;
import com.studytrack.app.studytrack_v1.Utils.Animator;
import com.studytrack.app.studytrack_v1.Utils.JSONloader;
import com.studytrack.app.studytrack_v1.myFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vadim on 22.01.16.
 */
public class UniversityFragment extends myFragment
        implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.95f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.5f;
    private static final float DIMMING_RATE                         = 0.7f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 100;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private AppCompatActivity activity;
    private View fragment;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private TextView mMainTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private View overlay;
    private ImageView mHeader;
    private CircleImageView mLogo;
    protected RecyclerView recycler;
    protected View progress;

    private UniData uniData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return fragment = inflater.inflate(R.layout.uni_body, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new FragmentLoader().execute();
    }

    private void hideMainToolbar() {
        Toolbar toolbar   = (Toolbar) activity.findViewById(R.id.main_toolbar);
        Animator.animAlpha(toolbar, 100, View.INVISIBLE);
        toolbar.setEnabled(false);
    }

    private void loadData() {
        uniData = (UniData) getArguments().getSerializable("data");
    }

    private void initActivity() {
        activity        = (AppCompatActivity) getActivity();
        mTitle          = (TextView) fragment.findViewById(R.id.main_textview_title);
        mMainTitle      = (TextView) fragment.findViewById(R.id.university_title_big);
        mTitleContainer = (LinearLayout) fragment.findViewById(R.id.main_linearlayout_title);
        mAppBarLayout   = (AppBarLayout) fragment.findViewById(R.id.main_appbar);
        overlay         = fragment.findViewById(R.id.overlay);
        mHeader         = (ImageView) fragment.findViewById(R.id.university_header);
        mLogo           = (CircleImageView) fragment.findViewById(R.id.university_logo);
        recycler        = (RecyclerView) fragment.findViewById(R.id.recycler);

        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    private void initToolbar() {
        mToolbar        = (Toolbar) fragment.findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.arrow_left);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onBackPressed();
            }
        });
    }

    private void initTitle() {
        mMainTitle.setText(uniData.getName());
        mTitle.setText(uniData.getName());

        mMainTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float scaledDensity = activity.getResources().getDisplayMetrics().scaledDensity;

                while (mMainTitle.getLineCount() > 3)
                    mMainTitle.setTextSize(mMainTitle.getTextSize() / scaledDensity - 1);
            }
        });

        Animator.animAlpha(mTitle, 0, View.INVISIBLE);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleDimming(percentage);
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleDimming(float percentage) {
        overlay.setAlpha(percentage * DIMMING_RATE);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                Animator.animAlpha(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
                mTitle.setSelected(true);
            }

        } else {

            if (mIsTheTitleVisible) {
                mTitle.setSelected(false);
                Animator.animAlpha(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                Animator.animAlpha(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                Animator.animAlpha(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private class FragmentLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadData();
            initActivity();
            initProgress();
            showProgress();
            hideMainToolbar();
            initToolbar();
            initTitle();
        }

        @Override
        protected Void doInBackground(Void... params) {

            long rowId = uniData.getName().hashCode();
            Log.d("Test", Long.toString(rowId));

            SQLiteDatabase db = ((StudyTrackApplication) activity.getApplicationContext()).getDB();
            Cursor cursor = db.query("university_table",
                    new String[] {"id", "isloaded", "isfavourite", "address", "phone", "site"},
                    "id = " + Long.toString(rowId),
                    null, null, null, null);

            if (cursor.moveToFirst()) {
                int isloaded = cursor.getInt(cursor.getColumnIndex("isloaded"));
                cursor.close();

                if (isloaded == 0) {
                    updateInfo(rowId, db);
                }

                readInfo(uniData, db, rowId);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Picasso.with(activity).load(R.drawable.university_header).into(mHeader);
            Picasso.with(activity).load(R.drawable.msu_logo).into(mLogo);

            recycler.setLayoutManager(new LinearLayoutManager(activity));
            recycler.setAdapter(new RecyclerAdapter(uniData));

            hideProgress();
        }

        private void updateInfo(long Id, SQLiteDatabase db) {
            try {
                JSONloader loader = new JSONloader();
                JSONArray ar = loader.fromHTTP("http://finduniv.appspot.com/getUniversities", "count=20");

                for (int univ_id = 0; univ_id < ar.length(); ++univ_id) {
                    JSONObject univ = ar.getJSONObject(univ_id);
                    if (univ.getString("name").hashCode() == Id) {
                        ContentValues newValues = new ContentValues();
                        newValues.put("isloaded", 1);
                        newValues.put("site", univ.getString("site_url"));
                        newValues.put("phone", univ.getString("telephone"));
                        newValues.put("address", univ.getString("address"));

                        db.update("university_table", newValues, "id = " + Long.toString(Id), null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readInfo(UniData data, SQLiteDatabase db, long Id) {

            Cursor cursor = db.query("university_table",
                    new String[] {"id", "isloaded", "isfavourite", "address", "phone", "site"},
                    "id = " + Long.toString(Id),
                    null, null, null, null);

            cursor.moveToFirst();

            int siteIdx  = cursor.getColumnIndex("site");
            int phoneIdx = cursor.getColumnIndex("phone");
            int adrIdx   = cursor.getColumnIndex("address");

            data.setPhone(parsePhone(cursor.getString(phoneIdx)));
            data.setAddress(cursor.getString(adrIdx));
            data.setSite(cursor.getString(siteIdx));

            cursor.close();
        }

        private void initProgress() {
            progress = fragment.findViewById(R.id.progress);
            progress.setAlpha(1.0f);
        }

        private void showProgress() {
            progress.setAlpha(1.0f);
        }

        private void hideProgress() {
            Animator.animAlpha(progress, 150, View.INVISIBLE);
        }

        private String parsePhone(String phone) {
            return phone.replaceAll("Телефон приемной комиссии: ",
                    "\nПриемная комиссия:\n")
                    .replaceAll(" - ", "-");
        }
    }
}