package com.task.myappwidget.model;

import java.util.List;
/**
 * Created by rajeesh on 3/3/17.
 */
public class ChallengeResponse {
    public List<Challenge> getResponse() {
        return response;
    }

    public void setResponse(List<Challenge> response) {
        this.response = response;
    }

    private List<Challenge> response;


}
