
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client extends Thread {

	private String host;
	private int port;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		new Client("localhost", 444).run();
	}

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() {
		// connect client to server
		Socket client = null;
		try {
			client = new Socket(host, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Client successfully connected to server!");

		// create a new thread for server messages handling
		try {
			new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ask for a nickname
		Scanner sc = new Scanner(System.in);

		PrintStream output = null;
		try {
			output = new PrintStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (sc.hasNextLine()) {
		
		}
		
		output.close();
		sc.close();
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ReceivedMessagesHandler implements Runnable {

	private InputStream server;

	public ReceivedMessagesHandler(InputStream server) {
		this.server = server;
	}

	public void run() {
		// receive server messages and print out to screen
		Scanner s = new Scanner(server);
		while (s.hasNextLine()) {
			System.out.println(s.nextLine());
		}
		s.close();
	}
}