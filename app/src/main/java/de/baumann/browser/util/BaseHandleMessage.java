package de.baumann.browser.util;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

public class BaseHandleMessage {

    private final ArrayList<Handler> mUpdateHandlers;
    private static BaseHandleMessage mInstance;

    public ArrayList<Handler> getmUpdateHandler() {
        return mUpdateHandlers;
    }

    private BaseHandleMessage() {
        mInstance = this;
        mUpdateHandlers = new ArrayList<>();
    }

    public synchronized static BaseHandleMessage getInstance() {
        if (mInstance == null)
            mInstance = new BaseHandleMessage();
        return mInstance;
    }


    public void addBaseHandleMessage(Handler handler) {
        if (handler != null) {
            mUpdateHandlers.add(handler);
        }
    }

    public void removeBaseHandleMessage(Handler handler) {
        if (handler != null) {
            mUpdateHandlers.remove(handler);
        }
    }

    public void setHandlerMessage(int what, Object objsect) {
        for (int i = 0; i < mUpdateHandlers.size(); i++) {
            Handler handler = mUpdateHandlers.get(i);
            Message msg = Message.obtain();
            msg.obj = objsect;
            msg.what = what;
            handler.sendMessage(msg);

        }
    }

    public void sendMessageDelayed(int what, Object objsect, long delayMillis) {
        for (int i = 0; i < mUpdateHandlers.size(); i++) {
            Handler handler = mUpdateHandlers.get(i);
            Message msg = Message.obtain();
            msg.obj = objsect;
            msg.what = what;
            handler.sendMessageDelayed(msg, delayMillis);
        }
    }
}
