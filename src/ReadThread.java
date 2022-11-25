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
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ReadThread implements  Runnable {
    
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
	// TODO Auto-generated constructor stub
}




	public void run() {
    	String inputResponse;
		
		try {
			while ((inputResponse = (String)is.readObject()) != null) {

				if(inputResponse.equals("FILE INCOMING")){
					String cname = (String)is.readObject();
					String filename = (String)is.readObject();

					int test = is.read();
					while(test!=-1)
					{

						int size = 8096;
						int byteread;

						byte[] buffer = new byte[size];
						File f = new File(cname + "/" +filename);
						if(!f.getParentFile().exists()){
							f.getParentFile().mkdirs();
						}

						try {
							f.createNewFile();
						} catch (Exception e) {
							e.printStackTrace();
						}

						File file = new File(f.getParentFile(), f.getName());
						System.out.println("The file " + filename + " has been received");
						try {
							FileOutputStream fos = new FileOutputStream(file);
							BufferedOutputStream out = new BufferedOutputStream(fos);
							while ((byteread = is.read(buffer, 0, buffer.length)) != -1) {
								out.write(buffer, 0, byteread);
								out.flush();
							}

							fos.close();
							out.close();

							break;
						} catch(Exception e) {
							System.out.println("Exception: " +e);
						}


					}
				}

				else {
					System.out.println(inputResponse);
					if (inputResponse.indexOf("~ Bye") != -1)
						break;
				}
			}
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}