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

public class Board {
    class Piece {
        int id;
        Constants.PieceType type;
        Constants.PieceColor color;
    }

    class Tile {
        int id;
        Constants.SquareColor color;
    }

    class Square {
        Piece piece;
        Tile tile;
    }

    static final int SIZE = 8;

    private final int[] squares = {
            0, 1, 0, 1, 0, 1, 0, 1,
            1, 0, 1, 0, 1, 0, 1, 0,
            0, 1, 0, 1, 0, 1, 0, 1,
            1, 0, 1, 0, 1, 0, 1, 0,
            0, 1, 0, 1, 0, 1, 0, 1,
            1, 0, 1, 0, 1, 0, 1, 0,
            0, 1, 0, 1, 0, 1, 0, 1,
            1, 0, 1, 0, 1, 0, 1, 0,
    };

    private final char[] pieces = {
            'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R',
            'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
            'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R',
    };

    private Square[][] mBoard = new Square[SIZE][SIZE];

    private int mNextId;

    private void initBoard() {
        int idx = 0;

        for (int row = 0; row < SIZE; ++row) {
            for (int col = 0; col < SIZE; ++col) {
                int color = squares[idx++];
                mBoard[row][col] = new Square();

                Tile tile = new Tile();
                tile.id = mNextId++;
                mBoard[row][col].tile = tile;

                switch (color) {
                    case 0:
                        tile.color = Constants.SquareColor.BLACK;
                        break;
                    case 1:
                        tile.color = Constants.SquareColor.WHITE;
                        break;
                }
            }
        }
    }

    private void initPieces() {
        int idx = 0;

        for (int row = 0; row < SIZE; ++row) {
            for (int col = 0; col < SIZE; ++col) {
                char piece = pieces[idx++];

                if (piece == ' ') {
                    continue;
                }

                mBoard[row][col].piece = new Piece();
                mBoard[row][col].piece.id = mNextId++;

                switch (piece) {
                    case 'P':
                        mBoard[row][col].piece.type = Constants.PieceType.PAWN;
                        break;
                    case 'R':
                        mBoard[row][col].piece.type = Constants.PieceType.ROOK;
                        break;
                    case 'N':
                        mBoard[row][col].piece.type = Constants.PieceType.KNIGHT;
                        break;
                    case 'B':
                        mBoard[row][col].piece.type = Constants.PieceType.BISHOP;
                        break;
                    case 'Q':
                        mBoard[row][col].piece.type = Constants.PieceType.QUEEN;
                        break;
                    case 'K':
                        mBoard[row][col].piece.type = Constants.PieceType.KING;
                        break;
                }
            }
        }

        for (int col = 0; col < SIZE; ++col) {
            mBoard[0][col].piece.color = Constants.PieceColor.WHITE;
            mBoard[1][col].piece.color = Constants.PieceColor.WHITE;

            mBoard[SIZE - 2][col].piece.color = Constants.PieceColor.BLACK;
            mBoard[SIZE - 1][col].piece.color = Constants.PieceColor.BLACK;
        }
    }


    void init() {
        mNextId = 1;
        initBoard();
        initPieces();
    }

    Square getSquare(int row, int col) {
        return mBoard[row][col];
    }
}
