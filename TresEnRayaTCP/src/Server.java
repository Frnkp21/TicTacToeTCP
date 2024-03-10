import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final int PORT = 12345;
    private static List<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server running. Waiting for clients...");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                clients.add(clientSocket);

                if (clients.size() == 2) {
                    System.out.println("Enter name for Player 1:");
                    String player1Name = scanner.nextLine();
                    System.out.println("Enter name for Player 2:");
                    String player2Name = scanner.nextLine();
                    // Imprime los nombres de los jugadores
                    System.out.println("Player 1: " + player1Name);
                    System.out.println("Player 2: " + player2Name);
                    Thread gameThread = new Thread(new GameHandler(clients.get(0), clients.get(1), player1Name, player2Name));
                    gameThread.start();
                    clients.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class GameHandler implements Runnable {
    private final Socket player1;
    private final Socket player2;
    private final TicTacToe game;
    private final String player1Name;
    private final String player2Name;

    public GameHandler(Socket player1, Socket player2, String player1Name, String player2Name) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.game = new TicTacToe();
    }

    @Override
    public void run() {
        try (
                PrintWriter output1 = new PrintWriter(player1.getOutputStream(), true);
                BufferedReader input1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                PrintWriter output2 = new PrintWriter(player2.getOutputStream(), true);
                BufferedReader input2 = new BufferedReader(new InputStreamReader(player2.getInputStream()))
        ) {
            output1.println("You are Player 1: " + player1Name);
            output2.println("You are Player 2: " + player2Name);


            while (true) {
                // Turno del jugador 1
                output1.println("Enter row (0-2) and column (0-2):");
                String move1 = input1.readLine();
                String[] parts1 = move1.split(" ");
                int row1 = Integer.parseInt(parts1[0]);
                int col1 = Integer.parseInt(parts1[1]);
                game.placeMark(row1, col1);
                game.switchPlayer(); // Cambia el jugador después de cada turno

                String boardState = game.getBoardAsString();
                output1.println(boardState);
                output2.println(boardState);

                // Verifica si el jugador 1 gana o si el juego termina en empate
                if (game.checkForWin() || game.isBoardFull()) {
                    endGame(output1, output2);
                    break;
                }

                // Turno del jugador 2
                output2.println("Enter row (0-2) and column (0-2):");
                String move2 = input2.readLine();
                String[] parts2 = move2.split(" ");
                int row2 = Integer.parseInt(parts2[0]);
                int col2 = Integer.parseInt(parts2[1]);
                game.placeMark(row2, col2);
                game.switchPlayer(); // Cambia el jugador después de cada turno

                // Envía el estado actualizado del tablero después del movimiento del jugador 2
                boardState = game.getBoardAsString();
                output1.println(boardState);
                output2.println(boardState);

                // Verifica si el jugador 2 gana o si el juego termina en empate
                if (game.checkForWin() || game.isBoardFull()) {
                    endGame(output1, output2);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void endGame(PrintWriter output1, PrintWriter output2) {
        if (game.checkForWin()) {
            // Obtener el nombre del jugador ganador basado en su símbolo
            String winnerName = (game.getCurrentPlayer() == 'X') ? player1Name : player2Name;

            output1.println("Player " + winnerName + " wins!");
            output2.println("Player " + winnerName + " wins!");
        } else {
            output1.println("It's a draw!");
            output2.println("It's a draw!");
        }
    }
}
