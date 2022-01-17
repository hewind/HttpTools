package com.example.myhttputils;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.common.tools.http.HttpRequest;
import com.common.tools.http.ParamsBuild;
import com.common.tools.http.itl.OnHttpRequestListener;
import com.common.tools.http.utils.HttpLog;

public class MainActivity extends Activity {

    private Button btn_http;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_http = findViewById(R.id.btn_http);
        content = findViewById(R.id.content);

        btn_http.setOnClickListener(view -> {
            postTest2();
        });
    }

    /**
     * post连接测试
     */
    private void postTest() {
        ParamsBuild params = ParamsBuild.build();
        params.put("group_id","710");
        params.put("game_id","1394628137031");
        params.put("role_id","123");
        params.put("role_name","ddd");
        params.put("role_level","99");
        params.put("party_name","丐帮");
        params.put("balance","0");
        params.put("vip_level","1");
        params.put("role_create_time",System.currentTimeMillis()+"");
        params.put("client_type","1");
        //添加header
        params.setHeader("Secret-Key","MUCVC-gdPpvhCtB.qKv8gaFi");
        HttpRequest httpRequest = new HttpRequest(this);
        httpRequest.sendRequest(params, "http://121.4.214.235:9528/services/app.tlbb-mobile/report/requests", new OnHttpRequestListener() {
            @Override
            public void onStart() {
                HttpLog.log().i("onStart");
            }
            @Override
            public void onSuccess(String result) {
                HttpLog.log().i("onSuccess result = "+result);
            }
            @Override
            public void onError(String msg) {
                HttpLog.log().i("onError msg = "+msg);
            }
        });
    }



    private void postTest2() {
        String appCode = "f5f614fa5d324bd785a46f8bfe9c93a5";
        ParamsBuild params = ParamsBuild.build();
        params.put("ip","218.18.228.178");
        //添加header
        params.setHeader("Authorization", "APPCODE " + appCode);
        //创建一个request
        HttpRequest httpRequest = new HttpRequest(this);
        httpRequest.setConnectType(HttpRequest.CONNECT_TYPE_GET);
        httpRequest.sendRequest(params, "http://api01.aliyun.venuscn.com/ip", new OnHttpRequestListener() {
            @Override
            public void onStart() {
                HttpLog.log().i("onStart");
            }
            @Override
            public void onSuccess(String result) {
                HttpLog.log().i("onSuccess result = "+result);
            }
            @Override
            public void onError(String msg) {
                HttpLog.log().i("onError msg = "+msg);
            }
        });
    }
}