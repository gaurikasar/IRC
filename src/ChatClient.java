import java.net.*;
import java.io.*;
 
/**
 * This is the chat client program.
 * Type 'bye' to terminte the program.
 *
 * @author www.codejava.net
 */
public class ChatClient {
	private static Socket clientSocket = null;
	private static ObjectOutputStream outputStrem = null;
	private static ObjectInputStream inputStream = null;
	private static BufferedReader input = null;
	private static boolean connectionClose = false;

	public static void main(String[] args) {

		// The default port
		int portNumber = 2222;
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
			outputStrem = new ObjectOutputStream(clientSocket.getOutputStream());
			inputStream = new ObjectInputStream(clientSocket.getInputStream());
			read = new ReadThread(input, inputStream);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		if (clientSocket != null && outputStrem != null && inputStream != null) {
			try {
				new Thread(read).start();
				connectionClose = read.isClosed();
				while (!connectionClose) {
					System.out.println("Please enter your command on the next line");
					outputStrem.writeObject(input.readLine().trim());
				}
				
				/*
				 * Close the output stream, input stream and the socket
				 */
				outputStrem.close();
				inputStream.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}
}