import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;


public class Dashboard extends JFrame
{
	private JTable table					= null;
	private JScrollPane scroll				= null;
	private JComboBox<String> cbxId			= null;
	private JComboBox<String> cbxStatus 	= null;
	private JButton update					= null;
	private JButton btnSend					= null;
	private JButton btnList				= null;
	
	private Object[][] data					= null;
	private String[] columns 				= {"ID","Quantity","Details","Status","Last seen"};
	
	private final int SCR_W					= 640;
	private final int SCR_H					= 580;
	private final int LOWER_CONTAINER_H		= 200;
	private final int COLS					= 5;
	private String destIp					= "";
	private boolean changed					= true;
	//private boolean updated				= false;
	private final static String SRV_IP		= "104.236.46.5";//"192.168.43.93";"localhost";
	
	private ArrayList<Item> items			= new ArrayList<Item>();
	//private Socket client					= null;
	//private ServerSocket server				= null;
	private DatagramSocket server			= null;
	
	public Dashboard(String destIp)
	{
		super("The Mediator's Dashboard");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(new Dimension(SCR_W,SCR_H));
		this.setLocationRelativeTo(null);
		
		this.destIp = destIp;		
		//Init server
		try 
		{
			//server = new ServerSocket(4244);
			server = new DatagramSocket(4244);
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
		
		//Try to load items from disk
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("dashData.dat")));
			items = (ArrayList<Item>)ois.readObject();
			ois.close();
			System.out.println("Loaded list from disc: " + items.size());
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved data.dat - must be a first time user: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		//
		
		initUI();
		updateList();
		Timer tRefreshTable = new Timer(1000, new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				populateTable();
			}
		});
		tRefreshTable.start();
		
		Thread tListen = new Thread(new Runnable() 
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
					System.err.println("Could not start listener agent: " + e.getMessage());
				}
			}
		});
		tListen.start();
		
		initHandlers();
	}
	
	public static void main(String[] args) 
	{
		new Dashboard(SRV_IP).setVisible(true);;
	}
	
	private void initUI()
	{
		this.setLayout(new BorderLayout());
		//
		data = new Object[0][COLS];
		table = new JTable(data,columns);
		//table.setPreferredSize(new Dimension(400,10));
		
		scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(SCR_W,SCR_H-LOWER_CONTAINER_H));
		
		JPanel upperContainer = new JPanel();
		upperContainer.add(scroll);
		upperContainer.setPreferredSize(new Dimension(SCR_W,SCR_H-LOWER_CONTAINER_H));
		upperContainer.setBackground(Color.DARK_GRAY);
		
		JPanel lowerContainer = new JPanel();
		lowerContainer.setPreferredSize(new Dimension(SCR_W,LOWER_CONTAINER_H));
		lowerContainer.setBackground(Color.GRAY);
		//
		lowerContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		//gbc.gridwidth = 3;
		//gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.BOTH;
		//gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		cbxId = new JComboBox<String>();
		//cbxId.setPreferredSize(new Dimension(200,40));
		cbxId.addItem("Choose target ID");
		cbxId.addItem("ALL");
		lowerContainer.add(cbxId,gbc);
		//gbc.gridwidth = 0;
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		cbxStatus = new JComboBox<String>();
		cbxStatus.addItem("Choose status for target");
		cbxStatus.addItem("READY");
		cbxStatus.addItem("PENDING");
		cbxStatus.addItem("CANCEL");
		cbxStatus.addItem("MSG");
		cbxStatus.addItem("NOTIF");
		cbxStatus.addItem("PRICE");
		lowerContainer.add(cbxStatus,gbc);
		
		
		
		/*gbc.gridx = 0;
		gbc.gridy = 1;
		lowerContainer.add(new JLabel("Message: "),gbc);*/
		
		/*gbc.gridx = 2;
		gbc.gridy = 1;
		btnSend				= new JButton("Send Message");
		btnSend.setPreferredSize(new Dimension(150,20));
		lowerContainer.add(btnSend,gbc);*/
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		btnList				= new JButton("Update List");
		btnList.setPreferredSize(new Dimension(150,20));
		lowerContainer.add(btnList,gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		update				= new JButton("Submit Form");
		update.setPreferredSize(new Dimension(150,50));
		lowerContainer.add(update,gbc);
		//cbxId.addItem();
		//
		
		this.add(upperContainer,BorderLayout.NORTH);
		this.add(lowerContainer,BorderLayout.CENTER);
	}
	
	private void initHandlers()
	{
		update.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String status =  cbxStatus.getSelectedItem().toString();
				String id = cbxId.getSelectedItem().toString();
				String msg;
				if(!id.equals("Choose target ID") && !status.equals("Choose status for target"))
				{
					try 
					{
						switch(status)
						{
							case "READY":
								sendMessage("READY\t" + id, SRV_IP);
								break;
							case "PENDING":
								sendMessage("PENDING\t" + id, SRV_IP);
								break;
							case "CANCEL":
								sendMessage("CANCEL\t" + id, SRV_IP);
								break;
							case "MSG":
								msg = JOptionPane.showInputDialog("Enter your message:");
								if(!msg.isEmpty())
								{
									String p = JOptionPane.showInputDialog("Enter your priority [Number only]\n[0=Normal,1=Medium,2=High,3=Extreme]:");
									int priority = Message.NORMAL;
									try
									{
										priority = Integer.valueOf(p);
										sendMessage("MSG\t" + msg + "\t" +id + "\t" + priority, SRV_IP);
									}
									catch(NumberFormatException ex)
									{
										JOptionPane.showMessageDialog(null, "Input is not a number: " + ex.getMessage(),"Message not sent",JOptionPane.ERROR_MESSAGE);
									}
								}
								else
									JOptionPane.showMessageDialog(null, "Empty messages are not allowed!","Message not sent",JOptionPane.ERROR_MESSAGE);
								
								break;
							case "NOTIF":
								msg = JOptionPane.showInputDialog("Enter your message:");
								if(!msg.isEmpty())
								{
									String p = JOptionPane.showInputDialog("Enter your priority [Number only]\n[0=Normal,1=Medium,2=High,3=Extreme]:");
									int priority = Message.NORMAL;
									try
									{
										priority = Integer.valueOf(p);
										sendMessage("NOTIF\t" + msg + "\t" +id, SRV_IP);
									}
									catch(NumberFormatException ex)
									{
										JOptionPane.showMessageDialog(null, "Input is not a number: " + ex.getMessage(),"Message not sent",JOptionPane.ERROR_MESSAGE);
									}
								}
								else
									JOptionPane.showMessageDialog(null, "Empty messages are not allowed!","Message not sent",JOptionPane.ERROR_MESSAGE);
								break;
							case "PRICE":
								msg = JOptionPane.showInputDialog("Enter price:");
								try
								{
									if(!msg.isEmpty())
									{
										int prc = Integer.valueOf(msg);
										sendMessage("PRICE\t" + prc, SRV_IP);
									}
									else
										throw new NumberFormatException();
								}
								catch(NumberFormatException ex)
								{
									JOptionPane.showMessageDialog(null, "Empty messages are not allowed!: " + ex.getMessage(),"Price not sent",JOptionPane.ERROR_MESSAGE);
								}
								break;
						}
					}
					catch (IOException e1) 
					{
						System.err.println("Could not send form submission: " + e1.getMessage());
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Either your message is empty[if MSG option chosen] or drop down box choices are invalid.","Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		/*btnSend.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				//if(txt)
				{
					
				}
			}
		});*/
		
		btnList.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				updateList();
			}
		});		
	}
	
	private void updateList()
	{
		if(items != null)
		{
			//verify its data
			//Create comparision string
			String comp = "";
			for(Item i:items)
				comp += i.getRecStat()+"";
			
			//Send to server
			try
			{
				sendMessage("COMP\t" + comp, SRV_IP);
				//updated = true;
			} 
			catch (IOException e) 
			{
				System.err.println("Could not send data: " + e.getMessage());
				//updated = false;
			}
		}
	}
	
	private class AsciiMap
    {
        private char letter;
        private String ascii;
        public AsciiMap(char letter,String ascii){this.letter=letter; this.ascii=ascii;}
        public String[] getMapping(){return new String[]{String.valueOf(letter),String.valueOf(ascii)};}
        public char getChar(){return letter;}
        public String getAscii(){return ascii;}
    }
	
	private AsciiMap findAsciiMapping(String ascii,AsciiMap[] asciis)
    {
        //for(int i=0;i<msg.length();i+=3)
		for(AsciiMap a:asciis)
        {
            if(a.getAscii().equals(ascii))
                return a;
        }
        return null;//not found
    }
	
	private void populateTable()
	{
		//if(!updated)
		//	updateList();
		
		if(changed)//Only update when there's been a change - save resources.
		{
			data = new Object[0][COLS];
			//echo("Refreshing list..",false);
			cbxId.removeAllItems();
			cbxId.addItem("Choose target ID");
			cbxId.addItem("ALL");
			for(Item i:items)
			{
				/*long last = i.getLastSeen();
				long curr = new Date().getTime();
				long diff = curr-last;*/
				double hours = (float)i.getLastSeen()/1000;///*s*//60/*m*/;//_/60/*h*/;
				DecimalFormat df = new DecimalFormat("##.000");
				
				addData(i.getID(),i.getQuantity(),decrypt(i.getDetails()),i.getRecStat(),"Last seen " + df.format(hours/60) + " minutes ago");
				cbxId.addItem(i.getID());
			}
			
			DefaultTableModel model = new DefaultTableModel(data,columns);
			table.setModel(model);
			changed = false;
		}
	}
	
	public String decrypt(String msg)
	{
		AsciiMap[] chars = {
                new AsciiMap(' ',"032"),new AsciiMap('!',"033"),
                new AsciiMap('\"', "034"),new AsciiMap('#',"035"),new AsciiMap('$',"036"),
                new AsciiMap('%', "037"),new AsciiMap('&',"038"),new AsciiMap('\'',"039"),
                new AsciiMap('(', "040"),new AsciiMap(')',"041"),new AsciiMap('*',"042"),
                new AsciiMap('+', "043"),new AsciiMap(',',"044"),new AsciiMap('-',"045"),
                new AsciiMap('.', "046"),new AsciiMap('/',"047"),
                new AsciiMap('0',"048"),new AsciiMap('1',"049"),new AsciiMap('2',"050"),new AsciiMap('3',"051"),
                new AsciiMap('4',"052"),new AsciiMap('5',"053"),new AsciiMap('6',"054"),new AsciiMap('7',"055"),
                new AsciiMap('8',"056"),new AsciiMap('9',"057"),
                new AsciiMap(':',"058"),new AsciiMap(';',"059"),new AsciiMap('<',"060"),
                new AsciiMap('=',"061"),new AsciiMap('>',"062"),new AsciiMap('?',"063"),
                new AsciiMap('@',"064"),
                new AsciiMap('A',"065"),new AsciiMap('B',"066"),new AsciiMap('C',"067"),new AsciiMap('D',"068"),
                new AsciiMap('E',"069"),new AsciiMap('F',"070"),new AsciiMap('G',"071"),new AsciiMap('H',"072"),
                new AsciiMap('I',"073"),new AsciiMap('J',"074"),new AsciiMap('K',"075"),new AsciiMap('L',"076"),
                new AsciiMap('M',"077"),new AsciiMap('N',"078"),new AsciiMap('O',"079"),new AsciiMap('P',"080"),
                new AsciiMap('Q',"081"),new AsciiMap('R',"082"),new AsciiMap('S',"083"),new AsciiMap('T',"084"),
                new AsciiMap('U',"085"),new AsciiMap('V',"086"),new AsciiMap('W',"087"),new AsciiMap('X',"088"),
                new AsciiMap('Y',"089"),new AsciiMap('Z',"090"),
                new AsciiMap('[',"091"),new AsciiMap('\\',"092"),
                new AsciiMap(']',"093"),new AsciiMap('^',"094"),new AsciiMap('_',"095"),
                new AsciiMap('`',"096"),
                new AsciiMap('a',"097"),new AsciiMap('b',"098"),new AsciiMap('c',"099"),new AsciiMap('d',"100"),
                new AsciiMap('e',"101"),new AsciiMap('f',"102"),new AsciiMap('g',"103"),new AsciiMap('h',"104"),
                new AsciiMap('i',"105"),new AsciiMap('j',"106"),new AsciiMap('k',"107"),new AsciiMap('l',"108"),
                new AsciiMap('m',"109"),new AsciiMap('n',"110"),new AsciiMap('o',"111"),new AsciiMap('p',"112"),
                new AsciiMap('q',"113"),new AsciiMap('r',"114"),new AsciiMap('s',"115"),new AsciiMap('t',"116"),
                new AsciiMap('u',"117"),new AsciiMap('v',"118"),new AsciiMap('w',"119"),new AsciiMap('x',"120"),
                new AsciiMap('y',"121"),new AsciiMap('z',"122"),
                new AsciiMap('{', "123"),new AsciiMap('|',"124"),new AsciiMap('}',"125"),
                new AsciiMap('~', "126")
                };
		 
		 	String temp = "";
		 	Pattern p = Pattern.compile("(\\[\\d{1}\\d{1}\\d{1}\\])");
		 	Matcher m = p.matcher(msg);
		 	System.out.println("Matching against: " + msg);
		 	while(m.find())
		 	{
		 		//System.out.println("Checking: " + msg.substring(m.start(),m.end()));
		 		AsciiMap a = findAsciiMapping(msg.substring(m.start()+1,m.end()-1),chars);
		 		if(a!=null)
		 		{
		 			msg = m.replaceFirst(String.valueOf("\\" +a.getChar()));
		 			m = p.matcher(msg);
		 		}
		 		else
		 			System.err.println("No mapping found for: " + m.group(0));
		 			//JOptionPane.showMessageDialog(null,"No mapping found for: " + m.group(0));// msg.substring(m.start(),3)
		 	}
	        return msg;
	}
	
	public void startListener() throws IOException
	{
		byte[] buffer = new byte[4096];
		DatagramPacket inbound = new DatagramPacket(buffer,buffer.length);
		while(true)
		{
			server.receive(inbound);
			/*Socket inbound = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(inbound.getInputStream()));
			String response = in.readLine();//new String(inbound.getData(),0,inbound.getLength());*/
			String response = new String(inbound.getData(), 0, inbound.getLength());
			StringTokenizer tokenizer = new StringTokenizer(response,"\t");
			String cmd = tokenizer.nextToken();
			switch(cmd)
			{
				case "COMPRD":
					String result = tokenizer.nextToken();
					if(result.equals("TRUE"))
					{
						System.out.println("Table up to date.");
					}
					else if(result.equals("FALSE"))
					{
						items = new ArrayList<Item>();
						System.out.println("Local table not up to date - requesting for update.");
						sendMessage("UPDATE\t", SRV_IP);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Unknown result from server","Error!",JOptionPane.ERROR_MESSAGE);
					}
					break;
				case "ERR":
					String msg = tokenizer.nextToken();
					JOptionPane.showMessageDialog(null, msg,"Error!",JOptionPane.ERROR_MESSAGE);
					break;
				case "DATA":
					String id = tokenizer.nextToken();
					String qty = tokenizer.nextToken();
					String details = tokenizer.nextToken();
					String stat = tokenizer.nextToken();
					String ls = tokenizer.nextToken();
					Item item = new Item(Integer.valueOf(qty), id, details, Long.valueOf(ls));
					item.setRecStat(Integer.valueOf(stat));
					items.add(item);
					System.out.println("Received data");
					changed = true;
					break;
				case "UPDATED":
					//Write to disk
					System.out.println("Table up to date.");
					WritersAndReaders.saveItems(items, "dashData.dat");
					break;
				case "PENDING":
					
					break;
				default:
					break;
			}
		}
	}
	
	public void sendMessage(String msg,String destIP) throws IOException
	{
		try 
		{
			DatagramPacket outbound = new DatagramPacket(msg.getBytes(), msg.getBytes().length,InetAddress.getByName(destIP),4242);
			server.send(outbound);
			System.out.println("Sent to server");
			/*client = new Socket(InetAddress.getByName(destIp),4242);//connect to server
			PrintWriter out = new PrintWriter(client.getOutputStream());
			out.println(msg);
			out.flush();
			out.close();
			client.close();*/
		}
		catch (UnknownHostException e) 
		{
			JOptionPane.showMessageDialog(null, "Could not send request:" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	public void addData(String id, int qty, String details, int stat,String seen)
	{
		Object[][] temp = new Object[data.length+1][COLS];
		//Make backup
		for(int row=0;row<data.length;row++)
		{
			for(int col=0;col<COLS;col++)
			{
				temp[row][col] = data[row][col];
			}
		}
		temp[data.length][0] = id;
		temp[data.length][1] = qty;
		temp[data.length][2] = details;
		temp[data.length][3] = stat;
		temp[data.length][4] = seen;
		//Copy back to actual array
		data = new Object[temp.length][COLS];
		for(int row=0;row<temp.length;row++)
		{
			for(int col=0;col<COLS;col++)
			{
				data[row][col] = temp[row][col];
			}
		}
	}
	
}
