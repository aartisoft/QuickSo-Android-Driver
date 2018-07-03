package com.apporio.demotaxiappdriver.newmodels;

import java.util.List;

/**
 * Created by Bhuvneshwar on 7/10/2017.
 */

public class WeeklyEarningModel {


    /**
     * result : 1
     * msg : Weekly Earning
     * out_standing_active_status : 2
     * fare_recevied : 3
     * company_cut : 0.30
     * total_payment_eraned : 0
     * weekly_amount : 2.7
     * company_payment : 0
     * driver_payment :
     * total_rides : 3
     * details : [{"date":"18-06-2018","day":"Monday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-18"}},{"date":"19-06-2018","day":"Tuesday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-19"}},{"date":"20-06-2018","day":"Wednesday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-20"}},{"date":"21-06-2018","day":"Thursday","detail":{"driver_earning_id":"8","driver_id":"5","total_amount":"3","rides":"3","amount":"2.7","outstanding_amount":"-1.7","ride_mode":"1","date":"2018-06-21"}},{"date":"22-06-2018","day":"Friday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-22"}},{"date":"23-06-2018","day":"Saturday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-23"}},{"date":"24-06-2018","day":"Sunday","detail":{"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-24"}}]
     */

    private int result;
    private String msg;
    private String out_standing_active_status;
    private String fare_recevied;
    private String company_cut;
    private String total_payment_eraned;
    private double weekly_amount;
    private String company_payment;
    private String driver_payment;
    private int total_rides;
    private List<DetailsBean> details;

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

    public String getOut_standing_active_status() {
        return out_standing_active_status;
    }

    public void setOut_standing_active_status(String out_standing_active_status) {
        this.out_standing_active_status = out_standing_active_status;
    }

    public String getFare_recevied() {
        return fare_recevied;
    }

    public void setFare_recevied(String fare_recevied) {
        this.fare_recevied = fare_recevied;
    }

    public String getCompany_cut() {
        return company_cut;
    }

    public void setCompany_cut(String company_cut) {
        this.company_cut = company_cut;
    }

    public String getTotal_payment_eraned() {
        return total_payment_eraned;
    }

    public void setTotal_payment_eraned(String total_payment_eraned) {
        this.total_payment_eraned = total_payment_eraned;
    }

    public double getWeekly_amount() {
        return weekly_amount;
    }

    public void setWeekly_amount(double weekly_amount) {
        this.weekly_amount = weekly_amount;
    }

    public String getCompany_payment() {
        return company_payment;
    }

    public void setCompany_payment(String company_payment) {
        this.company_payment = company_payment;
    }

    public String getDriver_payment() {
        return driver_payment;
    }

    public void setDriver_payment(String driver_payment) {
        this.driver_payment = driver_payment;
    }

    public int getTotal_rides() {
        return total_rides;
    }

    public void setTotal_rides(int total_rides) {
        this.total_rides = total_rides;
    }

    public List<DetailsBean> getDetails() {
        return details;
    }

    public void setDetails(List<DetailsBean> details) {
        this.details = details;
    }

    public static class DetailsBean {
        /**
         * date : 18-06-2018
         * day : Monday
         * detail : {"driver_earning_id":"0","driver_id":"5","total_amount":"0","rides":"0","amount":"0","date":"2018-06-18"}
         */

        private String date;
        private String day;
        private DetailBean detail;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            /**
             * driver_earning_id : 0
             * driver_id : 5
             * total_amount : 0
             * rides : 0
             * amount : 0
             * date : 2018-06-18
             */

            private String driver_earning_id;
            private String driver_id;
            private String total_amount;
            private String rides;
            private String amount;
            private String date;

            public String getDriver_earning_id() {
                return driver_earning_id;
            }

            public void setDriver_earning_id(String driver_earning_id) {
                this.driver_earning_id = driver_earning_id;
            }

            public String getDriver_id() {
                return driver_id;
            }

            public void setDriver_id(String driver_id) {
                this.driver_id = driver_id;
            }

            public String getTotal_amount() {
                return total_amount;
            }

            public void setTotal_amount(String total_amount) {
                this.total_amount = total_amount;
            }

            public String getRides() {
                return rides;
            }

            public void setRides(String rides) {
                this.rides = rides;
            }

            public String getAmount() {
                return amount;
            }

            public void setAmount(String amount) {
                this.amount = amount;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }
        }
    }
}