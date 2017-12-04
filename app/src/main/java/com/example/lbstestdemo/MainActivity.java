package com.example.lbstestdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;

    private int i=1,y=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();


        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
            permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
            permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
            permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions,1);
        }else {
            requestLocation();
        }
    }


    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只使用传感器定位
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);//得到详细信息
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for (int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void navigateTo(BDLocation bdLocation){
        if(isFirstLocate){
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
            MapStatus newMapStatus = new MapStatus.Builder().target(ll).zoom(16f).build();
            MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(newMapStatus);
            baiduMap.setMapStatus(update);
//            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
//            baiduMap.animateMapStatus(update);
//            update = MapStatusUpdateFactory.zoomTo(16f);
//            baiduMap.animateMapStatus(update);
//            baiduMap.animateMapStatus(update);//以动画方式更新
//            StringBuilder currentPosition = new StringBuilder();
//            currentPosition.append("省test11111111111： ").append(location.getProvince()).append("\n");
//            System.out.print(currentPosition);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度： ").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经线： ").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
//            currentPosition.append("门牌号：").append(bdLocation.getStreetNumber()).append("\n");
            currentPosition.append("定位方式: ");
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("TypeGpsLocation" + i);
                i++;
            }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络TypeNetWorkLocation"+ i);
                i++;
            }else if(bdLocation.getLocType() == BDLocation.GPS_RECTIFY_OUTDOOR){
                currentPosition.append("GPS_RECTIFY_OUTDOOR" + i);
                i++;
            } else {
                currentPosition.append("未知" + i);
                i++;
            }
            if((bdLocation.getLocType() == BDLocation.TypeGpsLocation) ||
                    (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation)){
                currentPosition.append("\n").append("地图定位入口：").append(y).append("\n");
                navigateTo(bdLocation);//不以动画更新，整个过程更新一次地图即可。
                y++;
            }
            positionText.setText(currentPosition);
        }
    }
}
