package com.zuel.syzc.spark.kit;

/**
 * 基站点
 * @author zongjunhao
 */
@SuppressWarnings("unused")
public class BaseStationPoint {
    private double longitude;
    private double latitude;
    private String laci;
    public boolean founded = false;

    public BaseStationPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public BaseStationPoint(double longitude, double latitude, String laci) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.laci = laci;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLaci() {
        return laci;
    }

    public void setLaci(String laci) {
        this.laci = laci;
    }

    @Override
    public String toString() {
        return "Point{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", laci='" + laci + '\'' +
                ", founded=" + founded +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseStationPoint other = (BaseStationPoint) obj;
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        return Double.doubleToLongBits(latitude) == Double.doubleToLongBits(other.latitude);
    }

}
