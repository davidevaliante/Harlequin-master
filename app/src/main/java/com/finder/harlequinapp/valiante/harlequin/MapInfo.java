package com.finder.harlequinapp.valiante.harlequin;

public class MapInfo {

    private Double lat,lng;
    private String pName, id, eName, phone,adress, referenceKey;
    private Float price=0.0f;
    private Integer likes = 0;
    private Long time=0L;
    private Integer totalAge = 0;


    public MapInfo(){

    }

    public MapInfo(Double lat,Double lng, String pName,String id, String eName,Float price, String phone,Integer likes,Long time,String adress, String referenceKey, Integer totalAge){
        this.lat = lat;
        this.lng = lng;
        this.pName = pName;
        this.id=id;
        this.eName=eName;
        this.price=price;
        this.phone=phone;
        this.likes=likes;
        this.time=time;
        this.adress=adress;
        this.referenceKey=referenceKey;
        this.totalAge=totalAge;
    }

    public Integer getTotalAge() {
        return totalAge;
    }

    public void setTotalAge(Integer totalAge) {
        this.totalAge = totalAge;
    }

    public String getReferenceKey() {
        return referenceKey;
    }

    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public Double getLat() {
        return lat;
    }



    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }
}

