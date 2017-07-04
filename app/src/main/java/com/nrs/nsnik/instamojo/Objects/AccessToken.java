package com.nrs.nsnik.instamojo.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nikhil on 04-Jul-17.
 */

public class AccessToken {

    @SerializedName("access_token")
    public String access_token;

    @SerializedName("expires_in")
    public int expires_in;

    @SerializedName("token_type")
    public String token_type;

    @SerializedName("scope")
    public String scope;
}
