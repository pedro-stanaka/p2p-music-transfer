package p2pfileshare;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;

//This class hooks the Listener Thread to Application and fined as many peers as possible in 
// SaEeD Group
public class PeersListing extends Thread implements Runnable, DiscoveryListener {
    //Class variables

    private PeerGroup SaEeDGroup = null;
    private DiscoveryService myDiscoveryService = null;
    private JTextArea log = null;
    private JList list = null;
    public boolean endOfSearch = false;
    DefaultListModel listModel = new DefaultListModel();
    /**
     * Creates a new instance of PeersListing
     */
    public PeersListing(PeerGroup group, JTextArea log, JList list) {
        this.SaEeDGroup = group;
        this.log = log;
        this.list = list;
        myDiscoveryService = SaEeDGroup.getDiscoveryService();
    }

    private void printOnLog(final String toPrint){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }
    
    
    @Override
    public void run() {   //this method will start this Thread
        printOnLog("[+]Start Searching for Peers.\n");
        while (true) {
            //Terminating this Thread as requested
            if (checkLastTime()) {
                break;
            }
            try {
                myDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 5, this);
                listModel.clear();
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                printOnLog("[-]Exception in Searching for Peers process!\n");
                Logger.getLogger(PeersListing.class.getName()).log(Level.SEVERE, null, ex);
            }




        }
        printOnLog("[-]Searching for Peers Stopped.\n");
    }

    private boolean checkLastTime()//causing the Thread to be terminated as requested
    {
        if (endOfSearch == true) {
            return true;
        }
        return false;
    }

    public void setEndOfSearch(boolean value) //Accessor to make an end for Thread
    {
        this.endOfSearch = value;
    }

    private void updatePeerList(ArrayList myList) //Updating Peer list
    {
        list.setListData(myList.toArray());
    }
    //Listener for monitoring Peers in SaEeD's Group

    @Override
    public void discoveryEvent(DiscoveryEvent event) {
        DiscoveryResponseMsg res = event.getResponse();
        String name = "unknown";
        boolean isInList = false;
        PeerAdvertisement peerAdv = res.getPeerAdvertisement();
        if (peerAdv != null) {
            name = peerAdv.getName();
        }
        PeerAdvertisement myAdv = null;
        Enumeration en = res.getAdvertisements();
        ArrayList<String> peerList = new ArrayList<>();
        //Assigning new Peers to Vector and show them
        if (en != null) {
            while (en.hasMoreElements()) {
                myAdv = (PeerAdvertisement) en.nextElement();
                peerList.add(myAdv.getName());
            }

            updatePeerList(peerList);
        }
    }
}
