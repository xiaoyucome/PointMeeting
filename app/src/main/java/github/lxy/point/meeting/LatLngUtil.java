package github.lxy.point.meeting;

import com.amap.api.maps.model.LatLng;

import java.util.List;

public class LatLngUtil {
    public static LatLng nEPos(LatLng ne, LatLng sw) {
        double neLatitude = ne.latitude;
        double neLongitude = ne.longitude;
        double swLatitude = sw.latitude;
        double swLongitude = sw.longitude;
        return new LatLng((neLatitude > swLatitude ? neLatitude : swLatitude), (neLongitude > swLongitude ? neLongitude : swLongitude));
    }

    public static LatLng sWPos(LatLng ne, LatLng sw) {
        double neLatitude = ne.latitude;
        double neLongitude = ne.longitude;
        double swLatitude = sw.latitude;
        double swLongitude = sw.longitude;
        return new LatLng((neLatitude > swLatitude ? swLatitude : neLatitude), (neLongitude > swLongitude ? swLongitude : neLongitude));
    }

    /**
     * 取得西南角坐标
     */
    public static LatLng getSWPoint(List<MarkerBean> beans) {
        double lat = 0, lng = 0;
        if (beans != null) {
            for (MarkerBean item : beans) {
                double latitude = Double.valueOf(item.lat);// 纬度
                double longitude = Double.valueOf(item.lng);// 经度
                if (lat == 0 && lng == 0) {
                    lat = latitude;
                    lng = longitude;
                }
                if (latitude <= lat) {
                    lat = latitude;
                }
                if (longitude <= lng) {
                    lng = longitude;
                }
            }
        }
        return new LatLng(lat, lng);
    }

    /**
     * 取得东北角坐标
     */
    public static LatLng getNEPoint(List<MarkerBean> beans) {
        double lat = 0, lng = 0;
        if (beans != null) {
            for (MarkerBean item : beans) {
                double latitude = Double.valueOf(item.lat);// 纬度
                double longitude = Double.valueOf(item.lng);// 经度
                if (lat == 0 && lng == 0) {
                    lat = latitude;
                    lng = longitude;
                }
                if (latitude >= lat) {
                    lat = latitude;
                }
                if (longitude >= lng) {
                    lng = longitude;
                }
            }
        }
        return new LatLng(lat, lng);
    }
}
