package de.baumann.browser.event;


public class DownTaskFinished {
    private Integer uuid;

    public DownTaskFinished(Integer uuid) {
        this.uuid = uuid;
    }

    public Integer getUuid(){
        return uuid;
    }
}
