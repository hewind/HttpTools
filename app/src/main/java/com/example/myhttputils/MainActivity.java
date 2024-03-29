package com.example.myhttputils;


import android.app.Activity;
import android.os.Bundle;
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
        params.put("game_id","1394628137031");
        params.put("role_id","123");
        params.put("role_name","roleName");
        params.put("role_level",99);
        params.put("party_name","丐帮");
        params.put("balance","0");
        params.put("vip_level",99);
        params.put("role_create_time","1642413612");
        params.put("client_type","1");

        //新加字段
        params.put("gameVersion","27");
        params.put("gameUid","494001757633190");
        params.put("cmbiChannelId", "2010041002");
        params.put("deviceID","04:79:70:63:77:1A\u0002");
        params.put("networkIP", "172.25.34.3");
        //添加header
        params.setHeader("Secret-Key","MUCVC-gdPpvhCtB.qKv8gaFi");
        HttpRequest httpRequest = new HttpRequest(this);
        httpRequest.setConnectType(HttpRequest.GET);
        httpRequest.setIsRetry(true);
        httpRequest.sendRequest(null, "url", new OnHttpRequestListener() {
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
        httpRequest.setConnectType(HttpRequest.GET);
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
