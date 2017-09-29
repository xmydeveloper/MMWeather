package com.xlw.mmweather.gson;

/**
 * Created by xmydeveloper on 2017/9/29/0029.
 */
public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
