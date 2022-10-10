package de.baumann.browser.event;

/**
 * @Author name
 * @Description  添加一个Toast
 * @Date 2022/8/17 14:06
 * @Param null
 * @Return
**/

public class ShowToastMessageEvent {
    private String message;

    public ShowToastMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
