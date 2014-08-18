/*
 * Copyright (C) 2014 Nod Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cardboard.chess;

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

    private static final float[] BLACK_PIECE = {0.15f, 0.15f, 0.15f, 1.0f};
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
