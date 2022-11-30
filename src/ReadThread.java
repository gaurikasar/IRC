import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import java.net.*;
 
/**
 * Thread
 * read input from server and print it on console
 * Infinite while loop to run until client disconnected from server
 *
 */
public class ReadThread extends  Thread {

    BufferedReader input;
	private ObjectInputStream is = null;
	private boolean closed=false;
	public boolean isClosed() {
		return closed;
	}
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	public ReadThread(BufferedReader inputLine, ObjectInputStream is2) {
		input=inputLine;
		is=is2;
}
	public void run() {
    	String inputResponse;

		try {
			while ((inputResponse = (String)is.readObject()) != null) {
					System.out.println(inputResponse);
					if (inputResponse.indexOf("~ Bye") != -1)
						break;
			}
			closed = true;
			System.out.println("Exiting......");
		} catch (IOException e) {
			closed = true;
			System.err.println("IOException:  " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}