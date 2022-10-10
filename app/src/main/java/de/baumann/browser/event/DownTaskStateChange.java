package de.baumann.browser.event;

public class DownTaskStateChange {
    private Integer uuid;
    private String state;

    public DownTaskStateChange(Integer uuid, String state) {
        this.uuid = uuid;
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }

    public Integer getUuid() {
        return uuid;
    }

}
