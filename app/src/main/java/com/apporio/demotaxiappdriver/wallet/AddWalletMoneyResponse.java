package com.apporio.demotaxiappdriver.wallet;

public class AddWalletMoneyResponse {


    /**
     * result : 1
     * msg : Wallent Money Added Successfully!!
     * payment_id : ch_1AcXjuIDsGis6xJF1wAQvypi
     */

    private int result;
    private String msg;
    private String payment_id;

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

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }
}

