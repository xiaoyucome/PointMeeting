package github.lxy.point.meeting;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity implements AMap.OnMarkerClickListener, AMap.OnMapClickListener, AMap.OnCameraChangeListener {
    private AMap aMap;
    private int width, height;// 屏幕高度(px)
    private ArrayList<MarkerOptions> visibleMarkerOptions = new ArrayList<MarkerOptions>();// 视野内的marker
    private ArrayList<MarkerOptions> mMakerOptionsList = new ArrayList<MarkerOptions>();//聚合时用

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
        drawCars();
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
        for (int i = 0; i < 500; i++) {
            LatLng latLng = new LatLng(Math.random() * 6 + 39,
                    Math.random() * 6 + 116);
            MarkerOptions option = new MarkerOptions();
            option.title(String.valueOf(latLng)).position(latLng).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMakerOptionsList.add(option);
            aMap.addMarker(option);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
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
