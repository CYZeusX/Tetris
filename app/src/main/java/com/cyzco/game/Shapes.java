package com.cyzco.game;

import java.util.Map;

public class Shapes
{
    public static final String I_BLOCK = "⬜";
    public static final String O_BLOCK = "\uD83D\uDFE8";
    public static final String T_BLOCK = "\uD83D\uDFEA";
    public static final String S_BLOCK = "\uD83D\uDFE9";
    public static final String Z_BLOCK = "\uD83D\uDFE5";
    public static final String J_BLOCK = "\uD83D\uDFE6";
    public static final String L_BLOCK = "\uD83D\uDFE7";
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
                {space, space, space, space},
                {I_BLOCK, I_BLOCK, I_BLOCK, I_BLOCK},
                {space, space, space, space},
                {space, space, space, space}
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
             {space, S_BLOCK, S_BLOCK},
             {S_BLOCK, S_BLOCK, space},
             {space, space, space}
            };

    public static final String[][] Z_SHAPE =
            {
             {Z_BLOCK, Z_BLOCK, space},
             {space, Z_BLOCK, Z_BLOCK},
             {space, space, space}
            };

    public static final String[][] J_SHAPE =
            {
             {J_BLOCK, space, space},
             {J_BLOCK, J_BLOCK, J_BLOCK},
             {space, space, space}
            };

    public static final String[][] L_SHAPE =
            {
             {space, space, L_BLOCK},
             {L_BLOCK, L_BLOCK, L_BLOCK},
             {space, space, space}
            };

    public static final String[][][] SHAPES =
    {I_SHAPE, O_SHAPE, T_SHAPE, S_SHAPE, Z_SHAPE, J_SHAPE, L_SHAPE};

    public static final Map<String, String[][]> SHAPE_MAP = Map.of(
            "I", I_SHAPE,
            "O", O_SHAPE,
            "T", T_SHAPE,
            "S", S_SHAPE,
            "Z", Z_SHAPE,
            "J", J_SHAPE,
            "L", L_SHAPE
    );

    public static final Map<String, String> EMOJI_MAP = Map.of(
            "I", I_BLOCK,
            "O", O_BLOCK,
            "T", T_BLOCK,
            "S", S_BLOCK,
            "Z", Z_BLOCK,
            "J", J_BLOCK,
            "L", L_BLOCK
    );

    // Enum to define piece types
    public enum PieceType {
        I, O, T, S, Z, J, L
    }
}