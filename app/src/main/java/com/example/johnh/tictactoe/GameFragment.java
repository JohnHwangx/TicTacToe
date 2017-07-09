package com.example.johnh.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by johnh on 2017/5/29.
 */

public class GameFragment extends Fragment {
    // 在这里定义数据结构……
    //将数字映射到小棋盘和格子的资源id
    static private int mLargeIds[] = {R.id.large1, R.id.large2, R.id.large3,
            R.id.large4, R.id.large5, R.id.large6, R.id.large7, R.id.large8,
            R.id.large9,};
    static private int mSmallIds[] = {R.id.small1, R.id.small2, R.id.small3,
            R.id.small4, R.id.small5, R.id.small6, R.id.small7, R.id.small8,
            R.id.small9,};
    //表示不同层级的格子
    private Tile mEntireBoard = new Tile(this);
    private Tile mLargeTiles[] = new Tile[9];
    private Tile mSmallTiles[][] = new Tile[9][9];
    private Tile.Owner mPlayer = Tile.Owner.X;
    //包含给定时点可下的所有格子。这个列表是根据前一步棋和游戏规则计算得到的
    private Set<Tile> mAvailable = new HashSet<Tile>();

    // Handler类能够将事情推迟到以后再做
    private Handler mHandler = new Handler();
    //最后一步棋的索引
    private int mLastLarge;
    private int mLastSmall;

    public GameFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设备配置发生变化时保留这个片段
        //在父活动因设备配置发生变化（如设备旋转）而被销毁时， Android不会销毁该片段
        setRetainInstance(true);
        initGame();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.large_board, container, false);
        initViews(rootView);
        updateAllTiles();
        return rootView;
    }

    public void initGame() {
        Log.d("UT3", "init game");
        mEntireBoard = new Tile(this);
        // 创建所有的格子
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large] = new Tile(this);
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small] = new Tile(this);
            }
            mLargeTiles[large].setSubTiles(mSmallTiles[large]);
        }
        mEntireBoard.setSubTiles(mLargeTiles);
        // 设置先下棋子的玩家可下的格子
        mLastSmall = -1;
        mLastLarge = -1;
        setAvailableFromLastMove(mLastSmall);
    }

    private void initViews(View rootView) {
        mEntireBoard.setView(rootView);
        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(mLargeIds[large]);
            mLargeTiles[large].setView(outer);
            for (int small = 0; small < 9; small++) {
                ImageButton inner = (ImageButton) outer.findViewById
                        (mSmallIds[small]);
                final int fLarge = large;
                final int fSmall = small;
                final Tile smallTile = mSmallTiles[large][small];
                smallTile.setView(inner);
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isAvailable(smallTile)) {
                            makeMove(fLarge, fSmall);
//                            switchTurns();
                            think();
                        }
                    }
                });
            }
        }
    }

    private void think() {
        //用户轻按棋盘格时，开启思考指示器，执行用户要求下的棋，然后启动定时器。 1000毫秒（1秒）后，执行方法run()
        ((GameActivity) getActivity()).startThinking();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                if (mEntireBoard.getOwner() == Tile.Owner.NEITHER) {
                    int move[] = new int[2];
                    pickMove(move);

                    if (move[0] != -1 && move[1] != -1) {
                        switchTurns();
                        makeMove(move[0], move[1]);
                        switchTurns();
                    }
                }
                ((GameActivity) getActivity()).stopThinking();
            }
        }, 1000);
    }

    /**
     * 选择走法
     * 遍历每个棋盘格（总共81个），并使用方法isAvailable()判断是否可在该棋盘格中下棋。
     * 如果可以，就复制整个棋盘，调用方法setOwner()在该棋盘格中下棋，然后再评估棋局。在遍
     * 历过程中，需要记录评估函数返回的最佳值及对应的走法。循环结束后，通过传入的数组返回走
     * 法
     *
     * @param move
     */
    private void pickMove(int move[]) {
        Tile.Owner opponent = mPlayer == Tile.Owner.X ? Tile.Owner.O : Tile
                .Owner.X;
        int bestLarge = -1;
        int bestSmall = -1;
        int bestValue = Integer.MAX_VALUE;
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile smallTile = mSmallTiles[large][small];
                if (isAvailable(smallTile)) {
                    // 尝试下棋并评估得到的棋局的得分
                    Tile newBoard = mEntireBoard.deepCopy();
                    newBoard.getSubTiles()[large].getSubTiles()[small]
                            .setOwner(opponent);
                    int value = newBoard.evaluate();
                    Log.d("UT3",
                            "Moving to " + large + ", " + small + " gives value " +
                                    "" + value
                    );
                    if (value < bestValue) {
                        bestLarge = large;
                        bestSmall = small;
                        bestValue = value;
                    }
                }
            }
        }
        move[0] = bestLarge;
        move[1] = bestSmall;
        Log.d("UT3", "Best move is " + bestLarge + ", " + bestSmall);
    }

    /**
     * 让另一个玩家接着下
     */
    private void switchTurns() {
        mPlayer = mPlayer == Tile.Owner.X ? Tile.Owner.O : Tile
                .Owner.X;
    }

    /**
     * 创建包含游戏状态的字符串
     */
    public String getState() {
        StringBuilder builder = new StringBuilder();
        builder.append(mLastLarge);
        builder.append(',');
        builder.append(mLastSmall);
        builder.append(',');
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                builder.append(mSmallTiles[large][small].getOwner().name());
                builder.append(',');
            }
        }
        return builder.toString();
    }

    /**
     * 根据给定的字符串恢复游戏状态
     */
    public void putState(String gameData) {
        String[] fields = gameData.split(",");
        int index = 0;
        mLastLarge = Integer.parseInt(fields[index++]);
        mLastSmall = Integer.parseInt(fields[index++]);
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile.Owner owner = Tile.Owner.valueOf(fields[index++]);
                mSmallTiles[large][small].setOwner(owner);
            }
        }
        setAvailableFromLastMove(mLastSmall);
        updateAllTiles();
    }

    /**
     * 重新开始游戏
     *
     * @return
     */
    public void restartGame() {
        initGame();
        initViews(getView());
        updateAllTiles();
    }

    private void updateAllTiles() {
        mEntireBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            mLargeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                mSmallTiles[large][small].updateDrawableState();
            }
        }
    }

    /**
     * 将棋下到格子中
     * 这里做的主要工作是，让当前玩家占据格子。
     * 接下来，需要确定是否有玩家赢得了当前格子所属的小棋盘。
     * 如果有，就设置该小棋盘的占据者。
     * 最后，检查整个棋盘，看是否有玩家占据了整个棋盘。
     * 如果已经有玩家占据了整个棋盘，就意味着该玩家获得了胜利，需要调用宣告获胜者的方法。
     *
     * @param large
     * @param small
     */
    private void makeMove(int large, int small) {
        mLastLarge = large;
        mLastSmall = small;
        Tile smallTile = mSmallTiles[large][small];
        Tile largeTile = mLargeTiles[large];
        smallTile.setOwner(mPlayer);
        setAvailableFromLastMove(small);
        Tile.Owner oldWinner = largeTile.getOwner();
        Tile.Owner winner = largeTile.findWinner();
        if (winner != oldWinner) {
            largeTile.setOwner(winner);
        }
        winner = mEntireBoard.findWinner();
        mEntireBoard.setOwner(winner);
        updateAllTiles();
        if (winner != Tile.Owner.NEITHER) {
            ((GameActivity) getActivity()).reportWinner(winner);
        }
    }

    /**
     * 用于清空可下棋格子列表，并将目标小棋盘中所有的空格子都标记为可下棋的
     *
     * @param small
     */
    private void setAvailableFromLastMove(int small) {
        clearAvailable();
        // 让目标小棋盘中所有空格子都可下棋
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                Tile largeTile = mLargeTiles[small];
                if (largeTile.getOwner() != Tile.Owner.NEITHER)
                    continue;

                Tile tile = mSmallTiles[small][dest];
                if (tile.getOwner() == Tile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
        // 如果目标小棋盘没有空格子，则令整个棋盘的所有空格子都可下棋
        if (mAvailable.isEmpty()) {
            setAllAvailable();
        }
    }

    /**
     * 将整个棋盘中的所有空格子都标记为可下棋的
     */
    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            Tile largeTile = mLargeTiles[large];
            if (largeTile.getOwner() != Tile.Owner.NEITHER)
                continue;
            for (int small = 0; small < 9; small++) {
                Tile tile = mSmallTiles[large][small];
                if (tile.getOwner() == Tile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
    }

    private void clearAvailable() {
        mAvailable.clear();
    }

    private void addAvailable(Tile tile) {
        mAvailable.add(tile);
    }

    public boolean isAvailable(Tile tile) {
        return mAvailable.contains(tile);
    }
}
