package com.cindura.evamomentsapp.model;

import java.io.Serializable;
import java.util.List;

//Model Class to describe the structure of the show
public class Presentation implements Serializable {
    private String selfPic;
    private List<String> selectedItems;
    private List<String> keywords;
    private String audioSavedPath;
    private int filteredPosition;
    private String createdDate;
    private String modifiedDate;
    private String numOfTimesPlayed;
    private String lastPlayed;

    public String getNumOfTimesPlayed() {
        return numOfTimesPlayed;
    }

    public void setNumOfTimesPlayed(String numOfTimesPlayed) {
        this.numOfTimesPlayed = numOfTimesPlayed;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getSelfPic() {
        return selfPic;
    }

    public void setSelfPic(String selfPic) {
        this.selfPic = selfPic;
    }

    public List<String> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<String> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getAudioSavedPath() {
        return audioSavedPath;
    }

    public void setAudioSavedPath(String audioSavedPath) {
        this.audioSavedPath = audioSavedPath;
    }

    public int getFilteredPosition() {
        return filteredPosition;
    }

    public void setFilteredPosition(int filteredPosition) {
        this.filteredPosition = filteredPosition;
    }
}
