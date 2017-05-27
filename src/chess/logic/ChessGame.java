package chess.logic;

import java.util.ArrayList;
import java.util.List;

import chess.console.ChessConsole;

/*
 * thể hiện cac quân cờ trạng thái của chúng 
 * 
*/

public class ChessGame implements Runnable {

	public int gameState = GAME_STATE_WHITE;
	public static final int GAME_STATE_WHITE = 0;
	public static final int GAME_STATE_BLACK = 1;
	public static final int GAME_STATE_END_BLACK_WON = 2;
	public static final int GAME_STATE_END_WHITE_WON = 3;

	// list quân cờ 0 = bottom, size = top
	public List<Piece> pieces = new ArrayList<Piece>();
	// list quân cờ bị bắt
	private List<Piece> capturedPieces = new ArrayList<Piece>();
	// xác nhận di chuyển
	private MoveValidator moveValidator;
	private IPlayerHandler blackPlayerHandler;
	private IPlayerHandler whitePlayerHandler;
	private IPlayerHandler activePlayerHandler;

	/**
	 * Khởi tạo
	 */
	public ChessGame() {

		this.moveValidator = new MoveValidator(this);

		// create and place pieces
		// rook, knight, bishop, queen, king, bishop, knight, and rook
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_1, Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);

		// pawns
		int currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, currentColumn);
			currentColumn++;
		}

		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_B);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_C);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, Piece.ROW_8, Piece.COLUMN_D);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_E);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_F);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_G);
		createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);

		// pawns
		currentColumn = Piece.COLUMN_A;
		for (int i = 0; i < 8; i++) {
			createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, currentColumn);
			currentColumn++;
		}
	}

	/**
	 * Thiết lập màu cho người chơi và máy 
	 * 
	 * @param pieceColor
	 *            màu sắc của máy và người ch
	 * @param playerHandler
	 *           máy / người chơi
	 */
	public void setPlayer(int pieceColor, IPlayerHandler playerHandler) {
		switch (pieceColor) {
		case Piece.COLOR_BLACK:
			this.blackPlayerHandler = playerHandler;
			break;
		case Piece.COLOR_WHITE:
			this.whitePlayerHandler = playerHandler;
			break;
		default:
			throw new IllegalArgumentException("Invalid pieceColor: " + pieceColor);
		}
	}

	/**
	 * bắt đầu game
	 */
	public void startGame() {
		// kiểm tra ng chơi và máy sắn sàng
		System.out.println("ChessGame: Chờ người chơi sắn sàng");
		while (this.blackPlayerHandler == null || this.whitePlayerHandler == null) {
			// người chơi vẫn còn thiếu
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// thiết lập người chơi đầu
		this.activePlayerHandler = this.whitePlayerHandler;

		// bắt đầu game
		System.out.println("ChessGame: Bắt đầu");
		while (!isGameEndConditionReached()) {
			waitForMove();
			swapActivePlayer();
		}

		System.out.println("ChessGame: Kết thúc");
		ChessConsole.printCurrentGameState(this);
		if (this.gameState == ChessGame.GAME_STATE_END_BLACK_WON) {
			System.out.println("Black won!");

		} else if (this.gameState == ChessGame.GAME_STATE_END_WHITE_WON) {
			System.out.println("White won!");

		} else {
			throw new IllegalStateException("Illegal end state: " + this.gameState);
		}
	}

	/**
	 * Thay đổi lượt chơi và trạng thái 
	 */
	private void swapActivePlayer() {
		if (this.activePlayerHandler == this.whitePlayerHandler) {
			this.activePlayerHandler = this.blackPlayerHandler;
		} else {
			this.activePlayerHandler = this.whitePlayerHandler;
		}

		this.changeGameState();
	}

	/**
	 * chờ cho máy/người di chuyên quân cờ và thông báo 
	 */
	private void waitForMove() {
		Move move = null;
		// chờ việc di chuyển
		do {
			move = this.activePlayerHandler.getMove();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (move != null && this.moveValidator.isMoveValid(move, false)) {
				break;
			} else if (move != null && !this.moveValidator.isMoveValid(move, true)) {
				System.out.println("provided move was invalid: " + move);

				ChessConsole.printCurrentGameState(this);
				move = null;
				System.exit(0);
			}
		} while (move == null);

		// thực hiện di chuyển
		boolean success = this.movePiece(move);
		if (success) {
			this.blackPlayerHandler.moveSuccessfullyExecuted(move);
			this.whitePlayerHandler.moveSuccessfullyExecuted(move);
		} else {
			throw new IllegalStateException("move was valid, but failed to execute it");
		}
	}

	/**
	 * tạo ra các quân cờ và thêm nó vào bàn cờ
	 * 
	 * @param color
	 *            on of Pieces.COLOR_..
	 * @param type
	 *            on of Pieces.TYPE_..
	 * @param row
	 *            on of Pieces.ROW_..
	 * @param column
	 *            on of Pieces.COLUMN_..
	 */
	public void createAndAddPiece(int color, int type, int row, int column) {
		Piece piece = new Piece(color, type, row, column);
		this.pieces.add(piece);
	}

	/**
	 * di chuyển quân cờ đến vị trí chỉ định, nếu đích có quân cờ khác
	 * đánh dấu quân cờ đó đã bị bắt
	 * nếu di chuyển k thành công thì sẽ trả về vị trí trước đó
	 * 
	 * @param move
	 *            thực hiện di chuyển
	 * @return true, nếu di chuyển thành công
	 */
	public boolean movePiece(Move move) {
		// đặt quân cờ cần di chuyển
		//  undoMove() method.
		move.capturedPiece = this.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

		Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);

		// kiểm tra việc di chuyển này có bắt quân cờ đối thủ k
		int opponentColor = (piece.getColor() == Piece.COLOR_BLACK ? Piece.COLOR_WHITE : Piece.COLOR_BLACK);
		if (isNonCapturedPieceAtLocation(opponentColor, move.targetRow, move.targetColumn)) {
			// giữ quân cờ
			Piece opponentPiece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
			this.pieces.remove(opponentPiece);
			this.capturedPieces.add(opponentPiece);
			opponentPiece.isCaptured(true);
		}

		// di chuyển quân cờ đến vị trí mới
		piece.setRow(move.targetRow);
		piece.setColumn(move.targetColumn);

		// reset game state
		// if(piece.getColor() == Piece.COLOR_BLACK){
		// this.gameState = ChessGame.GAME_STATE_BLACK;
		// }else{
		// this.gameState = ChessGame.GAME_STATE_WHITE;
		// }

		return true;
	}

	/**
	 * hoàn tác lại việc di chuyển được chọn
	 * cùng với việc điều chỉnh lại trạng thái game
	 * 
	 * @param move
	 */
	public void undoMove(Move move) {
		Piece piece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

		piece.setRow(move.sourceRow);
		piece.setColumn(move.sourceColumn);

		if (move.capturedPiece != null) {
			move.capturedPiece.setRow(move.targetRow);
			move.capturedPiece.setColumn(move.targetColumn);
			move.capturedPiece.isCaptured(false);
			this.capturedPieces.remove(move.capturedPiece);
			this.pieces.add(move.capturedPiece);
		}

		if (piece.getColor() == Piece.COLOR_BLACK) {
			this.gameState = ChessGame.GAME_STATE_BLACK;
		} else {
			this.gameState = ChessGame.GAME_STATE_WHITE;
		}
	}

	/**
	 * kiểm tra điều kiện kết thúc game
	 * khi quân vua bị bắt
	 * 
	 * @return true nếu điều kiện đúng
	 */
	private boolean isGameEndConditionReached() {
		for (Piece piece : this.capturedPieces) {
			if (piece.getType() == Piece.TYPE_KING) {
				return true;
			} else {
				// continue iterating
			}
		}

		return false;
	}

	/**
	 * trả lại vị trí được chỉ đinh mà k được đánh dấu là đã được ăn
	 * 
	 * @param row
	 *            one of Piece.ROW_..
	 * @param column
	 *            one of Piece.COLUMN_..
	 * @return trả về vị trí không có quan cờ
	 */
	public Piece getNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column) {
				return piece;
			}
		}
		return null;
	}

	/**
	 * kiểm tra tại vị trí được chỉ đinh
	 * có quân cờ nào cùng màu hay không 
	 * 
	 * @param color
	 *            one of Piece.COLOR_..
	 * @param row
	 *            one of Piece.ROW_..
	 * @param column
	 *            on of Piece.COLUMN_..
	 * @return true, nếu vị trí có chưa 1 quân cờ cùng màu
	 */
	boolean isNonCapturedPieceAtLocation(int color, int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column && piece.getColor() == color) {
				return true;
			}
		}
		return false;
	}

	/**
	 * kiểm tra có quân cờ nào không bị bắt tại vị trí đến
	 * @param row
	 *            one of Piece.ROW_..
	 * @param column
	 *            on of Piece.COLUMN_..
	 * @return true, if the location contains a piece
	 */
	boolean isNonCapturedPieceAtLocation(int row, int column) {
		for (Piece piece : this.pieces) {
			if (piece.getRow() == row && piece.getColumn() == column) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return trạng thái hiện tại (one of ChessGame.GAME_STATE_..)
	 */
	public int getGameState() {
		return this.gameState;
	}

	/**
	 * @return danh sách quân cờ
	 */
	public List<Piece> getPieces() {
		return this.pieces;
	}

	/**
	 * chuyển đổi trạng thái lượt chơi
	 */
	public void changeGameState() {

		// kiểm tra đã thỏa điều kiện kết thúc game
		//
		if (this.isGameEndConditionReached()) {

			if (this.gameState == ChessGame.GAME_STATE_BLACK) {
				this.gameState = ChessGame.GAME_STATE_END_BLACK_WON;
			} else if (this.gameState == ChessGame.GAME_STATE_WHITE) {
				this.gameState = ChessGame.GAME_STATE_END_WHITE_WON;
			} else {
				// leave game state as it is
			}
			return;
		}

		switch (this.gameState) {
		case GAME_STATE_BLACK:
			this.gameState = GAME_STATE_WHITE;
			break;
		case GAME_STATE_WHITE:
			this.gameState = GAME_STATE_BLACK;
			break;
		case GAME_STATE_END_WHITE_WON:
		case GAME_STATE_END_BLACK_WON:// don't change anymore
			break;
		default:
			throw new IllegalStateException("unknown game state:" + this.gameState);
		}
	}

	/**
	 * @return kiểm tra việc di chuyển
	 */
	public MoveValidator getMoveValidator() {
		return this.moveValidator;
	}

	@Override
	public void run() {
		this.startGame();
	}

}
