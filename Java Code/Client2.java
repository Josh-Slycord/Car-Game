
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client2 extends Thread{

	private String host;
	private int port;
	public volatile static String carRecieved = "null";
	private String identifier;
	public boolean running = false;

	public Client2(String host, int port, String hostName) {
		this.host = host;
		this.port = port;
		this.identifier = hostName;
	}

	public static synchronized String getCarData(){
		return carRecieved;
	}
	
	public synchronized void setCarData(String newData){
		carRecieved = newData;
	}

	public void setRunning(boolean isRunning){
		this.running = isRunning;	
	}
	
	public void run() {
		while(!running){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
		
		Socket client = null;
		try {
			client = new Socket(host, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Client successfully connected to server!");

		// create a new thread for server messages handling
		try {
			new Thread(new ReceivedMessagesHandler2(client.getInputStream())).start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		PrintStream output = null;
		try {
			output = new PrintStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {	
			try{
			
			output.println(identifier + "," + getCarData());
			output.flush();
			Thread.sleep(5);
			}catch(Exception e){
				try {
					output = new PrintStream(client.getOutputStream());
				} catch (IOException e2) {
					e.printStackTrace();
				}
			}
			
		}
	}
}

class ReceivedMessagesHandler2 implements Runnable {

	private InputStream server;

	public ReceivedMessagesHandler2(InputStream server) {
		this.server = server;
	}

	public void run() {
		Scanner s = new Scanner(server);
		while (s.hasNextLine()) {
			System.out.println(s.nextLine());
		}
		s.close();
	}
}