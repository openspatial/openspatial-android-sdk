package com.example.cardboard;

public class Board {
    class Piece {
        Constants.PieceType type;
        Constants.PieceColor color;
    }

    class Square {
        Piece piece;
        Constants.SquareColor color;
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

    private void initBoard() {
        int idx = 0;

        for (int row = 0; row < SIZE; ++row) {
            for (int col = 0; col < SIZE; ++col) {
                int color = squares[idx++];
                mBoard[row][col] = new Square();

                switch (color) {
                    case 0:
                        mBoard[row][col].color = Constants.SquareColor.BLACK;
                        break;
                    case 1:
                        mBoard[row][col].color = Constants.SquareColor.WHITE;
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
        initBoard();
        initPieces();
    }

    Square getSquare(int row, int col) {
        return mBoard[row][col];
    }
}
