
package p2pEsPeTaC.share;

import p2pEsPeTaC.file.CheckSumCalc;
import java.io.File;
import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.share.CMS;
import net.jxta.share.Content;
import net.jxta.share.ContentManager;
import net.jxta.share.SearchListener;

//This class will share contents through peers in SaEeD Group
public class EsPeTaCSharing extends Thread implements SearchListener 
{
        //Defining Class Variables
    private PeerGroup SaEeDGroup =null;
    private JTextArea log=null;
    private File myPath = null;
    //using Content Management Service Library for Sharing purposes
    private CMS cms =null;
    
    /** Creates a new instance of SaEeDSharing */
    public EsPeTaCSharing(PeerGroup group, JTextArea log, File givenPath) 
    {
        this.log = log;
        this.SaEeDGroup = group;
        this.myPath = givenPath;
        launchCMS();
    }
    
    private void printOnLog(final String textToPrint){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(textToPrint);
            }
        });
    }
    
    private void launchCMS()
    {
        //This method will initializie the CMS library
        
        printOnLog("[+]Initialising CMS Libraries...\n");
        cms = new CMS();
        try {
            cms.init(SaEeDGroup,null,null);//binding CMS object to SaEeD Group
            if(cms.startApp(myPath) == -1){
                printOnLog("[-]Creating CMS object Failed.\n");
                printOnLog("[!]CMS Initilization Failed.\nExiting.");
                System.exit(-1);
            }else{
                printOnLog("[+]CMS object Successfully Created.\n");
            }
            //sharing all files in shared directory
            ContentManager contentManager = null;
            contentManager = cms.getContentManager();
            
            File [] list = myPath.listFiles();
            CheckSumCalc checkSum = new CheckSumCalc();
            
            for(int i=0;i<list.length;i++){
                if(list[i].isFile())
                {//Sharing Files and check sums in network
                    contentManager.share(list[i],checkSum.getFileSum(list[i]));                    
                }
            }                     
            printOnLog("======= Shared Contents =======\n");
            //viewing the shared contents
            Content [] content = cms.getContentManager().getContent();
            //also shows the share contents in log area
            for(int j=0;j< content.length;j++){
                printOnLog("[*]" + content[j].getContentAdvertisement().getName()+  "\tSum: " + 
                        content[j].getContentAdvertisement().getDescription()+"\n");
            }
            printOnLog("[+]All Content are Successfully Shared :-)\n");
            
        } catch (PeerGroupException ex) 
        {
            printOnLog("[!]CMS Initilization Failed.\nExiting.");
            ex.printStackTrace();
            System.exit(-1);
        }catch(IOException e){
            printOnLog("[-]Exception: " + e.getMessage()+ "\n[!]Make sure File: \"Shares.ser\" is Deleted before" +
                    " start the Service.\n");
            printOnLog("[-]Exception: " + e.getMessage());            
        }
        
        printOnLog("[===========================]\n");
        
    }
    public void stopCMS()//this method will stop Content management Service
    {
        printOnLog("[+]Stopping CMS Object.\n");
        cms.stopApp();
        printOnLog("[+]Deleting CMS Content Advertisements File.\n");
        File temp = new File(myPath.getAbsolutePath()+ File.separator +"shares.ser");        
        if(temp.delete())
        {   //also deletes the CMS data file
            printOnLog("[+]File \""+ myPath.getAbsolutePath()+ File.separator + "shares.ser\" successfully deleted.\n");
            printOnLog("[+]File shares.ser successfully deleted.");
        }else{
            printOnLog("[-]File shares.ser Not Found!\n");
        }
    }
    //Listener to shows requested queries
    @Override
    public void queryReceived(String query){
        printOnLog("[Query Received]: " + query);
    }
    
}
