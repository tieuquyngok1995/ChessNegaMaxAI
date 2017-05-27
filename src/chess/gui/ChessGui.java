package chess.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chess.logic.ChessGame;
import chess.logic.IPlayerHandler;
import chess.logic.Move;
import chess.logic.MoveValidator;
import chess.logic.Piece;

/**
 * các vị trí được tính từ bên trái qua
 * ds đc coi là 0 và kích thước -1 là phần đầu
 * 
 */
public class ChessGui extends JPanel implements IPlayerHandler{
	
	private static final long serialVersionUID = -8207574964820892354L;
	
	private static final int BOARD_START_X = 299;
	private static final int BOARD_START_Y = 49;

	private static final int SQUARE_WIDTH = 50;
	private static final int SQUARE_HEIGHT = 50;

	private static final int PIECE_WIDTH = 48;
	private static final int PIECE_HEIGHT = 48;
	
	private static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH/2.0 - PIECE_WIDTH/2.0);
	private static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT/2.0 - PIECE_HEIGHT/2.0);
	
	private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
	private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

	private Image imgBackground;
	private JLabel lblGameState;
	
	private ChessGame chessGame;
	private List<GuiPiece> guiPieces = new ArrayList<GuiPiece>();

	private GuiPiece dragPiece;

	private Move lastMove;
	private Move currentMove;

	private boolean draggingGamePiecesEnabled;

	/**
	 * constructor - tạo giao diện người dùng
	 * @param chessGame
	 */
	public ChessGui(ChessGame chessGame) {
		this.setLayout(null);

		// background
		URL urlBackgroundImg = getClass().getResource("/gui/img/bo.png");
		this.imgBackground = new ImageIcon(urlBackgroundImg).getImage();
		
		// create chess game
		this.chessGame = chessGame;
		
		
		//xắp xếp thêm các giao diện đồ họa vào game
		for (Piece piece : this.chessGame.getPieces()) {
			createAndAddGuiPiece(piece);
		}
		

		// thêm sự kiện và nắm bắt kéo thả 
		//
		PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this.guiPieces,
				this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);

		// label to display game state
		String labelText = this.getGameStateAsText();
		this.lblGameState = new JLabel(labelText);
		lblGameState.setBounds(0, 30, 80, 30);
		lblGameState.setForeground(Color.WHITE);
		this.add(lblGameState);

		// tạo khung và cài đặt hiển thị
		//
		JFrame f = new JFrame();
		f.setSize(80, 80);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.setSize(imgBackground.getWidth(null), imgBackground.getHeight(null));
	}

	/**
	 * @return mô tả trạng thái trò chơi
	 */
	private String getGameStateAsText() {
		String state = "unknown";
		switch (this.chessGame.getGameState()) {
			case ChessGame.GAME_STATE_BLACK: state = "black";break;
			case ChessGame.GAME_STATE_END_WHITE_WON: state = "white won";break;
			case ChessGame.GAME_STATE_END_BLACK_WON: state = "black won";break;
			case ChessGame.GAME_STATE_WHITE: state = "white";break;
		}
		return state;
	}

	/**
	 * tạo các quân cờ 
	 * 
	 * @param color color constant
	 * @param type type constant
	 * @param x x position of upper left corner
	 * @param y y position of upper left corner
	 */
	private void createAndAddGuiPiece(Piece piece) {
		Image img = this.getImageForPiece(piece.getColor(), piece.getType());
		GuiPiece guiPiece = new GuiPiece(img, piece);
		this.guiPieces.add(guiPiece);
	}

	/**
	 * tải hình ảnh và màu cho các quân cờ cụ thể
	 * @param color color constant
	 * @param type type constant
	 * @return image
	 */
	private Image getImageForPiece(int color, int type) {

		String filename = "";

		filename += (color == Piece.COLOR_WHITE ? "w" : "b");
		switch (type) {
			case Piece.TYPE_BISHOP:
				filename += "b";
				break;
			case Piece.TYPE_KING:
				filename += "k";
				break;
			case Piece.TYPE_KNIGHT:
				filename += "n";
				break;
			case Piece.TYPE_PAWN:
				filename += "p";
				break;
			case Piece.TYPE_QUEEN:
				filename += "q";
				break;
			case Piece.TYPE_ROOK:
				filename += "r";
				break;
		}
		filename += ".png";

		URL urlPieceImg = getClass().getResource("/gui/img/" + filename);
		return new ImageIcon(urlPieceImg).getImage();
	}

	@Override
	protected void paintComponent(Graphics g) {

		// vẽ hình nền
		g.drawImage(this.imgBackground, 0, 0, null);

		// vẽ quân cờ
		for (GuiPiece guiPiece : this.guiPieces) {
			if( !guiPiece.isCaptured()){
				g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
			}
		}
		
		// tạo các động tác cuối cùng nếu k phải kéo quân cờ
		if( !isUserDraggingPiece() && this.lastMove != null ){
			int highlightSourceX = convertColumnToX(this.lastMove.sourceColumn);
			int highlightSourceY = convertRowToY(this.lastMove.sourceRow);
			int highlightTargetX = convertColumnToX(this.lastMove.targetColumn);
			int highlightTargetY = convertRowToY(this.lastMove.targetRow);
			
			g.setColor(Color.YELLOW);
			g.drawRoundRect( highlightSourceX+4, highlightSourceY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
			g.drawRoundRect( highlightTargetX+4, highlightTargetY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
			//g.drawLine(highlightSourceX+SQUARE_WIDTH/2, highlightSourceY+SQUARE_HEIGHT/2
			//		, highlightTargetX+SQUARE_WIDTH/2, highlightTargetY+SQUARE_HEIGHT/2);
		}
		
		// vẽ vị trí quân cờ nếu ng dùng đang kéo 1 quân cờ
		if( isUserDraggingPiece() ){
			
			MoveValidator moveValidator = this.chessGame.getMoveValidator();
			
			// lặp bảng để kiểm tra vị trí bảng
			for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
				for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
					int sourceRow = this.dragPiece.getPiece().getRow();
					int sourceColumn = this.dragPiece.getPiece().getColumn();
					
					// kiểm tra xem vị trí đích có hợp lệ
					if( moveValidator.isMoveValid( new Move(sourceRow, sourceColumn, row, column), false) ){
						
						int highlightX = convertColumnToX(column);
						int highlightY = convertRowToY(row);
						
						// vẽ bóng vị trí thả quân
						g.setColor(Color.BLACK);
						g.drawRoundRect( highlightX+5, highlightY+5, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
						// vẽ màu nổi bật
						g.setColor(Color.GREEN);
						g.drawRoundRect( highlightX+4, highlightY+4, SQUARE_WIDTH-8, SQUARE_HEIGHT-8,10,10);
					}
				}
			}
		}
		
		
		// vẽ nhãn trạng thái game
		this.lblGameState.setText(this.getGameStateAsText());
	}

	/**
	 * kiểm tra việc kéo 1 quân cờ
	 * @return true - ng dùng đang kéo 1 quân cờ
	 */
	private boolean isUserDraggingPiece() {
		return this.dragPiece != null;
	}

	/**
	 * @return trạng thái trò chơi hiện tại
	 */
	public int getGameState() {
		return this.chessGame.getGameState();
	}
	
	/**
	 * chuyển đổi cột logic
	 * @param column
	 * @return tọa độ cho cột
	 */
	public static int convertColumnToX(int column){
		return PIECES_START_X + SQUARE_WIDTH * column;
	}
	
	/**
	 * Chuyển đổi hàng thành tọa độ của y
	 * @param row
	 * @return to do y cho hàng
	 */
	public static int convertRowToY(int row){
		return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
	}
	
	/**
	 * chuyển đổi cột thành tọa độ x
	 * @param x
	 * @return toa do x cho cột
	 */
	public static int convertXToColumn(int x){
		return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
	}
	
	/**
	 * chuyen doi cot y thanh hang
	 * @param y
	 * @return hàng hợp lý cho tọa đô y
	 */
	public static int convertYToRow(int y){
		return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y)/SQUARE_HEIGHT;
	}

	/**
	 * thay đổi vị trí của quân cờ cho trước nếu vị trí đó hợp lệ
	 * neus vị tri đó k hợp lệ trả quân cờ về vị trí gốc
	 * @param dragPiece
	 * @param x
	 * @param y
	 */
	public void setNewPieceLocation(GuiPiece dragPiece, int x, int y) {
		int targetRow = ChessGui.convertYToRow(y);
		int targetColumn = ChessGui.convertXToColumn(x);
		
		Move move = new Move(dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn()
				, targetRow, targetColumn);
		if( this.chessGame.getMoveValidator().isMoveValid(move, true) ){
			this.currentMove = move;
		}else{
			dragPiece.resetToUnderlyingPiecePosition();
		}
	}

	/**
	 * đặt phần trò chơi hiện tại đc kéo bởi ng dùng
	 * @param guiPiece
	 */
	public void setDragPiece(GuiPiece guiPiece) {
		this.dragPiece = guiPiece;
	}
	
	/**
	 * trả về phần giao diện mà ng dùng đang kéo
	 * @return phần giao diện mà ng dùng đang kéo
	 */
	public GuiPiece getDragPiece(){
		return this.dragPiece;
	}

	@Override
	public Move getMove() {
		this.draggingGamePiecesEnabled = true; 
		Move moveForExecution = this.currentMove;
		this.currentMove = null;
		return moveForExecution;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		// điều chỉnh mảnh các quân cờ
		GuiPiece guiPiece = this.getGuiPieceAt(move.targetRow, move.targetColumn);
		if( guiPiece == null){
			throw new IllegalStateException("no guiPiece at "+move.targetRow+"/"+move.targetColumn);
		}
		guiPiece.resetToUnderlyingPiecePosition();
		
		// ghi nhớ nước đi cuối
		this.lastMove = move;
		
		// vô hiệu hóa kéo cho đến khi game yêu cầu trò chơi tiếp
		this.draggingGamePiecesEnabled = false;
				
		// tạo lại trạng thái mới
		this.repaint();
		
	}
	
	/**
	 * @return true -nếu ng dùng đc phép kéo quân cờ
	 */
	public boolean isDraggingGamePiecesEnabled(){
		return draggingGamePiecesEnabled;
	}

	/**
	 * lấy quân không ở vị trí quy đinh
	 * @param row
	 * @param column
	 * @return các quân cờ ở vị trí quy đinh, trống nếu không có quân cờ
	 */
	private GuiPiece getGuiPieceAt(int row, int column) {
		for (GuiPiece guiPiece : this.guiPieces) {
			if( guiPiece.getPiece().getRow() == row
					&& guiPiece.getPiece().getColumn() == column
					&& guiPiece.isCaptured() == false){
				return guiPiece;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		ChessGame chessGame = new ChessGame();
		ChessGui chessGui = new ChessGui(chessGame);
		chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
		chessGame.setPlayer(Piece.COLOR_BLACK, chessGui);
		new Thread(chessGame).start();
	}
}
