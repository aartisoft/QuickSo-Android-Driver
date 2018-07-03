package com.apporio.demotaxiappdriver;

/**
 * Created by lenovo on 3/26/2018.
 */

public class GetAddressResponse {

    /**
     * result : 1
     * msg : Address
     * details : 68, Plaza St, Block S, Uppal Southend, Sector 49, Gurugram, Haryana 122018, India
     * city_name : Gurugram
     */

    private int result;
    private String msg;
    private String details;
    private String city_name;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

}
