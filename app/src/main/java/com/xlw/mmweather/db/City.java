package com.xlw.mmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by xmydeveloper on 2017/9/28/0028.
 */
public class City extends DataSupport {
    private int id;
    private int cityCode;
    private String cityName;
    private int provinceId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }


}
