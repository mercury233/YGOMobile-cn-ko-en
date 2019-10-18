//
// Created by kenan on 19-10-18.
//

#ifndef YGOMOBILE_CN_KO_EN_ANDROIDGAMEUI_H
#define YGOMOBILE_CN_KO_EN_ANDROIDGAMEUI_H


#include <irrlicht.h>
#include <jni.h>
#include "android_tools.h"

namespace ygo {
    class AndroidGameUI {
    private:
        jobject host;
        jmethodID java_toggleIME;
        jmethodID java_performHapticFeedback;
        jmethodID java_showComboBoxCompat;
        jmethodID java_getWindowWidth;
        jmethodID java_getWindowHeight;
        jmethodID java_getInitOptions;
        jmethodID java_attachNativeDevice;
        jmethodID java_onGameLaunch;

        ANDROID_APP app;
    public:
        AndroidGameUI(ANDROID_APP  app, jobject obj);

        //! destructor
        virtual ~AndroidGameUI();

        void initMethods(JNIEnv *env);

        void toggleIME(JNIEnv *env, bool show, const char *msg);

        void performHapticFeedback(JNIEnv *env);

        void showAndroidComboBoxCompat(JNIEnv *env, bool pShow,
                                       char **pContents, int count, int mode = 0);

        int getWindowWidth(JNIEnv *env);

        int getWindowHeight(JNIEnv *env);

        irr::android::InitOptions *getInitOptions(JNIEnv *env);

        void attachNativeDevice(JNIEnv* env, void* device);

        void onGameLaunch(JNIEnv* env);
    };
}

#endif //YGOMOBILE_CN_KO_EN_ANDROIDGAMEUI_H
