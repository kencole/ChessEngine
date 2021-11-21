package engine;

import java.util.ArrayList;

public class Board {
	static final public byte _A = 0;
	static final public byte _B = 1;
	static final public byte _C = 2;
	static final public byte _D = 3;
	static final public byte _E = 4;
	static final public byte _F = 5;
	static final public byte _G = 6;
	static final public byte _H = 7;
	static final public byte _1 = 7 * 8;
	static final public byte _2 = 6 * 8;
	static final public byte _3 = 5 * 8;
	static final public byte _4 = 4 * 8;
	static final public byte _5 = 3 * 8;
	static final public byte _6 = 2 * 8;
	static final public byte _7 = 1 * 8;
	static final public byte _8 = 0 * 8;

	static final public byte empty = 0;
	static final public byte pawn = 1;
	static final public byte rook = 2;
	static final public byte knight = 3;
	static final public byte bishop = 4;
	static final public byte queen = 5;
	static final public byte king = 6;
	
	static final public byte black = -1;
	static final public byte white = 1;
	
	static final public byte white_castle_kingside = 1 << 0;
	static final public byte white_castle_queenside = 1 << 1;
	static final public byte black_castle_kingside = 1 << 2;
	static final public byte black_castle_queenside = 1 << 3;
	
	protected byte[] board;
	protected byte active_color;
	protected byte castle_ability;
	protected byte en_pessant_square;
	protected byte halfmove;
	protected byte move;
	
	public class Move {
		private short move;
		
		public Move(byte start, byte end) {
			this.move = (short) (((short) start) << 8 | end);
		}
		public byte start() {
			return (byte) (this.move >> 8);
		}
	}
	
	public Board(byte[] board, byte active_color, byte castle_ability, byte en_pessant_square, byte halfmove, byte move) {
		this.board = board;
		this.active_color = active_color;
		this.castle_ability = castle_ability;
		this.en_pessant_square = en_pessant_square;
		this.halfmove = halfmove;
		this.move = move;
	}
	
	private static String idx_to_square(byte idx) {
		String files = "abcdefgh";
		byte file = (byte) (idx % 8);
		byte rank = (byte) (8 - idx / 8);
		return "" + files.charAt(file) + rank;
	}
	
	private static byte[] board(String fen) {
		int idx = 0;
		byte[] board = new byte[64];
		for (int i = 0; i < board.length; i++) {
			board[i] = empty;
		}
		for (char c : fen.toCharArray()) {
			switch (c) {
			case 'p': board[idx] = (byte) (black * pawn); idx++; break;
			case 'P': board[idx] = (byte) (white * pawn); idx++; break;
			case 'r': board[idx] = (byte) (black * rook); idx++; break;
			case 'R': board[idx] = (byte) (white * rook); idx++; break;
			case 'n': board[idx] = (byte) (black * knight); idx++; break;
			case 'N': board[idx] = (byte) (white * knight); idx++; break;
			case 'b': board[idx] = (byte) (black * bishop); idx++; break;
			case 'B': board[idx] = (byte) (white * bishop); idx++; break;
			case 'q': board[idx] = (byte) (black * queen); idx++; break;
			case 'Q': board[idx] = (byte) (white * queen); idx++; break;
			case 'k': board[idx] = (byte) (black * king); idx++; break;
			case 'K': board[idx] = (byte) (white * king); idx++; break;
			case '/': idx = ((idx + 7) / 8) * 8; break;
			default:
				idx += Byte.parseByte(String.valueOf(c));
				break;
			}
		}
		return board;
	}
	
	private static byte active_color(String fen1) {
		switch (fen1) {
		case "w": return white;
		case "b": return black;
		default: 
		    throw new java.lang.RuntimeException("Invalid active_color_fen: " + fen1);
		}
	}
	
	private static byte castle_ability(String fen2) {
		byte castle_ability = 0;
		if(fen2.contains("K")) {
			castle_ability += white_castle_kingside;
		}
		if(fen2.contains("Q")) {
			castle_ability += white_castle_queenside;
		}
		if(fen2.contains("k")) {
			castle_ability += black_castle_kingside;
		}
		if(fen2.contains("q")) {
			castle_ability += black_castle_queenside;
		}
		return castle_ability;
	}

	private static byte en_pessant_square(String fen3) {
		if (fen3.equals("-")) {
			return 0;
		}
		byte en_pessant_square = 0;
		switch (fen3.charAt(0)) {
		case 'a': en_pessant_square += _A; break;
		case 'b': en_pessant_square += _B; break;
		case 'c': en_pessant_square += _C; break;
		case 'd': en_pessant_square += _D; break;
		case 'e': en_pessant_square += _E; break;
		case 'f': en_pessant_square += _F; break;
		case 'g': en_pessant_square += _G; break;
		case 'h': en_pessant_square += _H; break;
		default: throw new java.lang.RuntimeException("Invalid en pessant square: " + fen3);
		}
		switch (fen3.charAt(1)) {
		case '1': en_pessant_square += _1; break;
		case '2': en_pessant_square += _2; break;
		case '3': en_pessant_square += _3; break;
		case '4': en_pessant_square += _4; break;
		case '5': en_pessant_square += _5; break;
		case '6': en_pessant_square += _6; break;
		case '7': en_pessant_square += _7; break;
		case '8': en_pessant_square += _8; break;
		default: throw new java.lang.RuntimeException("Invalid en pessant square: " + fen3);
		}
		return en_pessant_square;
	}
	
	private void print_castle_ability(boolean kingside, boolean queenside, String side) {
		if (kingside && queenside) {
			System.out.println(side + " can castle both ways");
		} else if (kingside) {
			System.out.println(side + " can castle kingside");			
		} else if (queenside) {
			System.out.println(side + " can castle queenside");			
		} else {
			System.out.println(side + " cannot castle");
		}
	}
	
	public void print() {
		System.out.print("Move " + move + ". ");
		System.out.println((active_color == white ? "White" : "Black") + "'s turn:");

		boolean white_castle_kingside = (Board.white_castle_kingside & castle_ability) != 0;
        boolean white_castle_queenside = (Board.white_castle_queenside & castle_ability) != 0;
        boolean black_castle_kingside = (Board.black_castle_kingside & castle_ability) != 0;
		boolean black_castle_queenside = (Board.black_castle_queenside & castle_ability) != 0;
		print_castle_ability(white_castle_kingside, white_castle_queenside, "White");
		print_castle_ability(black_castle_kingside, black_castle_queenside, "Black");	
		
		if (en_pessant_square != 0) {
			System.out.println("En pessant can be performed on square: "
		                       + idx_to_square(en_pessant_square));
		}
		System.out.println("Halfmoves since capture or pawn move: " + halfmove);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				byte piece = board[i * 8 + j];
				if (piece < 0) {
					System.out.print(" " + piece);
				} else {
					System.out.print("  " + piece);
				}
			}
			System.out.println();
		}
	}
	
	public static Board of_fen_string(String fen) {
		String[] strings = fen.split(" ");
//		for (String string : strings) {
//			System.out.println(string);
//		}
		if (strings.length != 6) {
		    throw new java.lang.RuntimeException("Invalid Fen: " + fen);
		}
		byte[] board = board(strings[0]);
		byte active_color = active_color(strings[1]);
		byte castle_ability = castle_ability(strings[2]);
		byte en_pessant_square = en_pessant_square(strings[3]);
		byte halfmove = Byte.parseByte(strings[4]);
		byte move = Byte.parseByte(strings[5]);
		
		return new Board(board, active_color, castle_ability, en_pessant_square, halfmove, move);
	}
	
	private String board_string() {
		String board_string = "";
		for (int i = 0; i < 8; i++) {
			byte empties_past = 0;
			for (int j = 0; j < 8; j++) {
				byte curr_square = board[i * 8 + j];
				if (curr_square != empty && empties_past > 0) {
					board_string += empties_past;
					empties_past = 0;
				}
				switch (curr_square) {
					case empty: empties_past += 1; break;
					case (byte) (white * pawn):   board_string += "P"; break;
					case (byte) (white * rook):   board_string += "R"; break;
					case (byte) (white * knight): board_string += "N"; break;
					case (byte) (white * bishop): board_string += "B"; break;
					case (byte) (white * queen):  board_string += "Q"; break;
					case (byte) (white * king):   board_string += "K"; break;
					case (byte) (black * pawn):   board_string += "p"; break;
					case (byte) (black * rook):   board_string += "r"; break;
					case (byte) (black * knight): board_string += "n"; break;
					case (byte) (black * bishop): board_string += "b"; break;
					case (byte) (black * queen):  board_string += "q"; break;
					case (byte) (black * king):   board_string += "k"; break;
				}
			}
			if (empties_past > 0) {
				board_string += empties_past;
			}
			if (i < 7) board_string += "/"; 
		}
		return board_string;
	}
	
	private String castle_ability_string() {
		boolean white_castle_kingside = (Board.white_castle_kingside & castle_ability) != 0;
        boolean white_castle_queenside = (Board.white_castle_queenside & castle_ability) != 0;
        boolean black_castle_kingside = (Board.black_castle_kingside & castle_ability) != 0;
		boolean black_castle_queenside = (Board.black_castle_queenside & castle_ability) != 0;
		String castle_ability = "";
		if (white_castle_kingside) castle_ability += "K";
		if (white_castle_queenside) castle_ability += "Q";
		if (black_castle_kingside) castle_ability += "k";
		if (black_castle_queenside) castle_ability += "q";
		return (castle_ability == "" ? "-" : castle_ability);
	}
	
	public String to_fen_string() {
		String board_string = this.board_string();
		return board_string 
		  + " " + (active_color == white ? "w" : "b")
		  + " " + this.castle_ability_string()
		  + " " + (en_pessant_square != empty ? idx_to_square(en_pessant_square) : "-")
		  + " " + halfmove
		  + " " + move;
	}
	
	static {
		String[] testcases = {
				"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
				"rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
				"8/4b3/4P3/1k4P1/8/ppK5/8/4R3 b - - 1 45"};
		for (String test : testcases) {
			String roundtrip = Board.of_fen_string(test).to_fen_string();
			if (!test.equals(roundtrip)) {
				System.out.println("test failure:");
				System.out.println("input:     \"" + test + "\"");
				System.out.println("roundtrip: \"" + roundtrip + "\"");
				System.exit(1);;
			}
		}
	}
}
