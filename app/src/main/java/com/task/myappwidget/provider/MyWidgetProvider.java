package com.task.myappwidget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


import com.squareup.picasso.Picasso;
import com.task.myappwidget.R;
import com.task.myappwidget.myutils.AppWidgetAlarm;
import com.task.myappwidget.myutils.Utils;
import com.task.myappwidget.api.APIService;
import com.task.myappwidget.model.ChallengeResponse;
import com.task.myappwidget.model.Challenge;

import java.util.ArrayList;
import java.util.List;


import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
* Created by rajeesh on 3/3/17.
        */

public class MyWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MyWidgetProvider.class.getSimpleName();
    public static int[] AppWidgetIds;
    public static List<Challenge> challegeResponse = new ArrayList<>();
    public static List<Challenge> challegeList;
    public static int index=0;
    public String cType="LIVE";
    Subscription mSubscription;
    @Override
    public void onReceive(final Context context, Intent widgetIntent) {
        final String action = widgetIntent.getAction();
        Log.d(TAG, "Response: " + action);
        super.onReceive(context, widgetIntent);
        if (action.equals(Utils.ACTION_LIVE_CLICK)){
            index=0;
            cType="LIVE";
            updateWidget( context,"LIVE");
        }else if(action.equals(Utils.ACTION_UPCOMING_CLICK)){
            index=0;
            cType="UPCOMING";
            updateWidget( context,"UPCOMING");
        }else if(action.equals(Utils.ACTION_NEXT_CLICK)){
            index=index+1;
            updateWidget( context,"NEXT");
        }else if(action.equals(Utils.ACTION_PREV_CLICK)) {
            if (index > 0)
                index = index - 1;
            updateWidget(context, "PREV");
        }else if(action.equals(Utils.ACTION_AUTO_UPDATE)){
            fetchRemoteData(context);
        }else if(action.equals(Utils.ACTION_LOAD_URL)){
            String url = challegeList.get(index).getUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        }

    }

    @Override
    public void onEnabled(Context context)
    {
        // start alarm
        AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
        appWidgetAlarm.startAlarm();
    }

    @Override
    public void onDisabled(Context context)
    {
        // TODO: alarm should be stopped only if all widgets has been disabled

        // stop alarm
        AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
        appWidgetAlarm.stopAlarm();
    }
    private List<Challenge> getLiveChallenge(List<Challenge> challenge){
        List<Challenge> challengeList = new ArrayList<>();
        for(Challenge obj:challenge){
            if(obj.getStatus().equals("ONGOING") && !obj.getCollege()){
                challengeList.add(obj);
            }

        }
        return challengeList;

    }
    private List<Challenge> getUpcomingChallenge(List<Challenge> challenge){
        List<Challenge> challengeList = new ArrayList<>();
        for(Challenge obj:challenge){
            if(obj.getStatus().equals("UPCOMING")){
                challengeList.add(obj);
            }

        }
        return challengeList;

    }

    private void updateWidget(final Context context, final String challengeType) {

        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        if(challengeType.equals("LIVE")) {
            challegeList =  new ArrayList<>();
            challegeList = getLiveChallenge(challegeResponse);
            updateViews.setInt(R.id.btnLive, "setBackgroundColor", Color.RED);
            updateViews.setInt(R.id.btnUpcoming, "setBackgroundColor", Color.BLACK);
            updateViews.setTextViewText(R.id.btnLive,"Live ["+challegeList.size()+"]");
            updateViews.setTextViewText(R.id.btnStart,"START NOW");
            updateViews.setTextViewText(R.id.timeStamp,"END ON :"+challegeList.get(index).getEnd_timestamp());

        }else  if(challengeType.equals("UPCOMING")) {
            challegeList =  new ArrayList<>();
            challegeList= getUpcomingChallenge(challegeResponse);
            updateViews.setInt(R.id.btnLive, "setBackgroundColor", Color.BLACK );
            updateViews.setInt(R.id.btnUpcoming, "setBackgroundColor",Color.RED);
            updateViews.setTextViewText(R.id.btnUpcoming,"Upcoming ["+challegeList.size()+"]");
            updateViews.setTextViewText(R.id.btnStart,"REGISTER NOW");
            updateViews.setTextViewText(R.id.timeStamp,"START ON :"+challegeList.get(index).getStart_timestamp());
        }
        if(index>=challegeList.size()-1){
            index=challegeList.size()-1;
            updateViews.setViewVisibility(R.id.imgNext, View.GONE);
            updateViews.setViewVisibility(R.id.imgPrev, View.VISIBLE);
        }else if(index==0){
            updateViews.setViewVisibility(R.id.imgPrev, View.GONE);
            updateViews.setViewVisibility(R.id.imgNext, View.VISIBLE);
        }else{
            updateViews.setViewVisibility(R.id.imgNext, View.VISIBLE);
            updateViews.setViewVisibility(R.id.imgPrev, View.VISIBLE);
        }
        updateViews.setTextViewText(R.id.txtCount,"["+String.valueOf(index+1)+"/"+challegeList.size()+"]");
        updateViews.setTextViewText(R.id.title,challegeList.get(index).getTitle());
        updateViews.setTextViewText(R.id.description,challegeList.get(index).getDescription());
        updateViews.setTextViewText(R.id.challenge,"Hacker Earth-"+challegeList.get(index).getChallenge_type());

        Log.d(TAG, "thumbnail Response: " + challegeList.get(index).getThumbnail());
        String imgPath=challegeList.get(index).getThumbnail();
        if(imgPath!=null) {
            Picasso.with(context)
                    .load(imgPath)
                    .error(R.drawable.ic_launcher)
                    .into(updateViews, R.id.imageView, AppWidgetIds);
        }


        updateViews.setOnClickPendingIntent(R.id.btnLive,
                getPendingSelfIntent(context,
                        Utils.ACTION_LIVE_CLICK));
        updateViews.setOnClickPendingIntent(R.id.btnUpcoming,
                getPendingSelfIntent(context,
                        Utils.ACTION_UPCOMING_CLICK));
        updateViews.setOnClickPendingIntent(R.id.imgNext,
                getPendingSelfIntent(context,
                        Utils.ACTION_NEXT_CLICK));
        updateViews.setOnClickPendingIntent(R.id.imgPrev,
                getPendingSelfIntent(context,
                        Utils.ACTION_PREV_CLICK));
        updateViews.setOnClickPendingIntent(R.id.btnStart,
                getPendingSelfIntent(context,
                        Utils.ACTION_LOAD_URL));

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, MyWidgetProvider.class);
        mgr.updateAppWidget(cn, updateViews);



    }

    public void fetchRemoteData(final Context context) {
        APIService widgetService = APIService.retrofit.create(APIService.class);

        mSubscription =  widgetService.getChallenge()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ChallengeResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "SUCCESS");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(ChallengeResponse challengeResponse) {
                        if(challengeResponse.getResponse()!=null) {

                            challegeResponse=challengeResponse.getResponse();

                                  updateWidget(context, cType);
                           }


                    }
                });


    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "updating app widget");
        AppWidgetIds=appWidgetIds;
        fetchRemoteData(context);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    private PendingIntent getPendingSelfIntent(Context context, String action) {
        // An explicit intent directed at the current class (the "self").
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}
