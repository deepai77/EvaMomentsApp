package com.cindura.evamomentsapp.model;

import java.io.Serializable;
import java.util.List;

//Class which represents the list of shows stored in local storage
public class PresentationList implements Serializable {
    private List<Presentation> presentationList;

    public List<Presentation> getPresentationList() {
        return presentationList;
    }

    public void setPresentationList(List<Presentation> presentationList) {
        this.presentationList = presentationList;
    }
}
