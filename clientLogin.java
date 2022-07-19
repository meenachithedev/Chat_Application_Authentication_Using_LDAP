import java.awt.*;     //importing the packages
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

//Frameclient1 class 
class Frameclient1 extends JFrame implements ActionListener    
{
  //Declaring the components
  final static int ServerPort=1234;
  InetAddress ip;
  Socket s;
  JFrame loginf;
  JLabel label1,label2,label3;
  JTextArea textReceived;
  JTextField message;
  JButton buttonSend,choose_file,openfile,sendFile;
  JMenuBar menuBar;
  JMenu MyAccount,settings;
  JMenuItem modifyp,logout,delete_acc;
  JFileChooser file_chooser;
  OutputStream out;
  InputStream in;
   DataInputStream reader;
   DataOutputStream writer;
  Thread sendmsg;
  Thread readmsg;
  GridBagConstraints constraints;
  String username,password;
  File file,rfile;
  FileInputStream fin;
  FileOutputStream fout;
  JPanel panel1,panel2,panel3,panel4,mainpanel;
  JTextField enter_username;
  
  //constructor for Frameclient 
  Frameclient1(String str,JFrame auth,String name,String p)          
  {
    super(str);
    //username of the client
    username=name;    
    //password of the client
    password=p;       
    loginf=auth;
    String chk="";
    try
    {
    //get the ip
    ip=InetAddress.getByName("localhost");
    //socket creation
    Socket s=new Socket(ip,ServerPort);
    out=s.getOutputStream();
    in=s.getInputStream();
    //reader object to read the stream
    reader=new DataInputStream(in);
    //writer object to write the stream
    writer=new DataOutputStream(out);
    String info="Login";
    String details=info+"\\"+username+"\\"+password;
    //writes the details from the login frame
    writer.writeInt(details.length());
    writer.write(details.getBytes());
    int length=reader.readInt();
    byte[] ack=new byte[length];
    reader.readFully(ack,0,ack.length);
    String str_ack=new String(ack);
    System.out.println("Ack:"+str_ack);
    writer.flush();
    //reads the message from stream 
    //status of the authentication is read from the stream 
    length=reader.readInt();
    byte[] res=new byte[length];
    reader.readFully(res,0,res.length);
    String str_res=new String(res);
    StringTokenizer token=new StringTokenizer(str_res,"\\");
    chk=token.nextToken().trim();
    String Status=token.nextToken().trim();
    JOptionPane.showMessageDialog(this,Status,"Status",JOptionPane.PLAIN_MESSAGE);
    writer.flush();
    }
    catch(IOException ie)
     {
       System.out.println(ie);
     }
    //if the status is established it logs in and ready to send message 
    if(chk.equals("Established"))
    {
    System.out.println("inside if");
    loginf.setVisible(false);
    //Initialises the components
    label1=new JLabel("Username:"+username);             //GUI
    label2=new JLabel("Enter Message:");
    label3=new JLabel("Enter Username:");
    panel1=new JPanel();
    panel2=new JPanel();
    panel3=new JPanel();
    textReceived=new JTextArea(15,20);
    message=new JTextField(20);
    buttonSend=new JButton(" send ");
    choose_file=new JButton(" Choose File ");
    openfile=new JButton(" Open File");
    sendFile=new JButton(" Send File");
    menuBar=new JMenuBar();
    MyAccount=new JMenu("My Account");
    settings=new JMenu("Settings");
    delete_acc=new JMenuItem("Delete Account");
    modifyp=new JMenuItem(" Modify Password");
    logout=new JMenuItem("Logout");
    enter_username=new JTextField(20);
    settings.add(modifyp);
    settings.add(logout);
    settings.add(delete_acc);
    MyAccount.add(settings);
    menuBar.add(MyAccount);
    setVisible(true);
    //Layout
    constraints = new GridBagConstraints();
    setLayout(new FlowLayout(FlowLayout.LEFT, 20, 25));
    setJMenuBar(menuBar);
    panel2.setLayout(new GridLayout(2,2,3,3));
    panel2.add(label2);
    panel2.add(message);
    panel2.add(label3);
    panel2.add(enter_username);
    panel3.setLayout(new FlowLayout());
    buttonSend.setAlignmentX(Component.CENTER_ALIGNMENT);
    choose_file.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel3.add(buttonSend);
    panel3.add(choose_file);
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(label1);
    panel1.add(new JScrollPane(textReceived));
    panel1.add(panel2);
    panel1.add(panel3);
    add(panel1);
    pack();
    setSize(500,450);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //adding action listener
    message.addActionListener(this);         
    buttonSend.addActionListener(this);
    choose_file.addActionListener(this);
    modifyp.addActionListener(this);
    logout.addActionListener(this);
    delete_acc.addActionListener(this);
    openfile.addActionListener(this);
    sendFile.addActionListener(this);
           
    // Thread to receive message from the Stream
    readmsg=new Thread(new Runnable()         
    {
      public void run()
      {
        while(true)
        {
         try
         {
           int received_length=reader.readInt();
           byte[] data=new byte[received_length];
           reader.read(data,0,data.length);
           String info=new String(data);
           System.out.println(info);
           StringTokenizer spliter=new StringTokenizer(info,"\\");
           String request=spliter.nextToken();
           writer.flush();
           //if the request equals text it prints the message in the text area
           if(request.equals("text"))
           {
             String client_name=spliter.nextToken();
             String msg=spliter.nextToken();
             textReceived.append(client_name+": " +msg+"\n");
           }
           //if the request equals file it shows that the file received and shows the download page 
           else if(request.equals("file"))
           {
              String client_name=spliter.nextToken();
              String file_name=spliter.nextToken();
              String content=spliter.nextToken();
              textReceived.append(client_name+": " +file_name+" Received\n");
              //creates the download class object to show the download page
              download download_obj=new download(client_name,file_name,content);
              File rfile1=null;
              String status1="";
              while(rfile1==null|| status1.equals(""))
              {
              rfile=download_obj.getFile();
              rfile1=download_obj.getFile();
              status1=download_obj.getStatus();
              }
              if(status1.equals("Download"))
              {
              writer.flush();
              openfile.setAlignmentX(Component.CENTER_ALIGNMENT);
              panel3.add(openfile);
              pack();
              setSize(500,450);
              }
           }
         }
         catch(IOException ie){}
      }
    }
    });
    readmsg.start();
    }
    
 }
 
  public void actionPerformed(ActionEvent ae) 
  {
    //if the send button is clicked it send the message entered 
    if(ae.getSource()==buttonSend)
    {
     final String send_msg=message.getText();
     String client_name=enter_username.getText(); 
     if((send_msg.equals("")||client_name.equals("")))
     {
         return;
     }
     message.setText("");
     enter_username.setText("");
     System.out.println(" Name:"+client_name);
      // Thread to send message to the server
     sendmsg=new Thread(new Runnable()  
     {
     public void run(){
      try
      { 
     String str1="text\\"+send_msg+"\\"+client_name;
     writer.writeInt(str1.length());
     //write message to the outputstream 
     writer.write(str1.getBytes());   
     }
     catch(Exception e)
     {}
    }
   });
   sendmsg.start();
   }
   
   //if the choose File button is clicked it shows the file chooser page
   if(ae.getSource()==choose_file)
   {
     file_chooser=new JFileChooser();
     file_chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
     int opt=file_chooser.showOpenDialog(this);
     if(opt==JFileChooser.APPROVE_OPTION)
     {
       String file_path=file_chooser.getSelectedFile().toString().trim();
       file=new File(file_path);
       sendFile.setAlignmentX(Component.CENTER_ALIGNMENT);
       panel3.add(sendFile);
       pack();
       setSize(500,450);
      }
   }
   //if the modifypassword menuitem is clicked it shows the modify password screen
   if(ae.getSource()==modifyp)
   {
     new modifyPassword(reader,writer,username);
   }
   //to Logout
   if(ae.getSource()==logout)
   {
     try
     {
     setVisible(false);
     loginf.setVisible(true);
     String str2="logout";
     writer.writeInt(str2.length());
     //write message to the outputstream 
     writer.write(str2.getBytes());
     }
     catch(Exception e)
     {
       System.out.println(e);
     }
   }
   if(ae.getSource()==delete_acc)
   {
     try
     {
     String str3="delete";
     writer.writeInt(str3.length());
     writer.write(str3.getBytes());
     setVisible(false);
     loginf.setVisible(true);
     }
     catch(Exception e)
     {
       System.out.println(e);
     } 
   }
   //opens the downloaded file 
   if(ae.getSource()==openfile)
   {
     try
     {
         Desktop desktop=Desktop.getDesktop();
         desktop.open(rfile);
         panel3.remove(openfile);
         pack();
         setSize(500,450);
     }
     catch(Exception e)
     {
       System.out.println("Cant Open the File:"+e);
     }
   }
   //sends the file choosed 
   if(ae.getSource()==sendFile)
   {
     try
       {
         String name_client=enter_username.getText();
         if(name_client.equals(""))
         {
           JOptionPane.showMessageDialog(this,"Enter the Username","Error",JOptionPane.ERROR_MESSAGE);
           return;
         }
         String file_name=file.getName();
         System.out.println("File name:"+file_name);
         fin=new FileInputStream(file);
         byte[] filedata=new byte[(int)file.length()];
         fin.read(filedata);
         String message="file\\"+name_client+"\\"+file_name+"\\"+new String(filedata);
         writer.writeInt(message.length());
         writer.write(message.getBytes());
         System.out.println("sent");
         panel3.remove(sendFile);
         pack();
         setSize(500,450);
       }
       catch(Exception e)
       {
         System.out.println(e);
       }
   }
   }
}

//clientLogin class
class clientLogin extends JFrame  implements ActionListener
{
    JLabel lusername;
    JLabel lpassword;
    JTextField tusername;
    JPasswordField tpassword;
    JButton login,sign_up,hide_p;
    JCheckBox show_p;
    JPanel loginpanel,mainpanel,buttonpanel;

    //clientLogin constructor
    clientLogin(String name)
    {
        super(name);
        lusername=new JLabel("Username");
        lpassword=new JLabel("Password");
        tusername=new JTextField(20);
        tpassword=new JPasswordField(20);
        sign_up=new JButton("Sign Up");
        login=new JButton("Login");
        show_p=new JCheckBox("Show Password");
        hide_p=new JButton("Show_p");
        loginpanel=new JPanel();
        mainpanel=new JPanel();
        buttonpanel=new JPanel();

        setVisible(true);
        setUpform();
        this.setContentPane(mainpanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        login.addActionListener(this);
        sign_up.addActionListener(this);
        show_p.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae)
    {
        if(ae.getSource()==login)
        {
            //validates the details 
            //if it is valid it performs the action
            if(!validate_Details())
            {
              return;
            }
            String name=tusername.getText().trim();
            String password=tpassword.getText().trim();
            Frameclient1 clientobj=new Frameclient1("Client",this,name,password); 
            tusername.setText("");
            tpassword.setText("");
        }
        if(ae.getSource()==sign_up)
        {
            setVisible(false);
            //shows the signup screen
            sign_upFrame sobj=new sign_upFrame("Sign_Up",this);
            
        }
        if(ae.getSource()==show_p)
        {
          //to hide and show the password
          JCheckBox c=(JCheckBox) ae.getSource();
          tpassword.setEchoChar(c.isSelected() ? (char)0 :(Character)UIManager.get("PasswordField.echoChar"));
        }
    }
    //setupfrom method
    public void setUpform()
    {
        loginpanel.setLayout(new GridLayout(5,2,3,3));
        loginpanel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        loginpanel.add(lusername);
        loginpanel.add(tusername);
        loginpanel.add(lpassword);
        loginpanel.add(tpassword);
        loginpanel.add(show_p);

        mainpanel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
        buttonpanel.setLayout(new FlowLayout());
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        sign_up.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonpanel.add(login);
        buttonpanel.add(sign_up);
        mainpanel.add(loginpanel);
        mainpanel.add(buttonpanel);
    }

    //validates the entered details and return true or false
    public boolean validate_Details()
    {
      if(tusername.getText().equals("")&&tpassword.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Enter the Username and Password","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      else if(tusername.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Enter the Username!","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      else if(tpassword.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Enter the password!","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      return true;
    }   

    public static void main(String args[])
    {
      //creates the object for the clientLogin
        clientLogin obj=new clientLogin("Login");
    }
}

//sign_upFrame class
class sign_upFrame extends JFrame implements ActionListener
{
    JLabel label_username;
    JLabel label_password;
    JLabel label_rpassword;
    JLabel surname;
    JTextField txt_username;
    JPasswordField txt_password;
    JPasswordField txt_rpassword;
    JTextField tsurname;
    JCheckBox show_p;
    JButton submit;
    JPanel sign_uppanel,main1panel;
    JFrame login_frame;
    Socket temp;
    OutputStream msg;
    InputStream ack;
    DataInputStream r;
    DataOutputStream w;
    
    //sign_upFrame Constructor
    sign_upFrame(String str,JFrame loginf) 
    {
      super(str);
      label_username=new JLabel("First Name");
      label_password=new JLabel("Password");
      surname=new JLabel("Last Name");
      label_rpassword=new JLabel("Re-enter Password");
      show_p=new JCheckBox("Show Password");
      txt_username=new JTextField(20);
      txt_password=new JPasswordField(20);
      txt_rpassword=new JPasswordField(20);
      tsurname=new JTextField(20);
      submit=new JButton("Submit");
      sign_uppanel=new JPanel();
      main1panel=new JPanel();
      login_frame=loginf;
      
      setVisible(true);
      setUpform1();
      this.setContentPane(main1panel);
      pack();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //adds action Listener
      submit.addActionListener(this);
      show_p.addActionListener(this);
    }

    //setUpform1 method to arrange the components
    public void setUpform1()
    {
        sign_uppanel.setLayout(new GridLayout(5,2,3,3));
        sign_uppanel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        sign_uppanel.add(label_username);
        sign_uppanel.add(txt_username);
        sign_uppanel.add(surname);
        sign_uppanel.add(tsurname);
        sign_uppanel.add(label_password);
        sign_uppanel.add(txt_password);
        sign_uppanel.add(label_rpassword);
        sign_uppanel.add(txt_rpassword);
        sign_uppanel.add(show_p);

        main1panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        main1panel.setLayout(new BoxLayout(main1panel, BoxLayout.Y_AXIS));
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        main1panel.add(sign_uppanel);
        main1panel.add(submit);
    }

    public void actionPerformed(ActionEvent ae)
    {
      //sends the username,password to the server to create a new entry in the LDAP directory
      if(ae.getSource()==submit)
      {
         try
         {  
           String info="signup";
           //validates the details entered
           boolean check=validate_Details();
           if(!check)
           {
             return;
           }
           String username=txt_username.getText().trim();
           String lastname=tsurname.getText().trim();
           String password=txt_password.getText().trim();
           String newpassword=txt_rpassword.getText().trim();
           temp=new Socket("localhost",1234);
           msg=temp.getOutputStream();
           ack=temp.getInputStream();
           r=new DataInputStream(ack);
           w=new DataOutputStream(msg);
           String details=info+"\\"+username+"\\"+password+"\\"+lastname;
           System.out.println(details);
           //writes the details to the stream
           w.writeInt(details.length());
           w.write(details.getBytes());
           byte[] ackb=new byte[100];
           r.read(ackb);
           String str_ack=new String(ackb);
           System.out.println("Ack:"+str_ack);
           JOptionPane.showMessageDialog(this,str_ack,"Status",JOptionPane.PLAIN_MESSAGE);
           setVisible(false);
           login_frame.setVisible(true);
         }
         catch(Exception e){
           System.out.println(e);
         }
      }
      //to show and hide the password
      if(ae.getSource()==show_p)
      {
        JCheckBox c=(JCheckBox) ae.getSource();
        txt_password.setEchoChar(c.isSelected() ? (char)0 :(Character)UIManager.get("PasswordField.echoChar"));
        txt_rpassword.setEchoChar(c.isSelected() ? (char)0 :(Character)UIManager.get("PasswordField.echoChar"));
      }
    }
    
    //validates the details entered
    public boolean validate_Details()
    {
      if(txt_username.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Enter the Username!","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      if(txt_password.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Enter the password!","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      if(txt_rpassword.getText().equals(""))
      {
       JOptionPane.showMessageDialog(this,"Re Enter the password!","Error",JOptionPane.ERROR_MESSAGE);
       return false;
      }
      if(!txt_password.getText().equals(txt_rpassword.getText()))
      {
        JOptionPane.showMessageDialog(this,"Passwords not Matching!","Error",JOptionPane.ERROR_MESSAGE);
        return false;
      }
     return true;
    }
}

//modify class to modify the password  
class modifyPassword implements ActionListener
{
  JFrame modify;
  JPanel panel1,panel2;
  JPasswordField mpassword,newp;
  JLabel labelpassword,labelnewpassword,name;
  JButton Change;
  JCheckBox showp;
  DataInputStream in1;
  DataOutputStream out;

  //modifyPassword constructor
  modifyPassword(DataInputStream in,DataOutputStream out1,String name1) 
  {
    modify=new JFrame("Change Password");
    panel1=new JPanel();
    panel2=new JPanel();
    mpassword=new JPasswordField (10);
    newp=new JPasswordField (10);
    labelpassword=new JLabel("Password");
    labelnewpassword=new JLabel("Re-Enter Password");
    name=new JLabel("Username:"+name1);
    Change=new JButton("Modify");
    showp=new JCheckBox("Show Password");
    out=out1;
    in1=in;
    panel1.setLayout(new GridLayout(3,2,5,5));
    panel1.add(labelpassword);
    panel1.add(mpassword);
    panel1.add(labelnewpassword);
    panel1.add(newp);
    panel1.add(showp);
    panel2.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
    name.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel2.add(name);
    panel2.add(new JLabel("  "));
    panel2.add(panel1);
    Change.setAlignmentX(Component.CENTER_ALIGNMENT);
    modify.setContentPane(panel2);
    modify.add(Change);
    modify.setVisible(true);
    Change.addActionListener(this);
    modify.pack();
    modify.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    showp.addActionListener(this);
  }

  public void actionPerformed(ActionEvent ae)
  {
    //sends the new password to the server to change
    if(ae.getSource()==Change)
    {
        if(!mpassword.getText().equals(newp.getText()))
        {
          JOptionPane.showMessageDialog(modify,"Passwords are not Matching","Error",JOptionPane.ERROR_MESSAGE);
          return;
        }
        try
        {
        String pinfo="modify\\"+mpassword.getText();
        out.writeInt(pinfo.length());
        out.write(pinfo.getBytes());
        JOptionPane.showMessageDialog(modify,"Sucessfully Changed");
        out.flush();
        modify.setVisible(false);
        }
        catch(IOException ie)
        {
          System.out.println(ie);
        }
    }
    //shows and hide password
    if(ae.getSource()==showp)
      {
        JCheckBox c=(JCheckBox) ae.getSource();
        mpassword.setEchoChar(c.isSelected() ? (char)0 :(Character)UIManager.get("PasswordField.echoChar"));
        newp.setEchoChar(c.isSelected() ? (char)0 :(Character)UIManager.get("PasswordField.echoChar"));
      }
  }
}

//Download class
class download extends JFrame implements ActionListener
{
  JLabel label_msg;
  JButton download,cancel;
  String client_name,file_name,content;
  File rfile;
  FileOutputStream fout;
  JPanel panel1; 
  String status;

  //Download Constuctor
  download(String client_name,String file_name,String content)
  {
    super("Download");
    this.client_name=client_name;
    this.file_name=file_name;
    this.content=content;
    label_msg=new JLabel("Do You want to Download the File:"+file_name);
    download=new JButton("Download");
    cancel=new JButton("Cancel");
    panel1=new JPanel();
    panel1.setLayout(new FlowLayout());
    panel1.add(download);
    panel1.add(cancel);
    add(label_msg);
    add(panel1);
    setLayout(new FlowLayout());
    setVisible(true);
    pack();
    setSize(360,200);
    
    download.addActionListener(this);
    cancel.addActionListener(this);
  }

  public void actionPerformed(ActionEvent ae)
  {
     //downloads the file in the mentioned path
    if(ae.getSource()==download)
    {
       try
          {
              status="Download";
              String path="E:\\test\\"+file_name;
              System.out.println(path);
              rfile=new File(path);
              rfile.createNewFile();
              fout=new FileOutputStream(rfile);
              fout.write(content.getBytes());
              setVisible(false);
          }
          catch(IOException ie)
          {
            System.out.println(ie);
          }
    }

    //cancels 
    if(ae.getSource()==cancel)
    {
      status="cancel";
      setVisible(false);
    }
  }

  public File getFile()
  {
    return rfile;
  }
  public String getStatus()
  {
    return status;
  }
}