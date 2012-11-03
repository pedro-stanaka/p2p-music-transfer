
package p2pfileshare;

import java.io.FileInputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

// this class will create input pipe for chatting services and shows the incoming messages
public class ChatInput extends Thread implements PipeMsgListener 
{   //Class variables
    private JTextArea txtChat=null , log=null;
    private PeerGroup SaEeDGroup=null;
    private String myPeerID = null;
    private PipeService myPipeService =null;
    private PipeAdvertisement pipeAdv =null;
    private InputPipe pipeInput = null;
    
    /** Creates a new instance of ChatInput */
    public ChatInput(PeerGroup group, JTextArea log, JTextArea chat) 
    {
        this.log = log;
        this.txtChat = chat;
        this.SaEeDGroup = group;
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
        
    
    private void getServices()
    {   //Obtaining Peer Group services
        printOnLog("[+]Getting Services for Chat component...\n");
        myPipeService = SaEeDGroup.getPipeService();
        myPeerID = SaEeDGroup.getPeerID().toString();
        
        try{ //Creates input pipe
            FileInputStream is = new FileInputStream("saeedPipe.adv");
            pipeAdv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(MimeMediaType.XMLUTF8,is);
            is.close();
        }catch(Exception e){
            printOnLog("[+]Exception: " + e.getMessage()+"\n");
            e.printStackTrace();
            System.exit(-1);
        }
        printOnLog("[+]Input Pipe Successfully Created.\n");
        
    }
    public void startListening() //This method will start listening for incoming messages thro created pipe
    {
        printOnLog("[+]Start Listening for Incoming Messages.\n");
        try{
            pipeInput = myPipeService.createInputPipe(pipeAdv,this);
            
        }catch(Exception e){
            printOnLog("[-]Exception: " + e.getMessage()+"\n");
            return;
        }
        if(pipeInput == null){
            printOnLog("[-]Failure in Opening Input Pipe :-(\n");
            System.exit(-1);
        }
    }
    public void stopListening() //This method will stop input pipe
    {
        pipeInput.close();
        printOnLog("[-]Input Pipe Closed for Incomming Message.\n");
    }
    //this listener will respond to incoming messages and show them in Designated area
    public void pipeMsgEvent(PipeMsgEvent ev)
    {
        printOnLog("[+]Message Received...\n");
        Message myMessage = null;
        try{
            myMessage = ev.getMessage();
            if(myMessage == null){
                return;
            }
        }catch(Exception e){
            printOnLog("[-]Exception happend when trying to get Message element!");
            e.printStackTrace();
        }
        //Assigning values to wanted Tages
        ElementIterator el = myMessage.getMessageElements();
        MessageElement me = myMessage.getMessageElement("peerName");
        MessageElement me2 = myMessage.getMessageElement("peerID");
        MessageElement me3 = myMessage.getMessageElement("chatMessage");
        MessageElement me4 = myMessage.getMessageElement("Time");
        
        final String peerName = me.toString();
        String peerID = me2.toString();
        final String msgContent = me3.toString();
        printOnLog("[+]Message == "+msgContent+"...\n");
        final String sentTime = me4.toString();
        if(me.toString() == null || me2.equals(myPeerID)){
            return;
        }
        else{
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    txtChat.append("[ " + peerName + "@" + sentTime +"]  " + msgContent + "\n");
                }
            });
        }  
    }
    
}
