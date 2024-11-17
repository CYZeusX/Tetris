package com.cyzco.game;

public class Shapes
{
    public static char block = '回';
    public static char space = '.';

    public static final char[][] I_SHAPE =
            {{space, block, space, space},
             {space, block, space, space},
             {space, block, space, space},
             {space, block, space, space}};

    public static final char[][] O_SHAPE =
            {{block, block},
             {block, block}};

    public static final char[][] T_SHAPE =
            {
             {space, space, space, space},
             {space, block, space, space},
             {block, block, block, space},
             {space, space, space, space},
            };

    public static final char[][] S_SHAPE =
            {
             {space, space, space, space},
             {space, block, block, space},
             {block, block, space, space},
             {space, space, space, space},
            };

    public static final char[][] Z_SHAPE =
            {
             {space, space, space, space},
             {block, block, space, space},
             {space, block, block, space},
             {space, space, space, space}
            };

    public static final char[][] J_SHAPE =
            {
             {space, space, space, space},
             {space, block, space, space},
             {space, block, space, space},
             {space, block, block, space}
            };

    public static final char[][] L_SHAPE =
            {
             {space, space, space, space},
             {space, space, block, space},
             {space, space, block, space},
             {space, block, block, space},
            };

    public static final char[][][] SHAPES =
    {I_SHAPE, O_SHAPE, T_SHAPE, S_SHAPE, Z_SHAPE, J_SHAPE, L_SHAPE};
}