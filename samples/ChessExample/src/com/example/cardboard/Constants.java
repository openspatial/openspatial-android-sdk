package com.example.cardboard;

final class Constants {
    static enum PieceType {
        PAWN,
        ROOK,
        KNIGHT,
        BISHOP,
        QUEEN,
        KING
    }

    static enum PieceColor {
        BLACK,
        WHITE,
    }

    static enum SquareColor {
        BLACK,
        WHITE,
    }

    private static final float[] BLACK_SQUARE = {0.1f, 0.1f, 0.1f, 1.0f};
    private static final float[] WHITE_SQUARE = {0.99f, 0.99f, 0.99f, 1.0f};

    private static final float[] BLACK_PIECE = {0.3f, 0.3f, 0.3f, 1.0f};
    private static final float[] WHITE_PIECE = {0.8f, 0.8f, 0.8f, 1.0f};

    static float[] getSquareColor(SquareColor color) {
        switch (color) {
            case WHITE:
                return WHITE_SQUARE;
            case BLACK:
                return BLACK_SQUARE;
        }

        return null;
    }

    static float[] getPieceColor(PieceColor color) {
        switch (color) {
            case WHITE:
                return WHITE_PIECE;
            case BLACK:
                return BLACK_PIECE;
        }

        return null;
    }

    static int getResourceIdForPiece(PieceType piece) {
        switch (piece) {
            case PAWN:
                return R.raw.pawn;
            case ROOK:
                return R.raw.rook;
            case KNIGHT:
                return R.raw.knight;
            case BISHOP:
                return R.raw.bishop;
            case QUEEN:
                return R.raw.queen;
            case KING:
                return R.raw.king;
        }

        return 0;
    }
}
