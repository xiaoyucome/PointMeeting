package github.lxy.point.meeting;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity implements AMap.OnMarkerClickListener, AMap.OnMapClickListener, AMap.OnCameraChangeListener {
    private AMap aMap;
    private float curZoom, mLastZoom;
    private int width, height;// 屏幕高度(px)
    private static final float MINSCOPE = 0.0f;
    private ArrayList<MarkerOptions> visibleMarkerOptions = new ArrayList<MarkerOptions>();// 视野内的marker
    private ArrayList<MarkerOptions> mMakerOptionsList = new ArrayList<MarkerOptions>();//聚合时用
    private ArrayList<MarkerBean> markerBeans = new ArrayList<MarkerBean>();// 视野内的marker
    private static final int GIRDSIZE = 70;
    private Marker preAddMarker, preClickMarker;

    @Bind(R.id.map)
    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMapView.onCreate(savedInstanceState);
        initMap();
        initViews();
        initData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawCars();
            }
        }).start();
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    private void initViews() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
    }

    private void initData() {
        for (int i = 0; i < 100; i++) {
            MarkerBean markerBean = new MarkerBean();
            double lat = Math.random() * 6 + 39;
            double lng = Math.random() * 6 + 116;
            markerBean.lat = lat;
            markerBean.lng = lng;
            markerBean.title = String.valueOf(i);
            markerBeans.add(markerBean);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 模拟添加多个marker
     */
    private void drawCars() {
        aMap.clear();
        mMakerOptionsList.clear();

        if (markerBeans != null) {
            for (int i = 0; i < markerBeans.size(); i++) {
                MarkerBean markerBean = markerBeans.get(i);
                LatLng latLng = new LatLng(markerBean.lat, markerBean.lng);
                MarkerOptions option = new MarkerOptions();
                option.title(markerBean.title).position(latLng);
                mMakerOptionsList.add(option);
                aMap.addMarker(option);
            }
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(LatLngUtil.getSWPoint(markerBeans), LatLngUtil.getNEPoint(markerBeans)), 100));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(LatLngUtil.getSWPoint(markerBeans), LatLngUtil.getNEPoint(markerBeans)), 500, 500, 100));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        curZoom = cameraPosition.zoom;
        if ((mLastZoom == MINSCOPE || mLastZoom != curZoom)) {
            resetMarker();
            resetMarks();
            mLastZoom = curZoom;
        }
    }

    private void resetMarker() {
        if (preAddMarker != null) {
            preAddMarker.remove();
            preClickMarker.setVisible(true);
            preAddMarker = null;
            preClickMarker = null;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        resetMarker();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if ((preClickMarker == null || !preClickMarker.getTitle().equals(marker.getTitle())) && (!"聚合点".equals(marker.getTitle()))) {
            if (preAddMarker != null) {
                preAddMarker.remove();
                preAddMarker = null;
                preClickMarker.setVisible(true);
            }
            preClickMarker = marker;
            marker.setVisible(false);
            LatLng clickLatLng = marker.getPosition();
            MarkerOptions clickOption = new MarkerOptions();
            clickOption.position(clickLatLng);
            changeMarkerIcon(marker, clickOption);
        }
        return true;
    }

    public void changeMarkerIcon(Marker marker, MarkerOptions clickOption) {
        for (int i = 0; i < markerBeans.size(); i++) {
            MarkerBean markerBean = markerBeans.get(i);
            if (marker.getTitle().equals(markerBean.title)) {
                clickOption.title(markerBean.title);
                clickOption.icon(markerSelected(markerBean.title, true));
                preAddMarker = aMap.addMarker(clickOption);
            }
        }
    }

    private void resetMarks() {
        Projection projection = aMap.getProjection();//Projection对象可以在屏幕坐标与经纬度坐标之间进行转换
        Point point = null;
        visibleMarkerOptions.clear();

        for (MarkerOptions option : mMakerOptionsList) {
            point = projection.toScreenLocation(option.getPosition());//返回一个从地图位置转换来的屏幕位置
            if (point.x < 0 || point.y < 0 || point.x > width || point.y > height) {
                // 不在屏幕上的Marker
            } else {
                visibleMarkerOptions.add(option);
            }
        }

        ArrayList<MarkerCluster> clustersMarkers = new ArrayList<MarkerCluster>();//总聚合块个数
        for (MarkerOptions option : visibleMarkerOptions) {
            if (clustersMarkers.size() == 0) {//总聚合块个数为0
                clustersMarkers.add(new MarkerCluster(MainActivity.this, option, projection, GIRDSIZE));// gridSize根据自己需求调整
            } else {
                boolean isIn = false;
                for (MarkerCluster cluster : clustersMarkers) {//遍历得到每一个聚合块
                    if (cluster.getBounds().contains(option.getPosition())) {
                        cluster.addMarker(option);//在区域内就加到该块中
                        isIn = true;
                        break;
                    }
                }

                //如果不在已知聚合块当中,就创建个新的聚合块
                if (!isIn) {
                    clustersMarkers.add(new MarkerCluster(MainActivity.this, option, projection, GIRDSIZE));
                }
            }
        }

        aMap.clear();
        for (MarkerCluster cluster : clustersMarkers) {
            MarkerOptions markerOptions = cluster.setPositionAndIcon();// 设置聚合块的位置和icon,返回块的option

            //画不在任何块中的单个Marker
            if (!"聚合点".equals(markerOptions.getTitle())) {
                drawClusterMarker(markerOptions);
            } else {
                //画聚合块
                aMap.addMarker(cluster.getOptions());
            }
        }
    }

    public void drawClusterMarker(MarkerOptions option) {
        for (int i = 0; i < markerBeans.size(); i++) {
            MarkerBean monitorBean = markerBeans.get(i);
            if (option.getTitle().equals(monitorBean.title)) {
                option.icon(markerSelected(monitorBean.title, false));
                aMap.addMarker(option);
            }
        }
    }

    public BitmapDescriptor markerSelected(String title, boolean isSelect) {
        View markerView = LayoutInflater.from(this).inflate(R.layout.layout_marker, null);
        ImageView marker = (ImageView) markerView.findViewById(R.id.marker);
        TextView vinTv = (TextView) markerView.findViewById(R.id.vin);
        marker.setBackgroundResource(isSelect ? R.mipmap.icon_marker_pressed : R.mipmap.icon_marker_default);
        vinTv.setBackgroundResource(isSelect ? R.mipmap.icon_marker_title_pressed : R.mipmap.icon_marker_title_default);
        vinTv.setText(title);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(markerView);
        return bitmapDescriptor;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}