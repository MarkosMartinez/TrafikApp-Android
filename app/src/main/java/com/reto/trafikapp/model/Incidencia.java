package com.reto.trafikapp.model;

public class Incidencia {
    private String incidenceId;
    private int sourceId;
    private String incidenceType;
    private String province;
    private String cause;
    private String cityTown;
    private String startDate;
    private String road;
    private String pkStart;
    private String pkEnd;
    private String direction;
    private Double latitude;
    private Double longitude;
    private Boolean creada;

    public Incidencia(String incidenceId, int sourceId, String incidenceType, String province, String cause, String cityTown, String startDate, String road, String pkStart, String pkEnd, String direction, Double latitude, Double longitude, Boolean creada) {
        this.incidenceId = incidenceId;
        this.sourceId = sourceId;
        this.incidenceType = incidenceType;
        this.province = province;
        this.cause = cause;
        this.cityTown = cityTown;
        this.startDate = startDate;
        this.road = road;
        this.pkStart = pkStart;
        this.pkEnd = pkEnd;
        this.direction = direction;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creada = creada;
    }

    public Incidencia() {

    }

    public String getIncidenceId() {
        return incidenceId;
    }

    public void setIncidenceId(String incidenceId) {
        this.incidenceId = incidenceId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getIncidenceType() {
        return incidenceType;
    }

    public void setIncidenceType(String incidenceType) {
        this.incidenceType = incidenceType;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getCityTown() {
        return cityTown;
    }

    public void setCityTown(String cityTown) {
        this.cityTown = cityTown;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getPkStart() {
        return pkStart;
    }

    public void setPkStart(String pkStart) {
        this.pkStart = pkStart;
    }

    public String getPkEnd() {
        return pkEnd;
    }

    public void setPkEnd(String pkEnd) {
        this.pkEnd = pkEnd;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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

    public Boolean getCreada() {
        return creada;
    }

    public void setCreada(Boolean creada) {
        this.creada = creada;
    }
}
