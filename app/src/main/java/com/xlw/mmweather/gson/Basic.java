package com.xlw.mmweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xmydeveloper on 2017/9/29/0029.
 */
public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("update")
    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
