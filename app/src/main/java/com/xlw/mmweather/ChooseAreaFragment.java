package com.xlw.mmweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xlw.mmweather.db.City;
import com.xlw.mmweather.db.County;
import com.xlw.mmweather.db.Province;
import com.xlw.mmweather.util.HttpUtils;
import com.xlw.mmweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xmydeveloper on 2017/9/28/0028.
 */
public class ChooseAreaFragment extends Fragment {

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private View view;
    private ListView listView;
    private Button mBack;
    private TextView mTittle;
    private List<String> dataList = new ArrayList<>();

    /**
     * 当前选中级别
     */

    private int currentLevel;

    /**
     * 省列表
     */

    private List<Province> provinceList;

    /**
     * 市列表
     */

    private List<City> cityList;


    /**
     * 县列表
     */

    private List<County> countyList;


    /**
     * 选中的省
     */

    private Province selectedProvince;

    /**
     * 选中的市
     */

    private City selectedCity;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.choose_area, container, false);
        mBack = (Button) view.findViewById(R.id.back_button);
        mTittle = (TextView) view.findViewById(R.id.tittle_text);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    quryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    quryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(i).getWeatherId();

                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();

                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }


                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    quryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    quryProvices();
                }
            }
        });

        quryProvices();

    }

    //TODO  查询所有省
    private void quryProvices() {
        mTittle.setText("中国");
        mBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        //数据库里面数据不为空
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province provice : provinceList) {
                dataList.add(provice.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_PROVINCE;
        } else {
            //数据库里面数据为空，去服务器获取
            String address = "http://guolin.tech/api/china";
            quryFromServer(address, "province");
        }

    }


    //TODO  查询所选省下面所有的城市
    private void quryCities() {
        mTittle.setText(selectedProvince.getProvinceName());
        mBack.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();

            String address = "http://guolin.tech/api/china/" + provinceCode;
            quryFromServer(address, "city");
        }

    }

    //TODO  查询所选市下面所有的县
    private void quryCounties() {
        mTittle.setText(selectedCity.getCityName());
        mBack.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();

            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;

            quryFromServer(address, "county");

        }


    }

    //TODO  从服务器获取数据
    private void quryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtils.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                quryProvices();
                            } else if ("city".equals(type)) {
                                quryCities();
                            } else if ("county".equals(type)) {
                                quryCounties();
                            }

                        }
                    });
                }


            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });


    }

    /**
     * 关闭dialog
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }


    /**
     * 打开dialog
     */
    private void showProgressDialog() {
        if (progressDialog != null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

}
