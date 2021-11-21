package engine;

public class Main {

	public static void main(String[] args) {
		System.out.println("Starting chess engine");
		
		Board.of_fen_string("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1").print();
	}
}
