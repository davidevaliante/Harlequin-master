package com.finder.harlequinapp.valiante.harlequin;

public class DynamicData {

    //Tutti inizializzati a Zero
    private Integer like=0, maLike=0, fLike=0, eLike=0, sLike=0, nLike=0, age=0;
    private Float price= 0.0f;
    private Long date=0L;
    private String eName, iPath, pName;
    private Boolean isFree=true;


    public DynamicData() {

    }


    public DynamicData(Integer like,        //numero like totali
                       Integer maLike,       //numero like uomini
                       Integer fLike,       //numero like donne
                       Integer eLike,       //numero like impegnati
                       Integer sLike,       //numero like singles
                       Integer age,         //età totale
                       Long date,           //data in formato millisec
                       Float price,         //prezzo d'ingresso
                       String eName,        //nome evento
                       String iPath,        //URL immagine
                       String pName,        //nome del luogo
                       Boolean isFree,      //gratuito/ a pagamento
                       Integer nLike)       //like negativi per l'ordinamento
    {
        this.like=like;
        this.maLike=maLike;
        this.fLike=fLike;
        this.eLike=eLike;
        this.sLike=sLike;
        this.age=age;
        this.date=date;
        this.price=price;
        this.eName=eName;
        this.iPath=iPath;
        this.pName=pName;
        this.isFree=isFree;
        this.nLike=nLike;
    }

    //GETTERS AND SETTERS


    public Integer getMaLike() {
        return maLike;
    }

    public void setMaLike(Integer maLike) {
        this.maLike = maLike;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Integer geteLike() {
        return eLike;
    }

    public void seteLike(Integer eLike) {
        this.eLike = eLike;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public Integer getfLike() {
        return fLike;
    }

    public void setfLike(Integer fLike) {
        this.fLike = fLike;
    }

    public String getiPath() {
        return iPath;
    }

    public void setiPath(String iPath) {
        this.iPath = iPath;
    }

    public Boolean getFree() {
        return isFree;
    }

    public void setFree(Boolean free) {
        isFree = free;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public Integer getnLike() {
        return nLike;
    }

    public void setnLike(Integer nLike) {
        this.nLike = nLike;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getsLike() {
        return sLike;
    }

    public void setsLike(Integer sLike) {
        this.sLike = sLike;
    }


}
