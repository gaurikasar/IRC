

import java.io.*;
import java.net.*;
import java.util.*;
 
/**
 * This is the chat server program.
 * Press Ctrl + C to terminate the program.
 * @author www.codejava.net
 */
public class ChatServer {
    private static final int totalClientsCount = 20;
	private static final UserThread[] threads = new UserThread[totalClientsCount];
	private static Socket connectServer;
	private static ServerSocket serverSocket = null;
	public static volatile Map<String, List<String>> rooms= new HashMap<>();
 
   
    public static void main(String[] args) {
    	int port = 1002;
        if (args.length < 1) {
            System.out.println("Server is started at default port"+port);
        }
        else {
        	
        	 port = Integer.valueOf(args[0]).intValue();
        	 System.out.println("Server is started at specified port"+port);
        }
       
 
        try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        while (true) {
			try {
				connectServer = serverSocket.accept();
				int i = 0;
				for (i = 0; i < totalClientsCount; i++) {
					if (threads[i] == null) {
						(threads[i] = new UserThread(connectServer, threads,rooms)).start();
						break;
					}
				}
				if (i == totalClientsCount) {
					ObjectOutputStream os = new ObjectOutputStream(connectServer.getOutputStream());
					os.writeObject("Max client connection reached, try later.");
					os.close();
					connectServer.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
    }
}
 
