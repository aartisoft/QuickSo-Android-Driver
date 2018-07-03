package com.apporio.demotaxiappdriver.wallet;

public class CardSessionEvent {

    public String card_id;


    public CardSessionEvent(String card_id) {
        this.card_id = card_id;
    }

    public CardSessionEvent() {

    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

}
