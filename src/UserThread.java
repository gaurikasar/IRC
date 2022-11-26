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
	private EncryptionHelper encryptionHelper = new EncryptionHelper();
	private String formerEncryptedString = "";



	public UserThread(Socket clientSocket, UserThread[] threads, Map<String, List<String>> rooms) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		this.rooms = rooms;
		maxClientsCount = threads.length;
	}

	private String getMessage(String[] array, int startIndex) {
		String message = "";
		for(int i=startIndex;i < array.length;++i) {
			message = message + array[i] + " ";
		}
		return message;
	}
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		UserThread[] threads = this.threads;

		try {
			//input and output streams for this client
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				os.writeObject("Enter your name");
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
			os.writeObject("Welcome to the chat room " + name + ". To exit, type </quit> in a new line.");
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						threads[i].os.writeObject("Welcome new user " + name + " to the chat room");
					}
				}
			}

			while (true) {
				String line;
				try {
					//Read the client command
					 line = (String) is.readObject();

					// manage command to exit the application
					if (line.startsWith("/quit"))
					{
						break;
					}

					//Manage file transfers
					 else if (line.startsWith("file"))
					{
						String[] words = line.split("\\s", 4);
						if(words[1].equals("send_all"))
						{
							System.out.println("word array: "+words);
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
									System.out.println("File sent by " + name);

									in.read(buffer,0,buffer.length);
									//OutputStream output = clientSocket.getOutputStream();
									//output.write(buffer, 0, );

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
												threads[i].os.writeObject("File In Transit....");
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
/*
						else if(words[1].equals("send_to"))
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
									System.out.println("File sent by " + name +" to " + words[2].substring(1));

									in.read(buffer,0,buffer.length);

									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i] != this
													&& threads[i].clientName != null
													&& threads[i].clientName.equals(words[2])) {
												threads[i].os.writeObject("File In Transit....");
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

 */

					}



					//Manage message transfers

					//Private messaging:
					else if (line.startsWith("pvt_msg"))
					{
						String[] words = line.split("\\s", 3);
						if (words.length > 1 && words[2] != null)
						{
							words[2] = words[2].trim();
							System.out.println("Message sent by " + name + " to " + words[1].substring(1) );
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
											this.os.writeObject("Private message : " + words[1].substring(1));
											break;
										}
									}
								}
							}
						}
					}

					//Public messaging:
					else if (line.startsWith("pub_msg"))
					{
						String[] words = line.split("\\s", 2);
						if (words.length > 1 && words[1] != null)
						{
							words[1] = words[1].trim();
							System.out.println("Message sent to all by " + name);
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

					else if (line.startsWith("secure")){
						String[] words = line.split("\\s");
						if (words.length > 1 && words[2] != null) {
							//getmessage function converts array to string
							String msg = getMessage(words,3);
							String encrypt = encryptionHelper.encrypt(words[1].trim(),msg);
							System.out.println("Encrypted string : " + encrypt);
							System.out.println("Secured message sent by " + name + " to " + words[2].substring(1));
							if (!words[2].isEmpty()) {
								synchronized (this)
								{
									for (int i = 0; i < maxClientsCount; i++)
									{
										if (threads[i] != null && threads[i] != this
												&& threads[i].clientName != null
												&& threads[i].clientName.equals(words[2]))
										{
											threads[i].formerEncryptedString = encrypt;
											System.out.println("encrypted code :" + encrypt);
											threads[i].os.writeObject("<<" + name + ">> " + encrypt);
											threads[i].os.writeObject("<<system>> To decrypt this message use command:  decrypt <password>");
											this.os.writeObject("Secured message sent to " + words[2].substring(1));
											break;
										}
									}
								}

							}
						}
					}


					else if (line.startsWith("decrypt")) {
						String[] words = line.split("\\s", 2);
						if (words.length > 1 && words[1] != null) {
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i] == this) {
										if (threads[i].formerEncryptedString == "") {
											threads[i].os.writeObject("<<system>> No messages to decrypt!");
										}
										else {
											String decryptedMsg = encryptionHelper.decrypt(words[1].trim(),threads[i].formerEncryptedString);
											threads[i].formerEncryptedString = "";
											threads[i].os.writeObject("<<system>> "+ decryptedMsg);
										}
										break;
									}
								}
							}
						}
					}


					//manage all the 'room' related commands
					else if (line.startsWith("room")){
						//System.out.println("Rooms : " + rooms.keySet());
						String[] words = line.split("\\s");

						// create room :
						if(words[1].equals("create")){
							String room_name = words[2];
							System.out.println("Request to create a room : " + words[2]);
							List<String> client_list = new ArrayList<>();
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null && threads[i] == this ) {
										client_list.add(threads[i].clientName);
										threads[i].rooms.put(room_name,client_list);
										threads[i].os.writeObject("New room created : " + room_name);

									}
								}
							}

						}

						// join room :
						if(words[1].equals("join")) {
							String room_name = words[2];
							//System.out.println("Entering the room :" + words[2]);
							if (rooms.containsKey(words[2])) {
								System.out.println("Request to join a room :" + room_name);
								List<String> client_list = rooms.get(words[2]);
								synchronized (this) {
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
											client_list.add(threads[i].clientName);
											threads[i].rooms.put(room_name, client_list);
											threads[i].os.writeObject("You have joined the room : " + room_name);
										}
									}
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											if(rooms.get(room_name).contains(threads[i].clientName)) {
												threads[i].os.writeObject(clientName + " has entered the room : " + room_name);
											}
										}
									}
								}

							}
						}


						// leave room :
						if(words[1].equals("leave")) {
							String room_name = words[2];
							if (rooms.containsKey(words[2])) {
								System.out.println("Request to leave a room :" + room_name);
								List<String> client_list = rooms.get(words[2]);
								synchronized (this) {
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
											client_list.remove(threads[i].clientName);
											threads[i].rooms.put(room_name, client_list);
											threads[i].os.writeObject("You have left the room : " + room_name);
										}
									}
									for (int i = 0; i < maxClientsCount; i++) {
										if (threads[i] != null && threads[i].clientName != null && threads[i] != this) {
											if(rooms.get(room_name).contains(threads[i].clientName)) {
												threads[i].os.writeObject(clientName + " has left the room : " + room_name);
											}
										}
									}
								}
							}
						}

						else if (words[1].equals("chat"))
						{
							String room_name = words[2];

							if (rooms.containsKey(room_name) && words.length > 1 && words[3] != null)
							{
								//get message from the command
								words[3] = words[3].trim();
								System.out.println("Message sent to '" + room_name + "' room by " + name);
								if (!words[3].isEmpty())
								{
									synchronized (this)
									{
										for (int i = 0; i < maxClientsCount; i++)
										{
											if (threads[i] != null && threads[i].clientName != null) {
												if(rooms.get(room_name).contains(threads[i].clientName)) {
													threads[i].os.writeObject("message for room '" + room_name + "' : " + words[3]);
												}
											}
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
						// list room names
						if(words[1].equals("names")){
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
										threads[i].os.writeObject("Available rooms : " + rooms.keySet());
									}
								}
							}
						}

					}

					else if(line.startsWith("help")){
						synchronized (this) {
							for (int i = 0; i < maxClientsCount; i++) {
								if (threads[i] != null && threads[i].clientName != null && threads[i] == this) {
											threads[i].os.writeObject("HELP: \n" +
												"To create room:                 room create <room_name> \n\n" +
												"To join room:                   room join <room_name> \n\n" +
												"To leave room:                  room leave <room_name> \n\n" +
												"To enlist all room members:     room members <room_name> \n\n" +
												"To enlist all available users : enlist_users \n\n" +
												"To enlist all available rooms : room names \n\n" +
												"To send message to a room:      room chat <type message> \n\n" +
												"To send a private message:      pvt_msg @clientname <type message> \n\n" +
												"To send a public message :      pub_msg @clientname <type message> \n\n" +
												"To send a secure message :      secure <password> @clientname <typemessage> \n\n" +
												"To decrypt the secure message:  dycrypt <password> \n\n");
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
										threads[i].os.writeObject("Available users : " + threads[j].clientName);
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