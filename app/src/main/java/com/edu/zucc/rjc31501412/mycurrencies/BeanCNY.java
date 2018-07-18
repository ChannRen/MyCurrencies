package com.edu.zucc.rjc31501412.mycurrencies;

public class BeanCNY {
    private String forCurrency;
    private String bid;//买入
    private String ask;//卖出

    public BeanCNY(String forCurrency, String bid, String ask) {
        this.forCurrency = forCurrency;
        this.bid = bid;
        this.ask = ask;
    }

    public String getForCurrency() {
        return forCurrency;
    }

    public void setForCurrency(String forCurrency) {
        this.forCurrency = forCurrency;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }
}
