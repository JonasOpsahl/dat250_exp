package com.exp2.api.model;

public class VoteOption {
    
    private String caption;
    private Integer presentationOrder;

    public VoteOption() {

    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return this.caption;
    }

    public Integer getPresentationOrder() {
        return this.presentationOrder;
    }

    public void setPresentationOrder(Integer presentationOrder) {
        this.presentationOrder = presentationOrder;
    }

}
