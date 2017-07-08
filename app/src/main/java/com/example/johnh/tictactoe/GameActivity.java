package com.example.johnh.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by johnh on 2017/5/29.
 */

public class GameActivity extends Activity {
    public static final String KEY_RESTORE = "key_restore";//用于将棋盘恢复到以前冻结的状态
    public static final String PREF_RESTORE = "pref_restore";
    private GameFragment mGameFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //获取游戏界面，九宫格
        mGameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.fragment_game);

        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        if (restore) {
            //获取指向该活动的Android首选项管理器的句柄
            String gameData = getPreferences(MODE_PRIVATE).getString(PREF_RESTORE, null);
            if (gameData != null) {
                mGameFragment.putState(gameData);
            }
        }
        Log.d("UT3", "restore = " + restore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String gameData = mGameFragment.getState();
        //获取一个指向首选项存储区的句柄（getPreferences），
        // 为首选项创建一个编辑器（edit），
        // 使用键PREF_RESTORE保存游戏数据（putString），
        // 并将修改存储到首选项存储区（commit）。
        getPreferences(MODE_PRIVATE).edit().putString(PREF_RESTORE, gameData).commit();
        Log.d("UT3", "state = " + gameData);
    }

    /**
     * 重新开始
     */
    public void restartGame(){
        mGameFragment.restartGame();
    }

    public void reportWinner(final Tile.Owner winner){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.declare_winner, winner));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        final Dialog dialog = builder.create();
        dialog.show();
        // 将棋盘重置为初始状态
        mGameFragment.initGame();
    }
}