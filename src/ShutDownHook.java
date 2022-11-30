import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This thread is responsible for shutting down everything gracefully
 */
public class ShutDownHook extends  Thread {

	private Socket clientSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	public ShutDownHook(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
		this.input = in;
		this.output = out;
		this.clientSocket = socket;
}
	public void run() {

		try {
			System.out.println("Closing everything...");
			output.writeObject("/quit");
			output.close();
			input.close();
			clientSocket.close();

		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
    }
}