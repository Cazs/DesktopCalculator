

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
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class Server 
{
	//private ServerSocket server					= null;
	private DatagramSocket server					= null;
	private String[] funcs						= {"","sin","cos","tan","ln","e","log","sec","cosec","cot"};
	private int msgID							= 0;
	private ArrayList<Item> items				= null;
	private ArrayList<Message> messages			= null;
	private int price							= 10;
	
	public Server()
	{
		//Try to load items from disk
		items = WritersAndReaders.loadItems("srvData.dat");
		//Instantaite for fallback
		if(items==null)
			items=new ArrayList<Item>();
		//Try to load messages
		messages = WritersAndReaders.loadMessages("srvMsgData.dat");
		
		//Instantaite for fallback
		if(messages==null)
			messages=new ArrayList<Message>();
		
		//Get last ID if not null && not null 
		if(messages != null)//could take this out.
			if(!messages.isEmpty())
				msgID = messages.get(messages.size()-1).getId()+1;//get last elements ID + 1

		try 
		{
			server = new DatagramSocket(4242);
			startListener();
		} 
		catch (IOException e) 
		{
			System.out.println("IOException: " + e.getMessage());
			WritersAndReaders.writeToLog("log.log","IOException: " + e.getMessage());
		}
		
		/*Thread tListen = new Thread(new Runnable()
		{
			
			@Override
			public void run() 
			{
				try 
				{
					startListener();
				} 
				catch (IOException e) 
				{
					System.out.println("IOException: " + e.getMessage());
					WritersAndReaders.writeToLog("log.log","IOException: " + e.getMessage());
				}
			}
		});
		tListen.start();*/
	}
	
	public void sendMessage(String msg, DatagramPacket inbound/*boolean close*/,int port) throws IOException
	{
		try 
		{
			DatagramPacket outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,InetAddress.getByName(inbound.getAddress().getHostAddress()),inbound.getPort());
			server.send(outbound);
			WritersAndReaders.writeToLog("log.log","Sent datagram: " + msg + ";" + inbound.getAddress().getHostAddress() + ":" +inbound.getPort() );
			/*Socket dest = new Socket(client.getInetAddress(),port);//clients listen on port 4243
			PrintWriter out = new PrintWriter(dest.getOutputStream());
			out.println(msg);
			out.flush();*/
			//if(close)dest.close();
			//if(close)client.close();
		}
		catch (UnknownHostException e) 
		{
			System.out.println("Could not send request:" + e.getMessage());
			WritersAndReaders.writeToLog("log.log","Could not send request:" + e.getMessage());
		}
	}
	
	public void startListener() throws IOException
	{
		byte[] buffer = new byte[4096];
		DatagramPacket inbound = new DatagramPacket(buffer,buffer.length);
		System.out.println("Server is running...");
		WritersAndReaders.writeToLog("log.log","Server is running...");
		int priority = Message.NORMAL;
		while(true)
		{
			server.receive(inbound);
			/*BufferedReader in = new BufferedReader(new InputStreamReader(inbound.getInputStream()));
			String response = in.readLine();//new String(inbound.getData(),0,inbound.getLength());*/
			String response = new String(inbound.getData(),0,inbound.getLength());
			WritersAndReaders.writeToLog("log.log",response);
			StringTokenizer tokenizer = new StringTokenizer(response,"\t");
			String cmd = tokenizer.nextToken();
			String id = "",msg = "";
			switch(cmd)
			{
				case "COMPUTE":
					//Store record
					msg = tokenizer.nextToken();
					id = tokenizer.nextToken();
					String details = tokenizer.nextToken();
					
					String symbol = msg.substring(0, msg.indexOf("("));
					
					int index = Arrays.asList(funcs).indexOf(symbol);
					if(index>0)
					{
						//System.out.println(id);
						int val = Integer.valueOf(msg.substring(msg.indexOf("(")+1, msg.indexOf(")")));
						int result = val/(index)/2;
						boolean found = false;
						for(Item i:items)
						{
							if(i.getID().equals(id))
							{
								found = true;
								i.setQuantity(result);
								i.setRecStat(1);
								i.setDetails(details);
								break;
							}
						}
						if(!found)
						{
							Item i= new Item(result, id, details, new Date().getTime());
							i.setRecStat(1);
							items.add(i);
						}
					}
					System.out.println("Received COMPUTE, responding");
					WritersAndReaders.writeToLog("log.log","Received COMPUTE, responding.");
					sendMessage("STAT\t1\t"+id,inbound,4243);
					//Write to disk
					WritersAndReaders.saveItems(items,"srvData.dat");
					//sendMessage("MSG\tIt's working!",inbound);
					//inbound.close();
					break;
				case "PING":
					id = tokenizer.nextToken();
					Item item = null;
					for(Item i:items)
					{
						if(i.getID().equals(id))
						{
							item = i;
							break;
						}
					}
					System.out.println("Received PING, responding");//  + inbound.getSocketAddress() + " : " + inbound.getInetAddress());
					WritersAndReaders.writeToLog("log.log","Received PING, responding.");
					boolean remd		= false;
					if(item!=null)
					{
						//Update last seen
						item.setLastSeen(new Date().getTime());
						//Send status
						sendMessage("STAT\t"+item.getRecStat(),inbound,4243);
						//Send messages
						Iterator<Message> it = messages.iterator();
						try
						{
							for(Message m:messages)
							{
								it.next();
								if(m.getDestID().equals(item.getID()))
								{
									sendMessage(m.getType() + "\t"+m.getMsg()+"\t"+m.getPriority(),inbound,4243);
									it.remove();//remove it no exception is thrown - above line will throw an exception if anything breaks
									remd = true;
								}
							}
						}
						catch(ConcurrentModificationException e)
						{
							System.err.println(e.getMessage());
						}
					}
					else//Unknown ID
					{
						sendMessage("STAT\t3",inbound,4243);//Cancell order
					}
					//optimize - check to see if there was a change and then save if necessary
					if(remd)
						WritersAndReaders.saveMessages(messages,"srvMsgData.dat");
					else
					{
						System.out.println("Message stack empty.");
						WritersAndReaders.writeToLog("log.log","Message stack empty.");
					}
					//Send price
					sendMessage("PRICE\t"+price,inbound,4243);
					break;
				case "PRICE":
					String prc = tokenizer.nextToken();
					try
					{
						price = Integer.valueOf(prc);
						sendMessage("MSG\tPrice set!",inbound,4244);
					}
					catch(NumberFormatException e)
					{
						System.err.println("Can't convert to number: " + e.getMessage());
						sendMessage("ERR\tPrice not set!",inbound,4244);
					}
					break;
				case "COMP":
					String result;
					if(tokenizer.countTokens()>1)
					{
						String comp = tokenizer.nextToken();
						String comprd = "";
						for(Item i:items)
						{
							comprd += i.getRecStat()+"";
						}
						
						result = comp.equals(comprd)?"TRUE":"FALSE";
					}
					else
					{
						result = "FALSE";
					}
					System.out.println("Received COMP, responding.");
					WritersAndReaders.writeToLog("log.log","Received COMP, responding.");
					//Send to dashboard
					try
					{
						sendMessage("COMPRD\t" + result,inbound,4244);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					break;
				case "UPDATE":
					for(Item i:items)
					{
						long last = i.getLastSeen();
						long curr = new Date().getTime();
						long diff = curr-last;
						
						String data = i.getID()+ "\t" + i.getQuantity() + "\t" + i.getDetails() + "\t" 
								+ i.getRecStat() + "\t" + diff;
						sendMessage("DATA\t"+data,inbound,4244);
					}
					System.out.println("Received UPDATE, responding.");
					WritersAndReaders.writeToLog("log.log","Received UPDATE, responding.");
					sendMessage("UPDATED",inbound,4244);
					break;
				case "READY":
					id = tokenizer.nextToken();
					if(id.equals("ALL"))
					{
						for(Item i:items)
						{
							i.setRecStat(2);//ready
						}
					}
					else
					{
						for(Item i:items)
						{
							if(i.getID().equals(id))
							{
								i.setRecStat(2);//ready
								break;
							}
						}
					}
					break;
				case "PENDING":
					id = tokenizer.nextToken();
					if(id.equals("ALL"))
					{
						for(Item i:items)
						{
							i.setRecStat(1);//pending
						}
					}
					else
					{
						for(Item i:items)
						{
							if(i.getID().equals(id))
							{
								i.setRecStat(1);//pending
								break;
							}
						}
					}
					break;
				case "CANCEL":
					id = tokenizer.nextToken();
					if(id.equals("ALL"))
					{
						for(Item i:items)
						{
							i.setRecStat(3);//cancelled
						}
					}
					else
					{
						for(Item i:items)
						{
							if(i.getID().equals(id))
							{
								i.setRecStat(3);//cancelled
								break;
							}
						}
					}
					break;
				case "MSG":
					msg = tokenizer.nextToken();
					id = tokenizer.nextToken();
					priority = Integer.valueOf(tokenizer.nextToken());
					if(id.equals("ALL"))
					{
						for(Item i:items)
						{
							messages.add(new Message(msgID, msg, "MSG", i.getID(),priority));
						}
					}
					else
					{
						messages.add(new Message(msgID, msg, "MSG", id,priority));
					}
					WritersAndReaders.saveMessages(messages,"srvMsgData.dat");
					msgID++;
					break;
				case "NOTIF":
					msg = tokenizer.nextToken();
					id = tokenizer.nextToken();
					priority = Integer.valueOf(tokenizer.nextToken());
					if(id.equals("ALL"))
					{
						for(Item i:items)
						{
							messages.add(new Message(msgID, msg, "NOTIF", i.getID(),priority));
						}
					}
					else
					{
						messages.add(new Message(msgID, msg, "NOTIF", id,priority));
					}
					WritersAndReaders.saveMessages(messages,"srvMsgData.dat");
					msgID++;
					break;
				default:
					System.err.println("UNKNOWN COMMAND.");
					break;
			}
		}
	}
	
	
	public static void main(String[] args) 
	{
		new Server();
	}
}
