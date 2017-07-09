package com.example.johnh.tictactoe;

//import android.service.quicksettings.Tile;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;

/**
 * Created by johnh on 2017/5/29.
 * 表示任何层次的棋盘格子，它可以是可包含X或O的最小格子、包含9个格子的小棋盘或包含9个小棋盘的大棋盘
 */

public class Tile {
    public enum Owner {
        X, O /* 字母O */, NEITHER, BOTH
    }

    // 这些级别是在drawable中定义的
    private static final int LEVEL_X = 0;
    private static final int LEVEL_O = 1; // letter O
    private static final int LEVEL_BLANK = 2;
    private static final int LEVEL_AVAILABLE = 3;
    private static final int LEVEL_TIE = 3;
    private final GameFragment mGame;
    private Owner mOwner = Owner.NEITHER;
    private View mView;
    private Tile mSubTiles[];

    public Tile(GameFragment game) {
        this.mGame = game;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public Owner getOwner() {
        return mOwner;
    }

    public void setOwner(Owner owner) {
        this.mOwner = owner;
    }

    public Tile[] getSubTiles() {
        return mSubTiles;
    }

    public void setSubTiles(Tile[] subTiles) {
        this.mSubTiles = subTiles;
    }

    /**
     * 管理drawable状态的代码
     */
    public void updateDrawableState() {
        if (mView == null) return;
        int level = getLevel();
        if (mView.getBackground() != null) {
            mView.getBackground().setLevel(level);
        }
        if (mView instanceof ImageButton) {
            Drawable drawable = ((ImageButton) mView).getDrawable();
            drawable.setLevel(level);
        }
    }

    /**
     * @return
     */
    private int getLevel() {
        int level = LEVEL_BLANK;
        switch (mOwner) {
            case X:
                level = LEVEL_X;
                break;
            case O: // 字母O
                level = LEVEL_O;
                break;
            case BOTH:
                level = LEVEL_TIE;
                break;
            case NEITHER:
                level = mGame.isAvailable(this) ? LEVEL_AVAILABLE : LEVEL_BLANK;
                break;
        }
        return level;
    }

    public Owner findWinner() {
        // 如果已确定占据者，就返回它
        if (getOwner() != Owner.NEITHER)
            return getOwner();
        int totalX[] = new int[4];
        int totalO[] = new int[4];
        countCaptures(totalX, totalO);
        if (totalX[3] > 0) return Owner.X;
        if (totalO[3] > 0) return Owner.O;
        // 检查是否打成了平局
        int total = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner != Owner.NEITHER) total++;
            }
            if (total == 9) return Owner.BOTH;
        }
        // 未被任何玩家占据
        return Owner.NEITHER;
    }

    /**
     * 判断形势
     * 结果是通过两个数组返回的：表示玩家X的数组totalX和表示玩家O的数组totalO
     * @param totalX 玩家X的数组totalX
     * @param totalO 玩家O的数组totalO
     */
    private void countCaptures(int totalX[], int totalO[]) {
        int capturedX, capturedO;
        // 检查是否有同一个玩家的3个棋子排成了一行
        for (int row = 0; row < 3; row++) {
            capturedX = capturedO = 0;
            for (int col = 0; col < 3; col++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
                if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
            }
            totalX[capturedX]++;
            totalO[capturedO]++;
        }
        // 检查是否有同一个玩家的3个棋子排成了一列
        for (int col = 0; col < 3; col++) {
            capturedX = capturedO = 0;
            for (int row = 0; row < 3; row++) {
                Owner owner = mSubTiles[3 * row + col].getOwner();
                if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
                if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
            }
            totalX[capturedX]++;
            totalO[capturedO]++;
        }
        // 检查是否有同一个玩家的3个棋子排成对角线
        capturedX = capturedO = 0;
        for (int diag = 0; diag < 3; diag++) {
            Owner owner = mSubTiles[3 * diag + diag].getOwner();
            if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
            if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
        }
        totalX[capturedX]++;
        totalO[capturedO]++;
        capturedX = capturedO = 0;
        for (int diag = 0; diag < 3; diag++) {
            Owner owner = mSubTiles[3 * diag + (2 - diag)].getOwner();
            if (owner == Owner.X || owner == Owner.BOTH) capturedX++;
            if (owner == Owner.O || owner == Owner.BOTH) capturedO++;
        }
        totalX[capturedX]++;
        totalO[capturedO]++;
    }

    /**
     * 评估函数
     * @return  评估值，如果整个棋盘、小棋盘或格子被X或O玩家占据，评估函数将返回一个很大的数字
     */
    public int evaluate() {
        switch (getOwner()) {
            case X:
                return 100;
            case O:
                return -100;
            case NEITHER://未被任何玩家占据
                int total = 0;
                if (getSubTiles() != null) {
                    for (int tile = 0; tile < 9; tile++) {
                        total += getSubTiles()[tile].evaluate();//评估每个子元素，并将每个子元素的评估结果相加
                    }
                    int totalX[] = new int[4];
                    int totalO[] = new int[4];
                    countCaptures(totalX, totalO);
                    //对于X占据的子元素，每出现3个排成一条线的情况时都加8；
                    //  每出现两个子元素排除一条线时都加2；
                    //  每个单独的子元素都加1；
                    //对于O占据的子元素，采用相同的算法，但不是加上而是减去相应的数字
                    total = total * 100 + totalX[1] + 2 * totalX[2] + 8 *
                            totalX[3] - totalO[1] - 2 * totalO[2] - 8 * totalO[3];
                }
                return total;
        }

        return 0;
    }

    /**
     * 首先创建一个新的Tile实例，并复制占据者。接下来，检查它是否有子元素。如果有，就创
     建一个新的Tile引用数组，用来存储子元素副本。然后，对每个子元素递归调用deepCopy()。
     最后，将子元素引用设置为这个新数组。如果最初的Tile实例没有子元素（换句话说，它是包含
     X或O的棋盘格），那就什么都不做
     * @return
     */
    public Tile deepCopy() {
        Tile tile = new Tile(mGame);
        tile.setOwner(getOwner());
        if (getSubTiles() != null) {
            Tile newTiles[] = new Tile[9];
            Tile oldTiles[] = getSubTiles();
            for (int child = 0; child < 9; child++) {
                newTiles[child] = oldTiles[child].deepCopy();
            }
            tile.setSubTiles(newTiles);
        }
        return tile;
    }
}
