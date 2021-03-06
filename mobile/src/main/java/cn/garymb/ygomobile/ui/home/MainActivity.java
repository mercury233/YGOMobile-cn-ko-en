package cn.garymb.ygomobile.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.io.File;

import cn.garymb.ygomobile.AppsSettings;
import cn.garymb.ygomobile.Constants;
import cn.garymb.ygomobile.GameUriManager;
import cn.garymb.ygomobile.YGOMobileActivity;
import cn.garymb.ygomobile.YGOStarter;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.ui.activities.LogoActivity;
import cn.garymb.ygomobile.ui.activities.WebActivity;
import cn.garymb.ygomobile.ui.plus.DialogPlus;
import cn.garymb.ygomobile.ui.plus.VUiKit;
import cn.garymb.ygomobile.utils.ComponentUtils;
import cn.garymb.ygomobile.utils.IOUtils;
import cn.garymb.ygomobile.utils.NetUtils;
import cn.garymb.ygomobile.utils.PermissionUtil;
import libwindbot.windbot.WindBot;

import static cn.garymb.ygomobile.Constants.CORE_BOT_CONF_PATH;
import static cn.garymb.ygomobile.Constants.DATABASE_NAME;
import static cn.garymb.ygomobile.Constants.NETWORK_IMAGE;

public class MainActivity extends HomeActivity {
    private static final String TAG = "ResCheckTask";
    private GameUriManager mGameUriManager;
    private ImageUpdater mImageUpdater;
    private boolean enableStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YGOStarter.onCreated(this);
        mImageUpdater = new ImageUpdater(this);
        Log.i("kk", "MainActivity:onCreate");
        boolean isNew = getIntent().getBooleanExtra(LogoActivity.EXTRA_NEW_VERSION, false);
        int err = getIntent().getIntExtra(LogoActivity.EXTRA_ERROR, ResCheckTask.ERROR_NONE);
        //资源复制
        onCheckCompleted(err, isNew);
    }

    @Override
    protected void onResume() {
        super.onResume();
        YGOStarter.onResumed(this);
        //如果游戏Activity已经不存在了，则
        if (!ComponentUtils.isActivityRunning(this, new ComponentName(this, YGOMobileActivity.class))) {
            ComponentUtils.killActivity(this, new ComponentName(this, YGOMobileActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        YGOStarter.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("kk", "MainActivity:onNewIntent");
        getGameUriManager().doIntent(intent);
    }

    private GameUriManager getGameUriManager() {
        if (mGameUriManager == null) {
            mGameUriManager = new GameUriManager(this);
        }
        return mGameUriManager;
    }

    @Override
    protected void openGame() {
        if (enableStart) {
            YGOStarter.startGame(this, null);
        } else {
            VUiKit.show(this, R.string.dont_start_game);
        }
    }

    @Override
    public void updateImages() {
        Log.e("MainActivity", "重置资源");
        DialogPlus dialog = DialogPlus.show(this, null, getString(R.string.message));
        final AssetManager assetManager = getAssets();
        String resPath = AppsSettings.get().getResourcePath();
        VUiKit.defer().when(() -> {
            Log.e("MainActivity", "开始复制");
            try {
                IOUtils.createNoMedia(AppsSettings.get().getResourcePath());
                if (IOUtils.hasAssets(assetManager, Constants.ASSET_PICS_FILE_PATH)) {
                    IOUtils.copyFile(assetManager, Constants.ASSET_PICS_FILE_PATH,
                            new File(resPath, Constants.CORE_PICS_ZIP), true);
                }
                if (IOUtils.hasAssets(assetManager, Constants.ASSET_SCRIPTS_FILE_PATH)) {
                    IOUtils.copyFile(assetManager, Constants.ASSET_SCRIPTS_FILE_PATH,
                            new File(resPath, Constants.CORE_SCRIPTS_ZIP), true);
                }
                IOUtils.copyFile(assetManager, Constants.ASSET_CARDS_CDB_FILE_PATH,
                        new File(AppsSettings.get().getDataBasePath(), Constants.DATABASE_NAME), true);

                IOUtils.copyFile(assetManager, Constants.ASSET_STRING_CONF_FILE_PATH,
                        new File(AppsSettings.get().getResourcePath(), Constants.CORE_STRING_PATH), true);

                IOUtils.copyFolder(assetManager, Constants.ASSET_SKIN_DIR_PATH,
                        AppsSettings.get().getCoreSkinPath(), false);

                IOUtils.copyFolder(assetManager, Constants.ASSET_FONTS_DIR_PATH,
                        AppsSettings.get().getFontDirPath(), false);

                IOUtils.copyFolder(assetManager, Constants.ASSET_WINDBOT_DECK_DIR_PATH,
                        new File(resPath, Constants.LIB_WINDBOT_DECK_PATH).getPath(), true);
                IOUtils.copyFolder(assetManager, Constants.ASSET_WINDBOT_DIALOG_DIR_PATH,
                        new File(resPath, Constants.LIB_WINDBOT_DIALOG_PATH).getPath(), true);
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e("MainActivity", "错误" + e);
            }
        }).done((rs) -> {
            Log.e("MainActivity", "复制完毕");
            dialog.dismiss();
        });
    }

    /*        checkResourceDownload((result, isNewVersion) -> {
                Toast.makeText(this, R.string.tip_reset_game_res, Toast.LENGTH_SHORT).show();
            });*/

    private void onCheckCompleted(int error, boolean isNew) {
        if (error < 0) {
            enableStart = false;
        } else {
            enableStart = true;
        }
        if (isNew) {
            if (!getGameUriManager().doIntent(getIntent())) {
                final DialogPlus dialog = new DialogPlus(this);
                dialog.showTitleBar();
                dialog.setTitle(getString(R.string.settings_about_change_log));
                dialog.loadUrl("file:///android_asset/changelog.html", Color.TRANSPARENT);
                dialog.setLeftButtonText(R.string.help);
                dialog.setLeftButtonListener((dlg, i) -> {
                    dialog.setContentView(R.layout.dialog_help);
                    dialog.setTitle(R.string.question);
                    dialog.hideButton();
                    dialog.show();
                    View viewDialog = dialog.getContentView();
                    Button btnMasterRule = viewDialog.findViewById(R.id.masterrule);
                    Button btnTutorial = viewDialog.findViewById(R.id.tutorial);

                    btnMasterRule.setOnClickListener((v) -> {
                        WebActivity.open(this, getString(R.string.masterrule), Constants.URL_MASTERRULE_CN);
                        dialog.dismiss();
                    });
                    btnTutorial.setOnClickListener((v) -> {
                        WebActivity.open(this, getString(R.string.help), Constants.URL_HELP);
                        dialog.dismiss();
                    });
                });
                dialog.setRightButtonText(R.string.OK);
                dialog.setRightButtonListener((dlg, i) -> {
                    dlg.dismiss();
                    //mImageUpdater
                    if (NETWORK_IMAGE && NetUtils.isConnected(getContext())) {
                        if (!mImageUpdater.isRunning()) {
                            mImageUpdater.start();
                        }
                    }
                });
                    /*DialogPlus dialog = new DialogPlus(this)
                            .setTitleText(getString(R.string.settings_about_change_log))
                            .loadUrl("file:///android_asset/changelog.html", Color.TRANSPARENT)
                            .hideButton()
                            .setOnCloseLinster((dlg) -> {
                                dlg.dismiss();
                                //mImageUpdater
                                if (NETWORK_IMAGE && NetUtils.isConnected(getContext())) {
                                    if (!mImageUpdater.isRunning()) {
                                        mImageUpdater.start();
                                    }
                                }
                            });*/
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        PermissionUtil.isServicePermission(MainActivity.this, true);

                    }
                });
                dialog.show();
            }
        } else {
            PermissionUtil.isServicePermission(MainActivity.this, true);
            getGameUriManager().doIntent(getIntent());
        }
    }
}
