import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;

public class ChatServer 
{
	//Creates a Server Socket
	private static ServerSocket serverSocket = null;
	//Creates a Client Socket
	private static Socket clientSocket = null;
	//The server can host up to 10 connections at once.
	private static final int maxClientCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientCount];
	
	public static void main(String args[])
	{
		//Sets the default port number at 14001
		int portNumber = 14001;
		
		//If args is greater than 0 than something other than 'java ChatServer' was used to run the program.
		if(args.length > 0)
		{
			//If args equals 2 than there are 2 statements after 'java ChatClient' that have been used to run the program.
			if(args.length == 2)
			{
				if(args[0].equals("-csp"))
				{
					//If the first args is '-ccp' then it takes the args after it and sets it as the new port number.
					portNumber = Integer.parseInt(args[1]);
					//Prints a statement to notify the user of the port number change.
					System.out.println("Port has been set to: " + portNumber);
				}
				
			}
			else
			{
				//If these args do not correspond to -csp then the program will assume that the inputs were invalid and then close.
				System.err.println("Your Port number was invalid.");
				System.exit(0);
			}
		}
		else
		{
			//Notifies the user of the default port number.
			System.out.println("Port has been set to: " + portNumber);
		}
		
		//Opens a server on the given port number.
		try
		{
			serverSocket = new ServerSocket(portNumber);
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		
		while(true)
		{
			//Creates a client socket for each client that connects and stores it in a new thread corresponding to that client
			try
			{
				clientSocket = serverSocket.accept();
				int i = 0;
				
				for(i = 0; i < maxClientCount; i++)
				{
					if(threads[i] == null)
					{
						(threads[i] = new clientThread(serverSocket, clientSocket, threads)).start();
						break;
					}
				}
				
				if(i == maxClientCount)
				{
					PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());
					//If the number of clients exceeds 10 then the client is told that the Server is busy and their socket is closed.
					outputStream.println("Server is busy");
					outputStream.close();
					clientSocket.close();
				}
			}
			catch(IOException e)
			{
				//Notifies the user of an IOException.
				System.err.println("IOException e" );
			}
		}

	}
}

class clientThread extends Thread
{
	
	private String clientName = null;
	private BufferedReader inputStream = null;
	private PrintStream outputStream = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientCount;
	private ServerSocket serverSocket = null;

	public clientThread(ServerSocket serverSocket, Socket clientSocket, clientThread[] threads)
	{
		this.serverSocket = serverSocket;
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientCount = threads.length;
	}
	
	public void run()
	{
		int maxClientCount = this.maxClientCount;
		clientThread[] threads = this.threads;
		
		try
		{
			//Creates both input and output streams for the connected client.
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputStream = new PrintStream(clientSocket.getOutputStream());
			String name;
			
			//Asks the client for a name and stores it in a variable 'name'.
			outputStream.println("Enter your name.");
			name = inputStream.readLine().trim();
			outputStream.println("Welcome " + name + " to our chatroom.\nTo leave enter /disconnect in a new line.");
			
			synchronized(this)
			{
				for(int i = 0; i < maxClientCount; i++)
				{
					if(threads[i] != null && threads[i] == this)
					{
						//Assigns the client another name to be used with threads.
						clientName = "@" + name;
						break;
					}
				}
				
				for(int i = 0; i < maxClientCount; i++)
				{
					if(threads[i] != null && threads[i] == this)
					{
						//Notifies all clients of the new client that has joined the server.
						threads[i].outputStream.println( name + " has joined.");
					}
				}
			}
			
			while(true)
			{
				String line = inputStream.readLine();
				
				//If the client types '/disconnect' the while loop breaks.
				if(line.startsWith("/disconnect"))
				{
					break;
				}
				//If the client types 'EXIT' the client and server sockets are closed as well as the input and output streams, effectively shutting down the server.
				if(line.startsWith("EXIT"))
				{
					inputStream.close();
					outputStream.close();
					clientSocket.close();
					serverSocket.close();
					System.exit(0);
				}
				else
				{
					synchronized(this)
					{
						for(int i = 0; i < maxClientCount; i++)
						{
							if(threads[i] != null && threads[i].clientName != null)
							{
								//Broadcasts the messages from each client to all other clients aswell as displaying their name before the message.
								threads[i].outputStream.println("<" + name + "> " + line);
							}
						}
					}
				}
			}
			//After '/disconnect is typed the while loop breaks and executes the rest of the code.
			synchronized(this)
			{
				for(int i = 0; i < maxClientCount; i++)
				{
					if(threads[i] != null && threads[i] != this && threads[i].clientName != null)
					{
						//Due to disconnecting the server notifies all other clients that that specific client has disconnected.
						threads[i].outputStream.println( name + " has left.");
					}
				}
			}
			
			//Prints a Bye message for the client that disconnects.
			outputStream.println("Bye" + name);
			
			synchronized(this)
			{
				for(int i = 0; i < maxClientCount; i++)
				{
					//Sets the value of the thread of the disconnected client to null such that a new client can join the server.
					if(threads[i] == this)
					{
						threads[i] = null;
					}
				}
			}
			
			//Closes the client socket as well as the input and output streams.
			inputStream.close();
			outputStream.close();
			clientSocket.close();
			
			
		}
		
		catch(IOException e)
		{
			//Notifies the user of an IOException.
			System.err.println("IOException e" );
		}
	}

	
}
