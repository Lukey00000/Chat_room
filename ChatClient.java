import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient implements Runnable 
{
	//Creates a Socket for the client.
	private static Socket clientSocket = null;
	//A PrintStream is used in order to create an Output Stream.
	private static PrintStream outputStream = null;
	//A BufferedReader is used to create an Input Stream.
	private static BufferedReader inputStream = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	
	
	public static void main(String[] args)
	{
		//Sets the default port number as 14001.
		int portNumber = 14001;
		//Sets localhost as the default IP address.
		String IPaddress = "localhost";
		
		
		//If args is greater than 0 than something other than 'java ChatClient' was used to run the program.
		if(args.length > 0)
		{
			//If args equals 4 than there are 4 statements after 'java ChatClient' that have been used to run the program.
			if(args.length == 4)
			{
				if(args[0].equals("-cca"))
				{
					//If the first args is '-cca' then it takes the args after it and sets it as the new IP address.
					IPaddress = args[1];
					//Prints a statement to notify the user of the IP change.
					System.out.println("IP address has been set to: " + IPaddress);
				}
				else if(args[2].equals("-cca"))
				{
					//If the third args is '-cca' then it takes the args after it and sets it as the new IP address.
					IPaddress = args[3];
					//Prints a statement to notify the user of the IP change.
					System.out.println("IP address has been set to: " + IPaddress);
				}
				else if(args[0].equals("-ccp"))
				{
					//If the first args is '-ccp' then it takes the args after it and sets it as the new port number.
					portNumber = Integer.parseInt(args[1]);
					//Prints a statement to notify the user of the port number change.
					System.out.println("Port has been set to: " + portNumber);
				}
				else if(args[2].equals("-ccp"))
				{
					//If the third args is '-ccp' then it takes the args after it and sets it as the new port number.
					portNumber = Integer.parseInt(args[3]);
					//Prints a statement to notify the user of the port number change.
					System.out.println("Port has been set to: " + portNumber);
				}
			}
			//If args equals 2 than there are 2 statements after 'java ChatClient' that have been used to run the program.
			else if(args.length == 2)
			{
				if(args[0].equals("-cca"))
				{
					//If the first args is '-cca' then it takes the args after it and sets it as the new IP address.
					IPaddress = args[1];
					//Prints a statement to notify the user of the IP change.
					System.out.println("IP address has been set to: " + IPaddress);
				}
				else if(args[0].equals("-ccp"))
				{
					//If the first args is '-ccp' then it takes the args after it and sets it as the new port number.
					portNumber = Integer.parseInt(args[1]);
					//Prints a statement to notify the user of the port number change.
					System.out.println("Port has been set to: " + portNumber);
				}
			}
			else
			{
				//If these args do not correspond to -cca or -ccp then the program will assume that the inputs were invalid and then close.
				System.err.println("Your Port number or IP adresss was invalid.");
				System.exit(0);
			}
		}
		else
		{
			//Notifies the user of the default port number and IP address, those being 14001 and localhost respectively.
			System.out.println("Port has been set to: " + portNumber);
			System.out.println("IP address has been set to: " + IPaddress);
		}
		
		
		try
		{
			//Opens a client socket on the given IP address and port number.
			clientSocket = new Socket(IPaddress, portNumber);
			//Opens the input and output streams.
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputStream = new PrintStream(clientSocket.getOutputStream());
		}
		catch(UnknownHostException e)
		{
			//Notifies the user that the given IP address is invalid.
			System.err.println("Invalid IP address.");
		}
		catch(IOException e)
		{
			//Notifies the user that a I/O connection could not be established to the given IP address.
			System.err.println("Couldn't get I/O for the connection to the IP address " + IPaddress);
		}
		
		if(clientSocket != null && outputStream != null && inputStream != null)
		{
			try
			{
				//Creates a thread that is used to read from the server.
				new Thread(new ChatClient()).start();
				while(!closed)
				{
					//Outputs the data inputed by the client to the server.
					outputStream.println(inputLine.readLine().trim());
				}
				//Closes the client socket as well as the input and output streams.
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			}
			catch(IOException e)
			{
			}
		}
	}
	
	public void run()
	{
		//Creates a thread to read from the server.
		String responseLine;
		try 
		{
			while((responseLine = inputStream.readLine()) != null)
			{
				//When the user uses /disconnect it causes the while loop in ChatServer to break which prints a 'Bye' to the user. 
				System.out.println(responseLine);
				if(responseLine.indexOf("Bye") != -1)
				{
					break;
				}
			}
			//This is then check by the client and if a 'Bye' is present then this while loop breaks and the value of closed becomes true. This causes it to stop reading from the socket. 
			closed = true;
		}
		catch(IOException e)
		{
		}
	}

}
