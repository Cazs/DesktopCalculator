import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;


public class BoSAgent 
{
	//private Socket client						= null;
	//private ServerSocket server					= null;
	private DatagramSocket server				= null;
	private Item item							= null;
	private int price							= 0;
	private String[] funcs						= {"","sin","cos","tan","ln","e","log","sec","cosec","cot"};
	private String destIP						= "";
	private boolean connected					= true;
	private ArrayList<Message> messages			= null;
	
	public BoSAgent(Item item, String destIP, int price)
	{
		this.item = item;
		this.price = price;
		this.destIP = destIP;
		try 
		{
			server = new DatagramSocket(4243);
			//server = new ServerSocket(4243);	
			//System.out.println("Local server started.");
		} 
		catch (SocketException e) 
		{
			JOptionPane.showMessageDialog(null, "Couldn't start client socket [make sure you allowed this program through your firewall]: " + e.getMessage());
			System.exit(-1);
		}
		/*catch (UnknownHostException e) 
		{
			JOptionPane.showMessageDialog(null, "Couldn't start client socket [make sure you allowed this program through your firewall]: " + e.getMessage());
			System.exit(-1);
		}*/
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Couldn't start client socket [make sure you allowed this program through your firewall]: " + e.getMessage());
			System.exit(-1);
		}
		//Attempt to read messages from disc
		messages = WritersAndReaders.loadMessages("msgData.dat");
		//Instantiate messages ArrayList for fallback
		if(messages==null)
				messages = new ArrayList<Message>();
	}
	
	//Sends the item
	public void transfer() throws IOException
	{
		if(item != null)
		{
			
			//System.out.println("Item stat: " + item.getRecStat());
			//if it has not been sent or has been sent but not delivered.
			if(item.getRecStat()== -1 || item.getRecStat()== 0)
			{
				//System.out.println("Sending");
				int r = new Random().nextInt((9 - 1) + 1)+1;//1-9 
				String eq = String.valueOf(item.getQuantity()* r * 2);// + "" + r;
				sendMessage("COMPUTE\t" + funcs[r] + "(" + eq + ")" + "\t" + item.getID() + "\t" + item.getDetails());
			}
			else
			{
				System.out.println("Item is in good standing..");
			}
		}
		else
		{
			System.out.println("Empty stack.");
		}
	}
	
	public DatagramSocket getLocalServer()
	{
		return server;
	}
	
	/*public Socket getConnectionToServer()
	{
		return client;
	}*/
	
	public void sendMessage(String msg) throws IOException
	{
		try 
		{
			DatagramPacket outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,InetAddress.getByName(destIP),4242);
			server.send(outbound);
			/*client = new Socket(InetAddress.getByName(destIp),4242);//connect to server
			//DatagramPacket outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,InetAddress.getByName("104.236.91.104"),4242);
			PrintWriter out = new PrintWriter(client.getOutputStream());
			out.println(msg);
			out.flush();
			out.close();
			client.close();
			connected = true;*/
		}
		catch (UnknownHostException e) 
		{
			connected = false;
			JOptionPane.showMessageDialog(null, "Could not send request:" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public ArrayList<Message> getMessages()
	{
		return messages;
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void startListener() throws IOException
	{
		byte[] buffer = new byte[4096];
		DatagramPacket inbound = new DatagramPacket(buffer,buffer.length);
		System.out.println("Local listener is running.");
		while(true)
		{
			server.receive(inbound);
			/*Socket inbound = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(inbound.getInputStream()));
			String response = in.readLine();//new String(inbound.getData(),0,inbound.getLength());*/
			String response = new String(inbound.getData(),0,inbound.getLength());
			StringTokenizer tokenizer = new StringTokenizer(response,"\t");
			String cmd = tokenizer.nextToken();
			String msg = "",stat = "";
			int priority = Message.NORMAL;
			switch(cmd)
			{
				case "MSG":
					msg = tokenizer.nextToken();
					priority = Integer.valueOf(tokenizer.nextToken());
					//String msgID = tokenizer.nextToken();
					messages.add(new Message(0, msg, "MSG", "ME",priority));
					JOptionPane.showMessageDialog(null, msg,"Message from server",JOptionPane.INFORMATION_MESSAGE);
					WritersAndReaders.saveMessages(messages, "msgData.dat");
					break;
				case "STAT":
					stat = tokenizer.nextToken();
					item.setRecStat(Integer.valueOf(stat));
					WritersAndReaders.saveItem(item, "data.dat");
					//JOptionPane.showMessageDialog(null, "ID Mismatch - delete data.dat and start the program again and make your order again","ID Mismatch!",JOptionPane.INFORMATION_MESSAGE);
					break;
				case "NOTIF":
					msg = tokenizer.nextToken();
					priority = Integer.valueOf(tokenizer.nextToken());
					messages.add(new Message(0, msg, "NOTIF", "ME",priority));
					WritersAndReaders.saveMessages(messages, "msgData.dat");
					break;
				case "PRICE":
					String prc = tokenizer.nextToken();
					int p = Integer.valueOf(prc);
					price = p;
					break;
				default:
					System.err.println("UNKNOW COMMAND.");
					break;
			}
		}
	}
	
	public void setPrice(int price)
	{
		this.price = price;
	}
}
