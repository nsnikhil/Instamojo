package com.nrs.nsnik.instamojo.interfaces;

import com.nrs.nsnik.instamojo.Objects.AccessToken;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetroFitCalls {

    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AccessToken> getAuthToken(@FieldMap Map<String, String> params);
}
