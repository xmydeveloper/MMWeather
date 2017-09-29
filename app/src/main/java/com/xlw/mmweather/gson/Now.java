package com.xlw.mmweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xmydeveloper on 2017/9/29/0029.
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;


    public class More {
        @SerializedName("txt")
        public String info;
    }
}
