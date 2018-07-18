package com.edu.zucc.rjc31501412.mycurrencies;

public class BeanRecord {
    private String forCode;
    private String homCode;
    private String forAmount;
    private String homAmount;
    private String time;
    public BeanRecord(){}
    public BeanRecord(String forCode,String forAmount,String homCode,String homAmount,String time)
    {
        this.forCode = forCode;
        this.homCode = homCode;
        this.homAmount = homAmount;
        this.forAmount = forAmount;
        this.time = time;
    }

    public String getForCode() {
        return forCode;
    }

    public void setForCode(String forCode) {
        this.forCode = forCode;
    }

    public String getHomCode() {
        return homCode;
    }

    public void setHomCode(String homCode) {
        this.homCode = homCode;
    }

    public String getForAmount() {
        return forAmount;
    }

    public void setForAmount(String forAmount) {
        this.forAmount = forAmount;
    }

    public String getHomAmount() {
        return homAmount;
    }

    public void setHomAmount(String homAmount) {
        this.homAmount = homAmount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
