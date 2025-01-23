package com.reto.trafikapp.model;

import java.io.Serializable;

public class Camara implements Serializable {
    private int id;
    private int cameraId;
    private int sourceId;
    private String cameraName;
    private String urlImage;
    private Double latitude;
    private Double longitude;
    private String road;
    private String kilometer;
    private String address;

    public Camara(int id, int cameraId, int sourceId, String cameraName, String urlImage, Double latitude, Double longitude, String road, String kilometer, String address) {
        this.id = id;
        this.cameraId = cameraId;
        this.sourceId = sourceId;
        this.cameraName = cameraName;
        this.urlImage = urlImage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.road = road;
        this.kilometer = kilometer;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getKilometer() {
        return kilometer;
    }

    public void setKilometer(String kilometer) {
        this.kilometer = kilometer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
