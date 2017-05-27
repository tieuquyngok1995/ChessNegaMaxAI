package chess;
import chess.ai.SimpleAiPlayerHandler;
import chess.console.ChessConsole;
import chess.gui.ChessGui;
import chess.logic.ChessGame;
import chess.logic.Piece;


public class Main {

	public static void main(String[] args) {

		// Đầu tiên chúng ta tạo ra trò chơi
		ChessGame chessGame = new ChessGame();

		// Khởi tạo người chơi và máy 
		ChessGui chessGui = new ChessGui(chessGame);
		//ChessConsole chessConsole = new ChessConsole(chessGame);
		SimpleAiPlayerHandler ai1 = new SimpleAiPlayerHandler(chessGame);
		SimpleAiPlayerHandler ai2 = new SimpleAiPlayerHandler(chessGame);

		// thiết lập độ thông minh của ai
		ai1.maxDepth = 1;
		ai2.maxDepth = 2;

		// Thiết lập ngươi chơi và máy 
		//chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
		//chessGame.setPlayer(Piece.COLOR_WHITE, chessConsole);
		chessGame.setPlayer(Piece.COLOR_BLACK, ai1);
		//chessGame.setPlayer(Piece.COLOR_BLACK, ai1);
		chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);

		// bắt đầu game
		new Thread(chessGame).start();
	}
	
}