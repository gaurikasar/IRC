import java.net.*;
import java.io.*;
 
/**
*Class
 * Initiates socket for connection
 * Sends request to the server
 */
public class ChatClient {
	private static Socket clientSocket = null;
	private static String username;
	private static ObjectOutputStream outputStream = null;
	private static ObjectInputStream inputStream = null;
	private static BufferedReader input = null;
	private static boolean connectionClose = false;

	public static void main(String[] args) {

		// The default port
		int portNumber = 1002;
		// The default host
		String host = "localhost";
		ReadThread read = null;

		if (args.length < 2) {
			System.out.println("Client connected to " + host + " on portNumber " + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open the socket connection, input and output streams
		 */
		try {
			clientSocket = new Socket(host, portNumber);
			input = new BufferedReader(new InputStreamReader(System.in));
			outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			inputStream = new ObjectInputStream(clientSocket.getInputStream());
			read = new ReadThread(input, inputStream);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		if (clientSocket != null && outputStream != null && inputStream != null) {
			try {
				Runtime.getRuntime().addShutdownHook(new ShutDownHook(inputStream,outputStream,clientSocket));
				read.start();
				while (!read.isClosed()) {
					outputStream.writeObject(input.readLine().trim());
				}
				System.out.println("Outside while loop");
				outputStream.writeObject("/quit");
				
				/*
				 * Close the output stream, input stream and the socket
				 */
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	public void setUserName(String userName) {
		this.username = userName;
	}
}