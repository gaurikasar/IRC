Project: Internet Relay Chat
Group Members: Shruti Deshmukh, Gauri Kanchar 

# Introduction:
This is the final project for "Internetworking Protocol" class for fall 2022. 
This project covers functionalities related to the communication between client and server. 

## Usage of functionality:

###Initiate server and clients
The server is initiated at port 8989 by default.

You can run the server using ide or command line.
To start server using command line type command:

_java Server \<portnumber>_

To start client using command line type command:
  
_java Client.
  
### Exhange messages:

1. To send private message
_pvt_msg @clientname \<type message>

2. To send a message to all users at once
_pub_msg \<type message>

3. To send a sercured message to user
_secure <key> @clientname \<type message>

4. To decrypt the key
_decrypt <key>
 
### Room related commands

1. To create a room
_room create <room name>

2. To join a room
_room join <room name>

3. To leave a room
_room leave <room name>

4. To enlist all members of a room
_room members <room name>

5. To enlist all available users
_enlist_users

6. To enlist all the rooms available
_room names


