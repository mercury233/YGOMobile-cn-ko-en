package cn.garymb.ygomobile.interfaces;

import androidx.annotation.Keep;

import java.nio.ByteBuffer;

@Keep
public interface IGameUI {

    int getWindowWidth();

    int getWindowHeight();

    void toggleIME(boolean show, String message);

    void performHapticFeedback();

    void showComboBoxCompat(String[] items, boolean isShow, int mode);

    ByteBuffer getInitOptions();

    void attachNativeDevice(int device);

    void onGameLaunch();

}
