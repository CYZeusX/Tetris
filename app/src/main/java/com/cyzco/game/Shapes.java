package com.cyzco.game;

public class Shapes
{
    public static final String I_BLOCK = "⬜";
    public static final String O_BLOCK = "\uD83D\uDFE8";
    public static final String T_BLOCK = "\uD83D\uDFEA";
    public static final String S_BLOCK = "\uD83D\uDFE9";
    public static final String Z_BLOCK = "\uD83D\uDFE5";
    public static final String J_BLOCK = "\uD83D\uDFE7";
    public static final String L_BLOCK = "\uD83D\uDFE6";
    public String block = "■";
    public static String space = "□";

    public void setBlock(String block)
    {
        this.block = block;
    }

    public String getBlock()
    {
        return this.block;
    }

    public static final String[][] I_SHAPE =
            {
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space}
            };

    public static final String[][] O_SHAPE =
            {{O_BLOCK, O_BLOCK},
             {O_BLOCK, O_BLOCK}};

    public static final String[][] T_SHAPE =
            {
             {space, T_BLOCK, space},
             {T_BLOCK, T_BLOCK, T_BLOCK},
             {space, space, space}
            };

    public static final String[][] S_SHAPE =
            {
             {space, space, space},
             {space, S_BLOCK, S_BLOCK},
             {S_BLOCK, S_BLOCK, space},
            };

    public static final String[][] Z_SHAPE =
            {
             {space, space, space},
             {Z_BLOCK, Z_BLOCK, space},
             {space, Z_BLOCK, Z_BLOCK},
            };

    public static final String[][] J_SHAPE =
            {
             {J_BLOCK, space, space},
             {J_BLOCK, J_BLOCK, J_BLOCK},
             {space, space, space},
            };

    public static final String[][] L_SHAPE =
            {
             {space, space, L_BLOCK},
             {L_BLOCK, L_BLOCK, L_BLOCK},
             {space, space, space}
            };

    public static final String[][][] SHAPES =
    {I_SHAPE, O_SHAPE, T_SHAPE, S_SHAPE, Z_SHAPE, J_SHAPE, L_SHAPE};
}