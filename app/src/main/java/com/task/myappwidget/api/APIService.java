package com.task.myappwidget.api;

import com.task.myappwidget.model.ChallengeResponse;


import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
/**
 * Created by rajeesh on 4/3/17.
 */
public interface APIService {
    String ENDPOINT = "https://www.hackerearth.com/";

    @GET("api/events/upcoming")
    Observable<ChallengeResponse> getChallenge();

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            . addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

}
