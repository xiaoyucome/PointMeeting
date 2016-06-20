package github.lxy.point.meeting;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * 聚合类
 */
public class MarkerCluster {
    private Activity activity;
    private MarkerOptions options;//聚合块的option
    private ArrayList<MarkerOptions> clusterOptions;//每一个块都是存放MarkerOptions的集合
    private LatLngBounds bounds;// 创建区域
    private MarkerOptions clusterOption;

    /**
     * 创建块
     *
     * @param activity
     * @param markerOptions
     * @param projection
     * @param gridSize      区域大小参数
     */
    public MarkerCluster(Activity activity, MarkerOptions markerOptions,
                         Projection projection, int gridSize) {
        options = new MarkerOptions();
        this.activity = activity;
        Point point = projection.toScreenLocation(markerOptions.getPosition());
        Point southwestPoint = new Point(point.x - gridSize, point.y + gridSize);
        Point northeastPoint = new Point(point.x + gridSize, point.y - gridSize);
        bounds = new LatLngBounds(
                projection.fromScreenLocation(southwestPoint),
                projection.fromScreenLocation(northeastPoint));
        options.title(markerOptions.getTitle())
                .position(markerOptions.getPosition())
                .icon(markerOptions.getIcon());
        clusterOptions = new ArrayList<MarkerOptions>();
        clusterOptions.add(options);
    }

    /**
     * 添加marker
     */
    public void addMarker(MarkerOptions markerOptions) {
        clusterOptions.add(markerOptions);// 向块中添加option
    }

    /**
     * 设置聚合点的中心位置以及图标
     */
    public MarkerOptions setPositionAndIcon() {
        int size = clusterOptions.size();
        if (size == 1) {
            return options;
        }
        double lat = 0.0;
        double lng = 0.0;
        for (MarkerOptions position : clusterOptions) {
            lat += position.getPosition().latitude;
            lng += position.getPosition().longitude;
        }
        clusterOption = new MarkerOptions();
        clusterOption.position(new LatLng(lat / size, lng / size));// 设置中心位置为聚集点的平均距离
        clusterOption.title("聚合点");
        clusterOption.icon(BitmapDescriptorFactory
                .fromBitmap(getViewBitmap(getView(size,
                        R.mipmap.icon_marker_cluster))));
        return clusterOption;
    }

    public ArrayList<MarkerOptions> clusterOptinos() {
        return clusterOptions;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }

    public MarkerOptions getOptions() {
        return clusterOption;
    }

    public void setOptions(MarkerOptions options) {
        this.options = options;
    }

    public View getView(int carNum, int resourceId) {
        View view = activity.getLayoutInflater().inflate(
                R.layout.monitor_cluster_view, null);
        TextView carNumTextView = (TextView) view.findViewById(R.id.my_car_num);
        RelativeLayout myCarLayout = (RelativeLayout) view
                .findViewById(R.id.my_car_bg);
        myCarLayout.setBackgroundResource(resourceId);
        carNumTextView.setText(String.valueOf(carNum));
        return view;
    }

    /**
     * 把一个view转化成bitmap对象
     */
    public static Bitmap getViewBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }
}
