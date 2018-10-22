import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

	public Physics physicsThread;
	private int port;
	private List<PrintStream> clients;
	private ServerSocket server;
	public volatile static String carRecieved = "null";
	static volatile Client2 player2;
	static volatile Client3 player3;

	// /////////////////////////////////////////////////////////
	private static String player2IP = "192.168.1.100"; //Matt
	private static String player3IP = "192.168.1.106"; //Steve
	public static String identifier = "Josh";

	// /////////////////////////////////////////////////////////

	public static void main(String[] args) throws IOException {
		new Server(444).run();
	}

	public synchronized static String getCarData() {
		return carRecieved;
	}

	public synchronized void setCarData(String newData) {
		carRecieved = newData;
	}

	public Server(int port) {
		this.port = port;
		this.clients = new ArrayList<PrintStream>();
	}

	public void run() {
		try {
			server = new ServerSocket(port) {
				protected void finalize() throws IOException {
					this.close();
				}
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Port 444 is now open.");

		physicsThread = new Physics(this);
		physicsThread.setRunning(true);
		physicsThread.start();
		
	
////////////////////Player 2///////////////////////////////
//		player2 = new Client2(player2IP, port, identifier);
//		try {
//			Thread.sleep(5);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//		player2.start();
////////////////////Player 2///////////////////////////////

////////////////////Player 3///////////////////////////////		
//		try {
//			Thread.sleep(5);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//
//		player3 = new Client3(player3IP, port, identifier);
//		try {
//			Thread.sleep(5);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//		player3.start();
////////////////////Player 3///////////////////////////////
		try {
			Thread.sleep(5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while (true) {
			// accepts a new client
			Socket client = null;
			try {
				client = server.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Connection established with client: "
					+ client.getInetAddress().getHostAddress());

			// add client message to list
			try {
				this.clients.add(new PrintStream(client.getOutputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}

			// create a new thread for client handling
			try {
				new Thread(new ClientHandler(this, client.getInputStream()))
						.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void broadcastMessages(String msg) {
		for (PrintStream client : this.clients) {
			client.println(msg);
		}
	}
}

class ClientHandler implements Runnable {

	private Server server;
	private InputStream client;
	public volatile static String[] carRecieved = { "null", "null", "null" };
	static volatile boolean first = true;

	public ClientHandler(Server server, InputStream client) {
		this.server = server;
		this.client = client;
	}

	public void run() {
		String message = null;
		String[] buffer;
		String[] physicsRecieved;
		String[] player2Recieved = {"null", "null", "null", "null"};
		String[] player3Recieved = {"null", "null", "null", "null"};
		String identifierPlayer2;
		String identifierPlayer3;
		String x;

		// ///////////////////////////////////////////////
		String identifier = "Josh";
		identifierPlayer2 = "Matt";
		identifierPlayer3 = "Stev";
		// ///////////////////////////////////////////////

//		if (first) {
//			System.out.println("All Servers Running?");
//			Scanner s = new Scanner(System.in);
//			x = s.nextLine();
//
//			Server.player2.setRunning(true);
//			Server.player3.setRunning(true);
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//			s.close();
//			System.out.println("Starting");
//			first = false;
//		}

		Scanner sc = new Scanner(this.client);

/////////////////////Leave This Commented////////////////////////
//      //Get Nickname from phone function 
//		while (sc.hasNextLine() && identifier.contains("null")) {
//			if (sc.nextLine().contains("name")
//					&& !(sc.nextLine().contains("null"))) {
//				physicsRecieved = sc.nextLine().split(",");
//				identifier = physicsRecieved[1];
//			}
//		}

		while (true) {

			if (sc.hasNextLine()) {
				try {
					if (sc.nextLine().contains("physics")
							&& !(sc.nextLine().contains("null"))) {
						physicsRecieved = sc.nextLine().split(",");
						Physics.gasOn = Boolean
								.parseBoolean(physicsRecieved[2]);
						Physics.brakeOn = Boolean
								.parseBoolean(physicsRecieved[3]);
						Physics.engineOn = Boolean
								.parseBoolean(physicsRecieved[4]);
						Physics.impact = Boolean
								.parseBoolean(physicsRecieved[5]);
						Physics.mCurrAngle = Double
								.parseDouble(physicsRecieved[6]);
//						Server.player2
//								.setCarData(physicsRecieved[7] + ","
//										+ physicsRecieved[8] + ","
//										+ physicsRecieved[9]);
//						Server.player3
//								.setCarData(physicsRecieved[7] + ","
//										+ physicsRecieved[8] + ","
//										+ physicsRecieved[9]);
					}

//					if (sc.nextLine().contains(identifierPlayer2)
//							&& !(sc.nextLine().contains("null"))) {
//						player2Recieved = sc.nextLine().split(",");
//
//					}
//					
//					if (sc.nextLine().contains(identifierPlayer3)
//							&& !(sc.nextLine().contains("null"))) {
//						player3Recieved = sc.nextLine().split(",");
//					}

				} catch (Exception e) {
					server.broadcastMessages("Error 1");
				}

				buffer = Physics.returnVariables;
				message = identifier + "," + buffer[0] + "," + buffer[1] + ","
						+ buffer[2] + "," + buffer[3] + "," + buffer[4] + ","
						+ buffer[5] + "," + buffer[6];
				
//				message = message + "," + player2Recieved[1] + "," + player2Recieved[2] + "," + player2Recieved[3];
				
//				message = message + "," + player3Recieved[1] + "," + player3Recieved[2] + "," + player3Recieved[3];				

				server.broadcastMessages(message);

			}
		}
		// sc.close();
	}
}