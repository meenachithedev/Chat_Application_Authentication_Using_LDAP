import java.awt.*;        //import packages for GUI
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

//import packages for LDAP Authentication
import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.directory.ModificationItem;
import javax.naming.NamingException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.AuthenticationException;

//Server class
class Frameserver extends JFrame   
{
  //vector to store the clients connected to the server 
  static Vector<ClientHandler> ar;    
  //count of the clients connected                      
  int count;                          
  //to create a serversocket bound to the particular port
  ServerSocket ss;                    
  Socket s;
  JLabel label1;
  InputStream in;
  OutputStream out;
  //reader is to read the data from the stream
  DataInputStream reader;            
  //writer is to write the data to the stream
  DataOutputStream writer;           
  DirContext adminContext;
  String status;
  Hashtable<String, String> environment;   

  //constructor for Frameserver class
  Frameserver(String str) throws Exception        
  {
    super(str);
    ar=new Vector<>();
    count=0;
    status="";
    //serverSocket bounded to the port 1234
    ServerSocket ss=new ServerSocket(1234);        

    label1=new JLabel("Connection Iniatiated");       
  
    setSize(300,210);
    setVisible(true);
     //layout
    setLayout(new FlowLayout());                   
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        
   //adding components to the GUI
    add(label1);
   
    //Loop to connect more than one client to the Server
    while(true)                                 
     {
        //accepts the request from the client
        Socket s=ss.accept();              
        out=s.getOutputStream();
        in=s.getInputStream();
        reader=new DataInputStream(in);
        writer=new DataOutputStream(out);
         //it stores the length of the data received from the client
        int received_length=reader.readInt();    
        //byte array to read the data from the Stream
        byte[] d=new byte[received_length];      
         //reads the data fully from the stream 
        reader.readFully(d,0,d.length);          
        //converts the bytes into string
        String details=new String(d).trim();     
        //divides the received message into substrings info,username and password
        StringTokenizer token1=new StringTokenizer(details,"\\"); 
        String info=token1.nextToken();
        System.out.println("msg"+info);
        writer.flush();
         //to store the username received
        String Username="";        
        //to store the password received
        String password="";         
        String ack="";
        //Based on the info received the corresponding method is called
        //if the info equals addUser method is called
        if(info.equals("signup"))
        {
            Username=token1.nextToken();
            password=token1.nextToken();
            String lastname=token1.nextToken();
            String status1=addUser(Username,password,lastname);
            System.out.println(Username+password);
            ack=status1;
            writer.write(ack.getBytes());
            System.out.println("msgentered");
            System.out.println("Inserted");
        //if the info equals ldap_auth method isa called
       }
       else if(info.equals("Login"))
       {
         ack="received";
         Username=token1.nextToken();
         password=token1.nextToken();
         writer.writeInt(ack.length());
         writer.write(ack.getBytes());
         //ldap_auth is called ,it returns the boolean value
         boolean flag=ldap_auth(Username,password);
         //if the received value is true clientHandler thread is created and 
         if(flag)
         {
           ClientHandler clientc=new ClientHandler(s,Username,count,reader ,writer,label1,adminContext);
           Thread t=new Thread(clientc);
           ar.add(clientc);
           t.start();
           count++;
           String txt="Established\\"+status;
           writer.writeInt(txt.length());
           writer.write(txt.getBytes());
           System.out.println("falg:"+flag);
         } 
         //if the received value is false it writes to the client, not established message to indicate login is not done and what is the nistake
         else
         {
           String txt="Not Established\\"+status;
           writer.writeInt(txt.length());
           writer.write(txt.getBytes()) ;
         }
       }
     }
  }
 
   //ldap_admin method is to bind to the LDAP Directory as admin.
   //authenticate with uid=admin,UserPassword=secret
   public boolean ldap_admin()
    {
        //to set up the environment for creating initial context
        environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //to connect to the LDAP server
        environment.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        //to mention the authenication property
        //it can be simple,none,sasl_mech
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        //to specify DN         
        environment.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        //to specify the credentials
        environment.put(Context.SECURITY_CREDENTIALS, "secret");
        try
        {
          //create the initial context
        adminContext = new InitialDirContext(environment);
        //if it is created it returns successsful
        status="Successful";
        return true;
        }
        //if the intial context is not created it returns false
        catch(Exception e)
        {
          System.out.println("Access Denied"+e);
          status="Not Successful";
          return false;
        }
    }

    public boolean ldap_auth(String uname,String Password)
    {
        //if the authentication of the admin fails it returns false
        if(!ldap_admin())
        {
          return false; 
        }
        String distinguishedName="";
        boolean flag=false;
      try
      {
        //it search for DN(Distinguished name ) of the user
        String filter = "(&(objectClass=inetOrgPerson)(cn="+uname+"))";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> searchResults = adminContext.search("ou=system", filter, searchControls);
        if (searchResults.hasMore()) 
        {
            System.out.println("inside if");
            SearchResult result = (SearchResult) searchResults.next();
            distinguishedName = result.getNameInNamespace();
        }
      }
      catch(NamingException ne)
      {
        System.out.println(ne);
        status=ne+" ";
      } 
        //using the Dn found during search the intial context is created for the client 
        if(!distinguishedName.equals(""))
        {
          try
          {
          environment.put(Context.SECURITY_PRINCIPAL, distinguishedName);
          environment.put(Context.SECURITY_CREDENTIALS, Password);
          DirContext usercontext = new InitialDirContext(environment);
          flag=true;
          status="Sucessfully Logged In";
          }
          // if there is error in creating the inital context for the client
          //Exceptions are thrown
          catch (AuthenticationNotSupportedException ex) 
          {
           System.out.println("The authentication is not supported by the server");
           status="The authentication is not supported by the server";
          }
          catch (AuthenticationException ex)
          {
           System.out.println("Incorrect password or username");
           status="Incorrect password or username";
          } 
          catch (NamingException ex) 
          {
           JOptionPane.showMessageDialog(this, "Error when trying to create the context");
           System.out.println(ex+"Error when trying to create the context");
           status="Error when trying to create the context";
          }
        }
        return flag ;
    }
  //This method is used to add the entry to the LDAP Directory
  public String addUser(String username,String password,String lastname)
  {
     if(!ldap_admin())
     {
       return "Not Successful";
     }
     try
     {
      Attributes entry=new BasicAttributes(true);
      Attribute attr1=new BasicAttribute("objectClass");
      attr1.add("inetOrgPerson");
      //Defining the attributes to be added to the entry
      Attribute attr2=new BasicAttribute("cn",username);
      Attribute attr3=new BasicAttribute("sn",lastname);
      Attribute attr4=new BasicAttribute("userPassword",password);
      //Adding the attribute to the entry
      entry.put(attr2);
      entry.put(attr3);
      entry.put(attr4);
      entry.put(attr1);
      //Creating the subcontext using the Dn and the attributes of the Entry 
      String entryDn="cn="+username+",ou=users,ou=system";
      adminContext.createSubcontext(entryDn,entry);
      return "Successfully Signed Up";
      }
      catch(NamingException ne)
      {
        System.out.println("invalid name"+ne);
        return "Username Already Exists";
      }
      
  }

  public static void main(String args[]) throws Exception
   {
     //creating the object for the FrameServer
      Frameserver frame=new Frameserver("Server");
   }
}

//ClientHandler class
class ClientHandler implements Runnable                  
{
  Socket s;
  final  DataInputStream in;
  final DataOutputStream out;
  String username;
  boolean status;
  static JLabel label1;
  static String text;
  int count;
  DirContext adminContext;
  
  //clientHandler class
  ClientHandler(Socket s,String str1,int count,DataInputStream in, DataOutputStream out,JLabel l1,DirContext adminContext)   //constructor for clientHandler
  {
    this.s=s;
    this.in=in;
    this.out=out;
    this.username=str1;
    this.status=true;
    this.label1=l1;
    this.count=count;
    this.adminContext=adminContext;
  }
  
  public void run()              
  {
    //reading the input stream and get message form the client
    while(true)
    {
     try
     {
       //to read the lenght of the data and the actual data fully
      int r_length=in.readInt();
      byte[] data=new byte[r_length];
      // read the message from the inputstream 
      in.readFully(data,0,data.length);
      String sendmsg;            
      String s1=new String(data).trim();
      StringTokenizer spliter=new StringTokenizer(s1,"\\");
      String request=spliter.nextToken();
      out.flush();
      //Based on the request received it performs the action 
      //if the request eqauls modify modify_password method is called 
      if(request.equals("modify"))
      {
         String newpassword=spliter.nextToken(); 
         String s="modify"+modify_Password(username,newpassword);
      }
      if(request.equals("logout"))
      {
        Frameserver.ar.remove(this);
      }
      if(request.equals("delete"))
      {
        String s="delete\\"+deleteEntry();
      }
      //if the request equals text and file,it gets the username and sends it by calling the sendmsg()
      if(request.equals("text"))
      {
        text=spliter.nextToken();
        String Client_name=spliter.nextToken();
        if(text=="bye")            
         {
           this.status=false;
           Frameserver.ar.remove(this);
           this.s.close();
           break;
         }         
         sendmsg="text\\"+username+"\\"+text;
         sendmsg(Client_name,sendmsg);
         System.out.println(sendmsg);
      }
      if(request.equals("file"))
      {
        String Client_name=spliter.nextToken();
        sendmsg="file\\"+username+"\\"+spliter.nextToken()+"\\"+spliter.nextToken();
        sendmsg(Client_name,sendmsg);
        System.out.println(sendmsg);
      }
     }

     catch(Exception e){}
    }
  }
   //method to send the message to the client 
  public static void sendmsg(String name,String sendmsg)                
  {
      String client=name;
       //search the client in the list of connected devices
      for(ClientHandler mc:Frameserver.ar)             
      {
         if(mc.username.equals(client.trim()) && mc.status==true)  
          {
            try
            {
               // write the message to the output stream
              mc.out.writeInt(sendmsg.length());
              mc.out.write(sendmsg.getBytes());       
            }
            catch(IOException ie)
            { ie.printStackTrace();
            }
          }
      }
  }
  //modify_password method is to change the password in the Ldap directory
  public String modify_Password(String cn,String newpassword) throws Exception
  {
      String BaseDn=",ou=users,ou=system";
      ModificationItem[] change=new ModificationItem[1];
      change[0]=new ModificationItem(DirContext.REPLACE_ATTRIBUTE,new BasicAttribute("userPassword",newpassword));
      adminContext.modifyAttributes("cn="+username+BaseDn,change);
      return "Password Modified";
  }
  //to destroy the LDAP Entry
  public String deleteEntry() throws Exception
  {
    String basedn=",ou=users,ou=system";
    adminContext.destroySubcontext("cn="+username+basedn);
    return "Deleted";
  }

}