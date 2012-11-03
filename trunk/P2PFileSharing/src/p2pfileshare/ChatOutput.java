
package p2pfileshare;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

//This class will send messages thro pipe for chatting services
public class ChatOutput extends Thread implements Runnable,
                                                  OutputPipeListener,
                                                  RendezvousListener
{   //Class Variables
    private JTextArea log=null , txtChat =null;
    private String myPeerID = null,
                   myPeerName= null;
    private PeerGroup SaEeDGroup =null;
    private PipeService myPipeService = null;
    
    private OutputPipe pipeOut =null;
    private PipeAdvertisement pipeAdv=null;
    private DiscoveryService myDiscoveryService=null;
    
    private RendezVousService myRendezVousService=null;
    private String msg = null;
    /** Creates a new instance of ChatOutput */
    public ChatOutput(PeerGroup group,JTextArea log,JTextArea chat) 
    {
        this.log = log;
        this.txtChat = chat;
        this.SaEeDGroup=group;        
        getServices();
        
    }
    
    private void printOnLog(final String toPrint){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }
    
    private void getServices() //This method will obtain Peer Group Services
    {
        printOnLog("[+]Obtaining Services for chat...\n");
        try{
            myRendezVousService = SaEeDGroup.getRendezVousService();
            myRendezVousService.addListener(this);
            
        }catch(Exception e){
            printOnLog("[-]Cannot obtain RendezVous Services.");
            printOnLog("[-]Fatal Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        myDiscoveryService = SaEeDGroup.getDiscoveryService();
        myPipeService = SaEeDGroup.getPipeService();
        myPeerID = SaEeDGroup.getPeerID().toString();
        myPeerName = SaEeDGroup.getPeerName();
        
        try{//Creating Pipe Advertisements from file
            FileInputStream in = new FileInputStream("saeedPipe.adv");
            pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8,in);
            in.close();
        }catch(IOException e){
            printOnLog("[-]Exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        printOnLog("[+]Chat Services sucessfully obtained.\n");
        
    }
    public void startingPipe()
    {
        printOnLog("[+]Creating Output Pipe.\n");
        try{
            //starting remoted advertisement to search for pipe
            myDiscoveryService.getRemoteAdvertisements(null,DiscoveryService.ADV,null,null,1);
            myPipeService.createOutputPipe(pipeAdv,this);
        }catch(Exception e){
            printOnLog("[+]Exception: " + e.getMessage()+"\n");
            e.printStackTrace();
            System.exit(-1);
        }
        printOnLog("[+]Output Pipe Successfully Created.\n");
    }
    
    public void setMessage(String message){//This accessor will set messages that need to be sent
        this.msg = message;
    } 
    //Listener to send message thro pipe as requested        
    @Override
    public void outputPipeEvent(OutputPipeEvent ev)
    {
        printOnLog("[+]Sending Message.\n");
        pipeOut = ev.getOutputPipe();
        Message myMessage = null;
        
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        final String myTime = dateFormat.format(date).toString();
        
        try{
            
            myMessage = new Message();
            //adding timestap and peers details also messages to XML tag and send them 
            StringMessageElement sme = new StringMessageElement("peerName",myPeerName,null);
            StringMessageElement sme1 = new StringMessageElement("peerID",myPeerID,null);
            StringMessageElement sme2 = new StringMessageElement("chatMessage",msg,null);
            StringMessageElement sme3 = new StringMessageElement("Time",myTime,null);
            
            myMessage.addMessageElement(null,sme);
            myMessage.addMessageElement(null,sme1);
            myMessage.addMessageElement(null,sme2);
            myMessage.addMessageElement(null,sme3);
            //Trigger the Sending            
            pipeOut.send(myMessage);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    txtChat.append("[ " + myPeerName+"@" + myTime+ "]  " + msg + "\n");
                }
            });
            
            
        }catch(Exception e)
        {
            printOnLog("[-]Exception: " + e.getMessage()+"\n");
            e.printStackTrace();
            System.exit(-1);            
        }
        
    }
    
    @Override
    public synchronized void rendezvousEvent(RendezvousEvent event)
    {
        if(event.getType() == event.RDVCONNECT || event.getType() == event.RDVRECONNECT){
            notify();
        }
    }
}
