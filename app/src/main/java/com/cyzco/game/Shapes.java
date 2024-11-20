package com.cyzco.game;

public class Shapes
{
    public static final char I_BLOCK = 'I';
    public static final char O_BLOCK = 'O';
    public static final char T_BLOCK = 'T';
    public static final char S_BLOCK = 'S';
    public static final char Z_BLOCK = 'Z';
    public static final char J_BLOCK = 'J';
    public static final char L_BLOCK = 'L';
    public static String block = "回"; //回 ■ □ #
    public static char space = '□';

    public static final char[][] I_SHAPE =
            {
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space},
                {space, I_BLOCK, space, space}
            };

    public static final char[][] O_SHAPE =
            {{O_BLOCK, O_BLOCK},
             {O_BLOCK, O_BLOCK}};

    public static final char[][] T_SHAPE =
            {
             {space, T_BLOCK, space},
             {T_BLOCK, T_BLOCK, T_BLOCK},
             {space, space, space}
            };

    public static final char[][] S_SHAPE =
            {
             {space, space, space, space},
             {space, S_BLOCK, S_BLOCK, space},
             {S_BLOCK, S_BLOCK, space, space},
             {space, space, space, space},
            };

    public static final char[][] Z_SHAPE =
            {
             {space, space, space, space},
             {Z_BLOCK, Z_BLOCK, space, space},
             {space, Z_BLOCK, Z_BLOCK, space},
             {space, space, space, space},
            };

    public static final char[][] J_SHAPE =
            {
             {space, space, space, space},
             {space, space, J_BLOCK, space},
             {space, space, J_BLOCK, space},
             {space, J_BLOCK, J_BLOCK, space},
            };

    public static final char[][] L_SHAPE =
            {
             {space, space, space, space},
             {space, L_BLOCK, space, space},
             {space, L_BLOCK, space, space},
             {space, L_BLOCK, L_BLOCK, space}
            };

    public static final char[][][] SHAPES =
    {I_SHAPE, O_SHAPE, T_SHAPE, S_SHAPE, Z_SHAPE, J_SHAPE, L_SHAPE};
}