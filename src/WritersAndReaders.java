import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;


public class WritersAndReaders 
{
	
	//Clients will use this - only have one Item instance to keep track of - theirs
	public static void saveItem(Item item,String filename)
	{
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
			oos.writeObject(item);
			oos.flush();
			oos.close();
			System.out.println("Saved item to disk: " + filename);
		} 
		catch (IOException e) 
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	public static void writeToLog(String filename,String log)
	{
		//File f = new File(filename);
		try 
		{
			String str = log + " at " + new Date() + "\n";
			Files.write(Paths.get("log.log"),str.getBytes(),StandardOpenOption.APPEND);
			/*PrintWriter out = new PrintWriter(f);
			out.append(log + " at " + new Date());
			out.flush();
			out.close();*/
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("FNE: " + e.getMessage());
		} 
		catch (IOException e)
		{
			System.err.println("IOE: " + e.getMessage());
		}
		
	}
	
	//Server will use this - has many Item instances to keep track of
	public static void saveItems(ArrayList<Item> items,String filename)
	{
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
			oos.writeObject(items);
			oos.flush();
			oos.close();
			System.out.println("Saved items to disk: " + filename);
		} 
		catch (IOException e) 
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	//Both server & clients
	public static void saveMessages(ArrayList<Message> messages,String filename)
	{
		//Write to disk
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
			oos.writeObject(messages);
			oos.flush();
			oos.close();
			System.out.println("Saved messages to disk: " + filename);
		} 
		catch (IOException e) 
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	//Clients will use this - only have one Item instance to keep track of - theirs
	@SuppressWarnings("unchecked")
	public static Item loadItem(String filename)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filename)));
			Item item = (Item)ois.readObject();
			ois.close();
			System.out.println("Loaded item from disk: " + item);
			return item;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved " + filename + " - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}
	
	//Server will use this - has many Item instances to keep track of
	@SuppressWarnings("unchecked")
	public static ArrayList<Item> loadItems(String filename)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filename)));
			ArrayList<Item> items = (ArrayList<Item>)ois.readObject();
			ois.close();
			System.out.println("Loaded items from disk: " + items.size());
			return items;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved " + filename + " - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}
	
	//Both server & clients
	@SuppressWarnings("unchecked")
	public static ArrayList<Message> loadMessages(String filename)
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filename)));
			ArrayList<Message> messages = (ArrayList<Message>)ois.readObject();
			ois.close();
			System.out.println("Loaded messages from disk: " + messages.size());
			return messages;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved "+filename+" - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}
}
