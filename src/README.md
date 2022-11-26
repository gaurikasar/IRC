**Project:** Internet Relay Chat<br />
**Group Members:** Shruti Deshmukh, Gauri Kanchar 

# Introduction:
This is the final project for "Internetworking Protocol" class for fall 2022. <br />
This project covers functionalities related to the communication between client and server. 

## Usage of functionality:

### Initiate server and clients
The server is initiated at port 8989 by default.

You can run the server using ide or command line.<br />
To start server using command line type command:<br />
**command:** java Server \<portnumber>

To start client using command line type command:<br />  
**command:** java Client.
  
### Exhange messages:

1. To send private message<br />
   **command:** pvt_msg @clientname \<type message>

2. To send a message to all users at once<br />
   **command:** pub_msg \<type message>

3. To send a sercured message to user<br />
   **command:** secure <key> @clientname \<type message>

4. To decrypt the key<br />
   **command:** decrypt <key>
 
### Room related commands

1. To create a room<br />
   **command:** room create <room name>

2. To join a room<br />
   **command:** room join <room name>

3. To leave a room<br />
   **command:** room leave <room name>

4. To enlist all members of a room<br />
   **command:** room members <room name>

5. To enlist all available users<br />
   **command:** enlist_users

6. To enlist all the rooms available<br />
   **command:** room names


