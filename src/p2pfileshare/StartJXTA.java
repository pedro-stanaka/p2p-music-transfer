
package p2pfileshare;
//This class is for initializing Peer and launch it into Default JXTA network and use its
//Services to Create out own group.
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
//importing JXTA Libraries
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public class StartJXTA 
{   //Class Variables
    public JTextArea log;
    private final static int TIMEOUT = 5*1000;
    private PeerGroup netPeerGroup = null,
              SaEeDGroup   =null;
    DiscoveryService myDiscoveryService =null,
                     SaEeDGroupDiscoveryService =null;
    PeerGroupAdvertisement SaEeDAdv =null;
    //unique id for group, it is taken from JXTA services
    private final String stringID = "jxta:uuid-4E0742B0E54F4D0ABAC6809BB82A311E02";
    
    /** Creates a new instance of StartJXTA */
    public StartJXTA(JTextArea txt) 
    {
        this.log = txt;
        launchJXTA();
        getServices();
        searchForGroup();
    }
    private void printOnLog(final String toPrint){
     SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }
    
    private void launchJXTA()
    {
        printOnLog("[+]Launching into JXTA Network...\n");
        try{
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();            
        }catch(PeerGroupException e){
            printOnLog("[-]Fatal Error:" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        
    }
    private void getServices()
    {
        //Obtaining JXTA Services from JXTA Global group
        printOnLog("[+]Obtaining Peer Group Services.\n");
        myDiscoveryService = netPeerGroup.getDiscoveryService();
    }
    private void searchForGroup() //This method will Search for Group
    {                            //If group found it will be join into it otherwise it
        Enumeration adv=null;    //will create the group itself.   
        int count =0;
        printOnLog("[+]Searching for SaEeDGroup Advertisements.\n");
        while(count < 5){
            try {
                printOnLog("[+]Try Number: " + count +"\n");
                //Searching for Group Advertisements , first from local cach if not
                //found then search from remote peers
                adv = myDiscoveryService.getLocalAdvertisements(DiscoveryService.GROUP,"Name","SaEeDGroup");
                if((adv != null) && adv.hasMoreElements()){
                    printOnLog("[+]SaEeDGroup found in Local advertisement.\n");
                    SaEeDAdv = (PeerGroupAdvertisement)adv.nextElement();
                    SaEeDGroup = netPeerGroup.newGroup(SaEeDAdv);
                    joinToGroup(SaEeDGroup);
                    break;
                }else{
                    printOnLog("[-]No Group Found in Local advertisement.\n[+]Starting Remote Search...\n");
                    myDiscoveryService.getRemoteAdvertisements(null,DiscoveryService.GROUP,"Name","SaEeDGroup",1);
                }
                Thread.sleep(TIMEOUT);
                //if group not found after couple of tries it will create the group itself
                if((count == 4) && (adv == null || !adv.hasMoreElements())){
                    printOnLog("[-]No Group Found!!! - Creating Group\n");
                    SaEeDGroup = createGroup();
                    joinToGroup(SaEeDGroup);
                    break;
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }catch(PeerGroupException e){
            printOnLog("[-]Fatal Error:" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
            }catch(InterruptedException e){
               printOnLog("[-]Fatal Error:" + e.getMessage());
               e.printStackTrace(); 
            }
            count++;
        }
    }
    private PeerGroup createGroup() //This method will Create SaEeD group :-)
    {
        printOnLog("[+]Creating New Group...\n");
        PeerGroup myNewGroup = null;
        try{
            //specifying advertisement for group and configure group, then publish it
            //Advertisement for remote peers
            ModuleImplAdvertisement myMIA = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
            myNewGroup = netPeerGroup.newGroup(getGID(),
                                               myMIA,
                                               "SaEeDGroup",
                                               "SaEeD P2P File Sharing Application");
            SaEeDAdv = myNewGroup.getPeerGroupAdvertisement();
            //publishing new group advertisements
            myDiscoveryService.publish(SaEeDAdv);
            myDiscoveryService.remotePublish(SaEeDAdv);
            printOnLog("[+]New Peer Group Successfully created :-)\n");
            printOnLog("[+]Publishing new Group Advertisements.\n");
            printOnLog("[+]Group Information:\n");
            printOnLog("[===========================]\n");
            printOnLog("[+]Group Name: " + SaEeDAdv.getName()+"\n");
            printOnLog("[+]Group ID:" + SaEeDAdv.getPeerGroupID().toString()+"\n");
            printOnLog("[+]Group Description: " + SaEeDAdv.getDescription()+"\n");
            printOnLog("[+]Group Module ID: " + SaEeDAdv.getModuleSpecID().toString()+"\n");
            printOnLog("[+]Advertisement Type: " + SaEeDAdv.getAdvertisementType()+"\n");
            printOnLog("[===========================]\n");
        }catch(Exception e){
            printOnLog("[*]Fatal Error:" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        return myNewGroup;
    }
    //This method will return peerGroupID from given String ID
    private PeerGroupID getGID() throws Exception{
        return (PeerGroupID) IDFactory.fromURL(new URL("urn","",stringID));
    }
    private void joinToGroup(PeerGroup group) //This method will join to either found group or created group
    {
        StructuredDocument creds = null;
        printOnLog("[===========================]\n");
        printOnLog("[+]Joining into SaEeDGroup..\n");
        
        try{
            //Athenticate and join to group
        AuthenticationCredential authCred = new AuthenticationCredential(group,null,creds);
        MembershipService membership = group.getMembershipService();
        Authenticator auth = membership.apply(authCred);
            if(auth.isReadyForJoin()){
                Credential myCred = membership.join(auth);
                printOnLog("[===== Group Details =====]");
                StructuredTextDocument doc = (StructuredTextDocument)myCred.getDocument(new MimeMediaType("text/plain"));
                StringWriter out = new StringWriter();
                doc.sendToWriter(out);
                
                printOnLog(out.toString());
                printOnLog("[+]Peer Name : " + group.getPeerName() + " is now online :-)\n");
                printOnLog("[+]Obtaining SaEeDGroup Services.\n");
                //Publishing Peer Advertisements.
                SaEeDGroupDiscoveryService = group.getDiscoveryService();
                printOnLog("[+]Publishing Peer Advertisement.\n");
                SaEeDGroupDiscoveryService.publish(group.getPeerAdvertisement());
                SaEeDGroupDiscoveryService.remotePublish(group.getPeerAdvertisement());
                
                printOnLog("[===========================]\n");
            }
            else{
                printOnLog("[!!]Fatal Error: Cannot Join to The Group!");
                System.exit(-1);
            }            
        }catch(Exception e){
                printOnLog("[!]Fatal Error: " + e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            }
    }
    
    public PeerGroup getSaEeDGroup() //This accessor will return group
    {
     return SaEeDGroup;   
    }
    public PeerGroupAdvertisement getSaEeDAdv(){//This accessor will return Advertisements
        return SaEeDAdv;
    }
    
}
