import java.io.*;
import java.net.*;
import java.util.*;
 
/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 *
 * @author www.codejava.net
 */
public class UserThread extends Thread {

	private String clientName = null;
	private ObjectInputStream is = null;
	private ObjectOutputStream os = null;
	private Socket clientSocket = null;
	private final UserThread[] threads;
	private int maxClientsCount;
	private Map<String,List<String>> rooms;



	public UserThread(Socket clientSocket, UserThread[] threads, Map<String, List<String>> rooms) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		this.rooms = rooms;
		maxClientsCount = threads.length;
	}

	public void run() {
		int maxClientsCount = this.maxClientsCount;
		UserThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client
			 */
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				os.writeObject("Enter the desired client name");
				try {
					name = (String)is.readObject();
					if (name.indexOf('@') == -1) {
						break;
					} else {
						os.writeObject("The name cannot contain the character '@'");
					}
				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				}

			}

			System.out.println("Client " + name + " has joined the chatroom");
			os.writeObject("Welcome to the chat room " + name + ". To leave, enter </quit> in a new line.");
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].os.writeObject("A new user " + name + " has joined the chat room");
					}
				}
			}

			while (true) {
				String line;
				try {

					/*
					 * Reading the user command
					 */

					line = (String) is.readObject();

					// Quit if user types /quit
					if (line.startsWith("/quit"))
					{
						break;
					}

					/*
					 * Handle file transfers
					 */

					else if (line.startsWith("file"))
					{
						String[] words = line.split("\\s", 4);

						if(words[1].equals("broadcast"))
						{

							if (words.length > 1 && words[2] != null)
							{
								words[2] = words[2].trim();
								if (!words[2].isEmpty())
								{

									String filepath = words[2];
									File file = new File(filepath);
									byte[] buffer = new byte[(int) file.length()];
									FileInputStream fis = new FileInputStream(file);
									BufferedInputStream in = new BufferedInputStream(fis);
									System.out.println("File broadcasted by " + name);

									in.read(buffer,0,buffer.length);

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
												threads[i].os.writeObject("FILE INCOMING");
												threads[i].os.writeObject(threads[i].clientName.substring(1));
												threads[i].os.writeObject(filepath.substring(filepath.lastIndexOf('/') + 1));
												threads[i].os.write(buffer, 0, buffer.length);
												threads[i].os.flush();
											}
										}
									}
								}
							}

						}

						else if(words[1].equals("unicast"))
						{

							if (words.length > 1 && words[3] != null)
							{
								words[3] = words[3].trim();
								if (!words[3].isEmpty())
								{

									String filepath = words[3];
									File file = new File(filepath);
									byte[] buffer = new byte[(int) file.length()];
									FileInputStream fis = new FileInputStream(file);
									BufferedInputStream in = new BufferedInputStream(fis);
									System.out.println("File unicast by " + name +" to " + words[2].substring(1));

									in.read(buffer,0,buffer.length);

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i] != this
													&& threads[i].clientName != null
													&& threads[i].clientName.equals(words[2])) {
												threads[i].os.writeObject("FILE INCOMING");
												threads[i].os.writeObject(threads[i].clientName.substring(1));
												threads[i].os.writeObject(filepath.substring(filepath.lastIndexOf('/') + 1));
												threads[i].os.write(buffer, 0, buffer.length);
												threads[i].os.flush();
											}
										}
									}
								}
							}

						}

					}

					/*
					 * Handle message transfers
					 */

					else if (line.startsWith("unicast"))
					{
						String[] words = line.split("\\s", 3);
						if (words.length > 1 && words[2] != null)
						{
							words[2] = words[2].trim();
							System.out.println("Message unicast by " + name + " to " + words[1].substring(1) );
							if (!words[2].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i] != this
												&& threads[i].clientName != null
												&& threads[i].clientName.equals(words[1]))
										{
											threads[i].os.writeObject("<<" + name + ">> " + words[2]);
											/*
											 * Echo this message to let the client know the private
											 * message was sent
											 */
											this.os.writeObject("Private message sent to " + words[1].substring(1));
											break;
										}
									}
								}
							}
						}
					}

					else if (line.startsWith("blockcast"))
					{
						String[] words = line.split("\\s", 3);
						if (words.length > 1 && words[2] != null)
						{
							words[2] = words[2].trim();
							System.out.println("Message blockcasted by " + name + " excluding " + words[1].substring(1) );
							if (!words[2].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i] != this
												&& threads[i].clientName != null
												&& !threads[i].clientName.equals(words[1]))
										{
											threads[i].os.writeObject("<" + name + "> " + words[2]);

										}
									}
								}
							}
						}
					}

					else if (line.startsWith("broadcast"))
					{
						String[] words = line.split("\\s", 2);
						if (words.length > 1 && words[1] != null)
						{
							words[1] = words[1].trim();
							System.out.println("Message broadcasted by " + name);
							if (!words[1].isEmpty())
							{
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											threads[i].os.writeObject("<" + name + "> " + words[1]);
										}
									}
								}
							}
						}
					}

					else if (line.startsWith("room")){
						System.out.println("Rooms : " + rooms.keySet());
						String[] words = line.split("\\s", 4);
						// create room
						if(words[1].equals("create")){
							String room_name = words[2];
							System.out.println("Creating room: " + words[2]);
							List<String> client_list = new ArrayList<>();
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null && threads[i] == this ) {
										client_list.add(threads[i].clientName);
										threads[i].rooms.put(room_name,client_list);
									}
								}
							}

						}
						// join room

						if(words[1].equals("join")){
							String room_name = words[2];
							System.out.println("Entering the room :" + words[2]);
							if(rooms.containsKey(words[2])){
								System.out.println("Room is available");
								List<String> client_list = rooms.get(words[2]);
								synchronized (this) {
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] == this ) {
											client_list.add(threads[i].clientName);
											threads[i].rooms.put(room_name,client_list);
										}
									}
								}
							}

						}


						// leave room
						if(words[1].equals("leave"))
						{
							String room_name = words[2];
							System.out.println("Entering the room :" + words[2]);
							if(rooms.containsKey(words[2])){
								System.out.println("Room is available");
								List<String> client_list = rooms.get(words[2]);
								synchronized (this) {
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] == this ) {
											client_list.remove(threads[i].clientName);
											threads[i].rooms.put(room_name,client_list);
										}
									}
								}
							}
						}
						// list room members
						if(words[1].equals("members") && words[2] != null){
							String room_name = words[2];
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
										threads[i].os.writeObject(room_name + " : " + rooms.get(room_name));
									}
								}
							}
						}
						// list roomnames
						if(words[1].equals("names")){
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
										threads[i].os.writeObject(" Rooms available : " + rooms.keySet());
									}
								}
							}
						}






					}

					else if (line.startsWith("enlist_users")) {
						synchronized (this) {
							for (int i = 0; i < maxClientsCount; i++) {
								if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {

									for (int j = 0; j < maxClientsCount; j++)
									{
										threads[i].os.writeObject("-" + threads[j].clientName);
									}
								}
							}
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this
							&& threads[i].clientName != null) {
						threads[i].os.writeObject("The user " + name
								+ " has left the chatroom");
					}
				}
			}
			os.writeObject("~ Bye " + name);
			System.out.println(name + " has left the chatroom");

			/*
			 * Clean up. Set the current thread variable to null so that a new client
			 * could be accepted by the server.
			 */
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, input stream and the socket
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
}