import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Main extends JFrame 
{
	//Containers
	JPanel iconContainer								= null;
	JPanel screenContainer								= null;
	JPanel upperButtonContainer							= null;
	JPanel lowerButtonContainer							= null;
	JPanel rightButtonContainer							= null;
	JPanel leftButtonContainer							= null;
	JPanel centerButtonContainer						= null;
	
	//Screen stuff
	private JTextArea outputScreen						= null;
	
	//Function buttons
	private JButton shift								= null;
	private JButton back								= null;
	private JButton c									= null;
	private JButton e									= null;
	private JButton divide								= null;
	private JButton multiply							= null;
	private JButton sub									= null;
	private JButton add									= null;
	
	//Number buttons
	private JButton seven								= null;
	private JButton eight								= null;
	private JButton nine								= null;
	private JButton four								= null;
	private JButton five								= null;
	private JButton six									= null;
	private JButton one									= null;
	private JButton two									= null;
	private JButton three								= null;
	private JButton m									= null;
	private JButton zero								= null;
	private JButton equals								= null;
	
	//Directional buttons
	private JButton up									= null;
	private JButton down								= null;
	private JButton left								= null;
	private JButton right								= null;
	private JButton ok									= null;
	
	//Icons
	private Image	upArrow									= null;
	private ImageIcon	upArr								= null;
	private Image	downArrow								= null;
	private ImageIcon	downArr								= null;
	private Image	leftArrow								= null;
	private ImageIcon	leftArr								= null;
	private Image	rightArrow								= null;
	private ImageIcon	rightArr							= null;
	private Image	backspace								= null;
	private ImageIcon	backspc								= null;
	private Image	addImg									= null;
	private ImageIcon	addIco								= null;
	private Image	subImg									= null;
	private ImageIcon subIco								= null;
	private Image	multiplyImg								= null;
	private ImageIcon	multiplyIco							= null;
	private Image	divideImg								= null;
	private ImageIcon	divideIco							= null;
	private Image	powerImg								= null;
	private ImageIcon	powerIco							= null;
	private Image	eImg									= null;
	private ImageIcon	eIco								= null;
	private Image	srootImg								= null;
	private ImageIcon	srootIco							= null;
	private Image	shiftImg								= null;
	private ImageIcon	shiftIco							= null;
	private Image inputImg									= null;
	private ImageIcon	inputIco							= null;
	private Image	cImg									= null;
	private ImageIcon	cIco								= null;
	private Image	equalsImg								= null;
	private ImageIcon	equalsIco							= null;
	private Image	mImg									= null;
	private ImageIcon	mIco								= null;
	private Image	internetImg								= null;
	private ImageIcon	internetIco							= null;
	private Image	pendingImg								= null;
	private ImageIcon	pendingIco							= null;
	private Image	readyImg								= null;
	private ImageIcon	readyIco							= null;
	private Image	shiftingImg								= null;
	private ImageIcon	shiftingIco							= null;
	private Image	exclaimImg								= null;
	private ImageIcon	exclaimIco							= null;
	private Image	cancelledImg							= null;
	private ImageIcon	cancelledIco						= null;
	
	//Icons on top notification bar
	private JLabel lblInput									= new JLabel();
	private JLabel lblPending								= new JLabel();
	private JLabel lblInternet								= new JLabel();
	private JLabel lblShifting								= new JLabel();
	private JTextArea notif									= null;
	private JMenuBar menubar								= null;
	private JMenu file										= null;
	private JMenuItem exit									= null;
	private JMenuItem instructions							= null;
	
	//Variables
	private final int SCR_W									= 480;
	private final int SCR_H									= 640;
	private final int BTN_W									= 50;
	private final int BTN_H									= 30;
	private final int LOWER_BTN_W							= 120;
	private final int LOWER_BTN_H							= 55;
	private final int OFFSET								= 10;
	private final int ICON_PANEL_H							= 60;
	private final int UPPER_CHILD_W							= 150;
	private final int UPPER_SIDE_BTN_W = 70,UPPER_SIDE_BTN_H = 60;
	private final static String SRV_IP		= "localhost";//"104.236.46.5";

	private Font calcFont									= null;
	private String equation									= "";
	//private int caret										= 0;
	private boolean bShift,bM, bE, bPow						= false;
	private boolean bInternet								= false;
	private boolean bOrdering								= false;
	private static Socket conn								= null;
	private boolean menuInitd								= false;
	
	private Timer calcTimer									= null;
	private Item item										= null;
	private BoSAgent agent									= null;
	private int price										= 10;
	private Thread tListen									= null;
	private Timer tSend										= null;
	
	public Main()
	{
		super("The Mediator's Calculator");
		this.setSize(new Dimension(SCR_W,SCR_H));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		
		//Try to make it look pretty
		try 
		{
			UIManager.setLookAndFeel(javax.swing.plaf.nimbus.NimbusLookAndFeel.class.getName());
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) 
		{
			System.err.println(e.getMessage());
		}
		
		try 
		{
			initIcons();
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "Could not load icons :" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			//System.exit(0);
		}
		//Attemp to read in locally saved data
		item = WritersAndReaders.loadItem("data.dat");
		//Instantaite for fallback
		if(item==null)
			item = new Item(0, getID(),"</>",new Date(0).getTime());//get date since the epoch
		
		//Instantiate agent
		agent = new BoSAgent(item,SRV_IP,price);
		
		initControls();
		initUI();
		initHandlers();
		
		//First time internet check
		bInternet = getConnectionStatus();
		
		menubar = new JMenuBar();
		this.setJMenuBar(menubar);
		//exit = new JMenuItem("Exit");
		//file = new JMenu("Options");
		//Start the timer
		calcTimer = new Timer(500, new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(bShift)
					lblShifting.setIcon(shiftingIco);
				else
					lblShifting.setIcon(null);
				
				//Check combination status
				if(bOrdering)
				{
					
					if(!menuInitd)
					{
						//exit = new JMenuItem("Exit");
						//file = new JMenu("Options");
						//instructions = new JMenuItem("Instructions");
						menubar.add(file);
						file.add(instructions);
						file.addSeparator();
						file.add(exit);
						
						bInternet = getConnectionStatus();
						menuInitd = true;
					}
					
					//Check internet status
					try 
					{
						if(bInternet)
							internetImg	= ImageIO.read(new File("./images/internet.png"));
						else
							internetImg = ImageIO.read(new File("./images/no_internet.png"));
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
					internetIco								= new ImageIcon(getScaledImage(internetImg));
					lblInternet.setIcon(internetIco);
					
					if(bInternet)
					{
						lblInput.setIcon(inputIco);//tell user to input order
						notif.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
						notif.setForeground(Color.GREEN);
						notif.setText("Notifications\n---------------------------");
					}
					else
						lblInput.setIcon(null);
					
					//Set status
					switch(item.getRecStat())
					{
						case 0:
							lblPending.setIcon(exclaimIco);
							break;
						case 1:
							lblPending.setIcon(pendingIco);
							break;
						case 2:
							lblPending.setIcon(readyIco);
							break;
						case 3:
							lblPending.setIcon(cancelledIco);
							break;
						default:
							lblPending.setIcon(null);
							break;
					}
				}
				else
				{
					lblInput.setIcon(null);
					lblInternet.setIcon(null);
					lblPending.setIcon(null);
					notif.setText("       Simple Calculator");
					notif.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
					notif.setForeground(Color.CYAN);
				}
			}
		});
		calcTimer.start();
		
		Timer ping = new Timer(10000,new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(bOrdering)
				{
					//Check internet connectivity
					bInternet = getConnectionStatus();
					
					if(agent!=null)
					{
						//Load messages
						ArrayList<Message> messages = null;
						messages = agent.getMessages();
						
						notif.setText("");
						notif.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
						notif.setForeground(Color.GREEN);
						
						if(messages!=null)
						{
							for(Message m:messages)
							{
								notif.append(m.getType() + ">>" + m.getMsg() + "\n");
							}
						}
						
						//Send a Ping
						try 
						{
							if(item!=null)
							{
								agent.sendMessage("PING\t" + item.getID());
								System.out.println("PINGED");
							}
							else
								System.err.println("Item instance is null.");
						} 
						catch (IOException e1)
						{
							System.err.println("Could not ping server: " + e1.getMessage());
						}
						
						//set price
						agent.setPrice(price);
					}
					else
					{
						System.err.println("BoSAgent instance is null");
					}
				}
			}
		});
		ping.start();//check connection status every 10 seconds
		
		//Start listener thread
		if(tListen==null)
		{
			tListen = new Thread(new Runnable() 
			{
				
				@Override
				public void run() 
				{
					try 
					{
						agent.startListener();
					} 
					catch (IOException e) 
					{
						JOptionPane.showMessageDialog(null, e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			tListen.start();
		}
	}
	
	private static boolean getConnectionStatus()
	{
		try 
		{
			//System.out.println("Sending req to google.com");
			conn = new Socket(InetAddress.getByName("google.com"),80);//173.194.45.52
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			String req = 	"GET / HTTP/1.1\r\n";
							//"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64)\r\n" + 
							//" AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
			out.print(req);
			req = "Host: google.com\r\n\r\n";
			out.print(req);
			out.flush();
			//System.out.println("Sent Request");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = in.readLine();
			//System.out.println("Response: "  + line);
			in.close();
			if(line!=null)
			{
				if(line.contains("302 Found"))
					return true;
				else
					return false;
			}
			return false;
		} 
		catch (UnknownHostException e) 
		{
			System.err.println("Cannot connect to the internet[UHE]: "  + e.getMessage());
			return false;
		} 
		catch (IOException e) 
		{
			System.err.println("Cannot connect to the internet[IOE]: "  + e.getMessage());
			return false;
		}
	}
	
	private void initUI()
	{
		//Screen container
		screenContainer					= new JPanel();
		screenContainer.add(iconContainer);
		JScrollPane scroll = new JScrollPane(outputScreen);
		
		screenContainer.add(scroll);
		screenContainer.setBackground(Color.DARK_GRAY);
		iconContainer.setBackground(Color.BLACK);
		
		//button container - middle
		centerButtonContainer = new JPanel();
		//centerButtonContainer.setLayout(new BorderLayout());
		centerButtonContainer.add(up);//,BorderLayout.NORTH
		centerButtonContainer.add(down);//,BorderLayout.SOUTH
		centerButtonContainer.add(left);//,BorderLayout.WEST
		centerButtonContainer.add(right);//,BorderLayout.EAST
		//centerButtonContainer.add(ok,BorderLayout.CENTER);
		
		//Left container - upper
		leftButtonContainer = new JPanel();
		leftButtonContainer.add(shift);
		leftButtonContainer.add(back);
		leftButtonContainer.add(divide);
		leftButtonContainer.add(multiply);
		//Right container - upper
		rightButtonContainer = new JPanel();
		rightButtonContainer.add(c);
		rightButtonContainer.add(e);
		rightButtonContainer.add(sub);
		rightButtonContainer.add(add);
		
		//button container - upper
		upperButtonContainer = new JPanel();
		upperButtonContainer.setBackground(new Color(38, 38, 38));
		upperButtonContainer.setLayout(new BorderLayout());
		upperButtonContainer.add(leftButtonContainer,BorderLayout.WEST);
		upperButtonContainer.add(centerButtonContainer,BorderLayout.CENTER);
		upperButtonContainer.add(rightButtonContainer,BorderLayout.EAST);
		
		//button container - lower
		lowerButtonContainer = new JPanel();
		//lowerButtonContainer.setBackground(new Color(51, 51, 51));
		lowerButtonContainer.add(seven);
		lowerButtonContainer.add(eight);
		lowerButtonContainer.add(nine);
		lowerButtonContainer.add(four);
		lowerButtonContainer.add(five);
		lowerButtonContainer.add(six);
		lowerButtonContainer.add(one);
		lowerButtonContainer.add(two);
		lowerButtonContainer.add(three);
		lowerButtonContainer.add(m);
		lowerButtonContainer.add(zero);
		lowerButtonContainer.add(equals);
		
		//Resize main containers
		screenContainer.setPreferredSize(new Dimension(SCR_W,180));
		upperButtonContainer.setPreferredSize(new Dimension(SCR_W,80));
		lowerButtonContainer.setPreferredSize(new Dimension(SCR_W,250));
		
		//Resize child containers
		rightButtonContainer.setPreferredSize(new Dimension(UPPER_CHILD_W,upperButtonContainer.getHeight()));
		centerButtonContainer.setPreferredSize(new Dimension(upperButtonContainer.getWidth()-UPPER_CHILD_W*2,upperButtonContainer.getHeight()));
		leftButtonContainer.setPreferredSize(new Dimension(UPPER_CHILD_W,upperButtonContainer.getHeight()));
		
		//Resize Screen stuff
		iconContainer.setPreferredSize(new Dimension(SCR_W,ICON_PANEL_H));
		scroll.setPreferredSize(new Dimension(SCR_W,150));
		outputScreen.setLineWrap(true);
		outputScreen.setEditable(false);
		//Set font
		try 
		{
			calcFont = Font.createFont(Font.TRUETYPE_FONT, new File("./fonts/pocket_calc.ttf"));//digital-7
			
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(calcFont);
		    
		    outputScreen.setFont(new Font("Pocket Calculator",Font.PLAIN,40));
		} 
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Could not load font [digital-7]:" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		catch (FontFormatException e)
		{
			JOptionPane.showMessageDialog(null, "Could not load font [digital-7]:" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Could not load font [digital-7]:" + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		
		//Add main containers
		this.add(screenContainer,BorderLayout.NORTH);
		this.add(upperButtonContainer,BorderLayout.CENTER);
		this.add(lowerButtonContainer,BorderLayout.SOUTH);
		
		resizeButtons();
		
		//Add icons
		//lblInternet.setPreferredSize(new Dimension(70,40));
		lblInternet.setToolTipText("States if you have an internet connection or not. Enter your order when your see an icon labelled 'input'");
		lblInput.setToolTipText("Enter your order now");
		lblPending.setToolTipText("States whether your order is ready or pending - when ready, you can expect the order within an hour - an exclaimation mark means your request has been sent but not received by us.");
		lblShifting.setToolTipText("Shift is ON");
		//lblShifting.setIcon(shiftingIco);
		//lblInternet.setIcon(internetIco);
		//lblInput.setIcon(inputIco);
		//lblPending.setIcon(pendingIco);
		
		//Notification text area
		JScrollPane scrollNotif = new JScrollPane(notif);
		scrollNotif.setPreferredSize(new Dimension(SCR_W-200,ICON_PANEL_H));
		notif.setLineWrap(true);
		notif.setBackground(Color.BLACK);
		notif.setForeground(Color.CYAN);
		
		iconContainer.add(scrollNotif);
		iconContainer.add(lblShifting);
		iconContainer.add(lblInternet);
		iconContainer.add(lblInput);
		iconContainer.add(lblPending);
	}
	
	private void initControls()
	{
		outputScreen					= new JTextArea("0");
		iconContainer					= new JPanel();
		
		notif							= new JTextArea("       Simple Calculator");
		notif.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		notif.setEditable(false);
		
		file = new JMenu("Options");
		exit = new JMenuItem("Exit");
		instructions = new JMenuItem("Instructions");
		
		//Function buttons
		shift							= new JButton(shiftIco);
		back							= new JButton(backspc);
		c								= new JButton(cIco);
		e								= new JButton(eIco);
		divide							= new JButton(divideIco);
		multiply						= new JButton(multiplyIco);
		sub								= new JButton(subIco);
		add								= new JButton(addIco);
		
		//Number buttons
		seven							= new JButton("7");
		eight							= new JButton("8");
		nine							= new JButton("9");
		four							= new JButton("4");
		five							= new JButton("5");
		six								= new JButton("6");
		one								= new JButton("1");
		two								= new JButton("2");
		three							= new JButton("3");
		m								= new JButton(mIco);
		zero							= new JButton("0");
		equals							= new JButton(equalsIco);
		
		//Directional buttons
		up								= new JButton();
		up.setIcon(powerIco);
		down							= new JButton();
		down.setIcon(downArr);
		left							= new JButton();
		left.setIcon(leftArr);
		right							= new JButton();
		right.setIcon(rightArr);
		ok								= new JButton("OK");
		//ok.setIcon(ok);
		
		//Other keys
	}
	
	private void resizeButtons()
	{
		//Upper - center
		//left.setPreferredSize(new Dimension(40,20));
		//right.setPreferredSize(new Dimension(40,20));
		
		//Lower
		seven.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		eight.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		nine.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		four.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		five.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		six.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		one.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		two.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		three.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		m.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		zero.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		equals.setPreferredSize(new Dimension(LOWER_BTN_W,LOWER_BTN_H));
		//Upper - left
		shift.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		back.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		divide.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		multiply.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		//Upper - right
		c.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		e.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		sub.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		add.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		//Upper - middle
		up.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		down.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		left.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
		right.setPreferredSize(new Dimension(UPPER_SIDE_BTN_W,UPPER_SIDE_BTN_H));
	}
	
	private Image getScaledImage(Image img)
	{
		return img.getScaledInstance(BTN_W-OFFSET<OFFSET?OFFSET:BTN_W-OFFSET, BTN_H-OFFSET<OFFSET?OFFSET:BTN_H-OFFSET, Image.SCALE_SMOOTH);
	}
	
	private void falsifyCombo()
	{
		if(bM || bE || bPow && !bOrdering)
		{
			bM = false;
			bE = false;
			bPow = false;
		}
	}
	
	private void initHandlers()
	{
		exit.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				System.exit(0);
			}
		});
		
		instructions.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String msg = 
						"1]Type in the amount of items you want using the calculator keypad.\n" + 
						"2]Click the '=' button to send your request\n" +
						"3]Type in your address & your phone number in the popup dialog\n" +
						"4]Wait to be lifted - We'll call you on the entered number when we deliver the item[s].\n\n" +
						"The notification icon panel is a great way to keep track of the status of your order.\n" +
						"Hover over the different icons on the notification panel to see the messages they have for you.\n"+
						"There will be notifications on the notification panel sometimes.\n";
				
				JOptionPane.showMessageDialog(null, msg,"Instructions",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		one.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("1");
				falsifyCombo();
			}
		});
		two.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("2");
				falsifyCombo();
			}
		});
		three.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("3");
				falsifyCombo();
			}
		});
		four.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("4");
				falsifyCombo();
			}
		});
		five.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("5");
				falsifyCombo();
			}
		});
		six.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("6");
				falsifyCombo();
			}
		});
		seven.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("7");
				falsifyCombo();
			}
		});
		eight.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("8");
				falsifyCombo();
			}
		});
		nine.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("9");
				falsifyCombo();
			}
		});
		zero.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("0");
				falsifyCombo();
			}
		});
		add.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("+");
				falsifyCombo();
			}
		});
		sub.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("-");
				falsifyCombo();
			}
		});
		divide.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("/");
				falsifyCombo();
			}
		});
		multiply.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.append("*");
				falsifyCombo();
			}
		});
		
		equals.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				//Calculate/Parse
				equation = outputScreen.getText();
				equation = compute(equation,"*");
				equation = compute(equation,"/");
				equation = compute(equation,"+");
				equation = compute(equation,"-");
				
				outputScreen.setText("");
				outputScreen.setText(equation);
				
				if(bOrdering)
				{
					String amount = outputScreen.getText();
					if(!amount.equals("0") && !amount.isEmpty())
					{
						String details = JOptionPane.showInputDialog("Enter your address followed by your phone number below [& any other comments]:");
						if(!details.isEmpty())
						{
							try
							{
								int amnt = Integer.valueOf(amount);
								//if(item == null)
								//	item = new Item(amnt, getID(),details);
								
								int resp = JOptionPane.showConfirmDialog(null, "Would you like to proceed with your purchase[R"+(amnt*price)+"]?","Proceed?",JOptionPane.YES_NO_OPTION );
								if(resp == JOptionPane.NO_OPTION)
								{
									item.setRecStat(3);//Cancelled
								}
								else if(resp == JOptionPane.YES_OPTION)
								{
									item.setQuantity(amnt);
									item.setRecStat(0);//Sent - but not delivered
									item.setDetails(details);
									
									//Back up item to disc
									WritersAndReaders.saveItem(item, "data.dat");
									
									//Restart services/agents
									//if(tSend!=null)tSend.stop();
									//tSend = null;
									//tListen = null;
									
									/*if(agent!=null)
									{
										Socket conn = agent.getConnectionToServer();
										if(!conn.isClosed())
										{
											conn.close();
										}
										
										ServerSocket srv = agent.getLocalServer();
										//if(!srv.isClosed())
										//	srv.close();
										//agent = null;
									}*/
									
									//Start agent
									if(agent==null)
										agent = new BoSAgent(item, SRV_IP,price);//104.236.87.104
									
									//Start sender timer - check status of item every 5 seconds
									if(tSend==null)
									{
										tSend = new Timer(5000,new ActionListener() 
										{
											
											@Override
											public void actionPerformed(ActionEvent ev) 
											{
												try 
												{
													agent.transfer();
												}
												catch (IOException e) 
												{
													JOptionPane.showMessageDialog(null, e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
													tSend.stop();
													tSend = null;
												}
												/*if(!agent.isConnected())
												{
													tSend.stop();
													tSend = null;
												}*/
											}
										});
										tSend.start();
									}
								}
							}
							catch(NumberFormatException e)
							{
								JOptionPane.showMessageDialog(null, "Invalid number","Error",JOptionPane.ERROR_MESSAGE);
							}
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Empty details!","Error",JOptionPane.ERROR_MESSAGE);
						}
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Invalid amount","Error",JOptionPane.ERROR_MESSAGE);
					}
				}
				else
					falsifyCombo();
			}
		});
		
		c.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				outputScreen.setText("");
				bOrdering = false;
				bShift = false;
				bM = false;
				bE = false;
				bPow = false;
			}
		});
		
		back.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				//Remove a character from the back
				outputScreen.setText(outputScreen.getText().substring(0, outputScreen.getText().length()-1));
				falsifyCombo();
			}
		});
		
		left.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				falsifyCombo();
				//outputScreen.moveCaretPosition(outputScreen.getCaretPosition()-1);
			}
		});
		
		shift.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				bShift = bShift?false:true;
			}
		});
		
		m.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				//Calculate
				equation = outputScreen.getText();
				equation = compute(equation,"*");
				equation = compute(equation,"/");
				equation = compute(equation,"+");
				equation = compute(equation,"-");
				
				outputScreen.setText("");
				outputScreen.setText(equation);
				
				bM = bM?false:true;
			}
		});
		
		e.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				bE = bE?false:true;
			}
		});
		
		up.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				bPow = bPow?false:true;
				
				if(bShift && bM && bE && bPow)
				{
					bOrdering = true;
					JOptionPane.showMessageDialog(null, "Welcome to the blunt ordering system :)\nClick on 'instructions' under 'Options' to see more info.","Welcome",JOptionPane.INFORMATION_MESSAGE);
					//System.out.println("Combination detected!");
				}
			}
		});
	}
	
	/**
	 * 
	 * @return an alphanumeric ID of 16 chars
	 */
	private String getID()
	{
		//excludes \ & "
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+{}|:<>?/.,';][=-`~";
		String id = "";
		int max = new Random().nextInt((16 - 12) + 1)+12;
		for(int i=0;i<16;i++)
		{
			int r = new Random().nextInt((chars.length()-1 - 0) + 1)+0;
			id+=chars.charAt(r);
		}
		return id;
	}
	
	/**
	 * Computes a given equation - use with caution - operator precedence!
	 * @param equ Equation to be computed.
	 * @param op Operator to be searched for.
	 * @return simplified equation without the supplied operator.
	 */
	private String compute(String equ,String op)
	{
		String equation = equ;
		Pattern mdPattern = Pattern.compile("(\\d+([.]\\d+)*)((["+op+"]))(\\d+([.]\\d+)*)");
		Matcher matcher		= mdPattern.matcher(equation);
		while(matcher.find())
		{
			String[] arr = null;
			double ans = 0;
			String eq = matcher.group(0);//get form x*y
			if(eq.contains(op))
			{
				arr = eq.split("\\"+op);//make arr
				if(op.equals("*"))
					ans = Double.valueOf(arr[0])*Double.valueOf(arr[1]);//compute
				if(op.equals("/"))
					ans = Double.valueOf(arr[0])/Double.valueOf(arr[1]);//compute
				if(op.equals("+"))
					ans = Double.valueOf(arr[0])+Double.valueOf(arr[1]);//compute
				if(op.equals("-"))
					ans = Double.valueOf(arr[0])-Double.valueOf(arr[1]);//compute
			}
			/*if(eq.contains("/"))
			{
				arr = eq.split("/");//make arr
				ans = Double.valueOf(arr[0])/Double.valueOf(arr[1]);//compute
			}
			if(eq.contains("+"))
			{
				arr = eq.split("/");//make arr
				ans = Double.valueOf(arr[0])/Double.valueOf(arr[1]);//compute
			}
			if(eq.contains("-"))
			{
				arr = eq.split("/");//make arr
				ans = Double.valueOf(arr[0])/Double.valueOf(arr[1]);//compute
			}*/
			equation = matcher.replaceFirst(String.valueOf(ans));//replace in equation
			matcher = mdPattern.matcher(equation);//look for more matches
			//System.out.println(eq + ":" + equation);
		}
		return equation;
	}
	
	private void initIcons() throws IOException
	{
		upArrow								= ImageIO.read(new File("./images/up.png"));
		downArrow							= ImageIO.read(new File("./images/down.png"));
		leftArrow							= ImageIO.read(new File("./images/left.png"));
		rightArrow							= ImageIO.read(new File("./images/right.png"));
		backspace							= ImageIO.read(new File("./images/backspace.png"));
		
		addImg								= ImageIO.read(new File("./images/add.png"));
		subImg								= ImageIO.read(new File("./images/minus.png"));
		multiplyImg							= ImageIO.read(new File("./images/multiply.png"));
		divideImg							= ImageIO.read(new File("./images/divide.png"));
		powerImg							= ImageIO.read(new File("./images/power.png"));
		eImg								= ImageIO.read(new File("./images/e.png"));
		srootImg							= ImageIO.read(new File("./images/square_root.png"));
		shiftImg							= ImageIO.read(new File("./images/shift.png"));
		cImg								= ImageIO.read(new File("./images/c.png"));
		equalsImg							= ImageIO.read(new File("./images/equal_sign.png"));
		mImg								= ImageIO.read(new File("./images/m.png"));
		inputImg							= ImageIO.read(new File("./images/input.png"));
		internetImg							= ImageIO.read(new File("./images/no_internet.png"));
		pendingImg							= ImageIO.read(new File("./images/pending.png"));
		readyImg							= ImageIO.read(new File("./images/ready.png"));
		shiftingImg							= ImageIO.read(new File("./images/shifting.png"));
		exclaimImg							= ImageIO.read(new File("./images/exclaim.png"));
		cancelledImg						= ImageIO.read(new File("./images/cancelled.png"));
		//
		upArr								= new ImageIcon(getScaledImage(upArrow));
		downArr								= new ImageIcon(getScaledImage(downArrow));
		leftArr								= new ImageIcon(getScaledImage(leftArrow));
		rightArr							= new ImageIcon(getScaledImage(rightArrow));
		backspc								= new ImageIcon(getScaledImage(backspace));

		addIco								= new ImageIcon(getScaledImage(addImg));
		subIco								= new ImageIcon(getScaledImage(subImg));
		multiplyIco							= new ImageIcon(getScaledImage(multiplyImg));
		divideIco							= new ImageIcon(getScaledImage(divideImg));
		powerIco							= new ImageIcon(getScaledImage(powerImg));
		eIco								= new ImageIcon(getScaledImage(eImg));
		srootIco							= new ImageIcon(getScaledImage(srootImg));
		shiftIco							= new ImageIcon(getScaledImage(shiftImg));
		inputIco							= new ImageIcon(getScaledImage(inputImg));
		cIco								= new ImageIcon(getScaledImage(cImg));
		equalsIco							= new ImageIcon(getScaledImage(equalsImg));
		mIco								= new ImageIcon(getScaledImage(mImg));
		internetIco							= new ImageIcon(getScaledImage(internetImg));
		pendingIco							= new ImageIcon(getScaledImage(pendingImg));
		readyIco							= new ImageIcon(getScaledImage(readyImg));
		shiftingIco							= new ImageIcon(getScaledImage(shiftingImg));
		exclaimIco							= new ImageIcon(getScaledImage(exclaimImg));
		cancelledIco						= new ImageIcon(getScaledImage(cancelledImg));
	}
	
	public static void main(String[] args) 
	{
		new Main().setVisible(true);;
		//System.out.println("WTF");
	}
}
