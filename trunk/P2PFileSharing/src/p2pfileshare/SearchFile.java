package p2pfileshare;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.jxta.peergroup.PeerGroup;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.client.CachedListContentRequest;

//This class will search for specifed contents through SaEeD Group
public class SearchFile extends Thread {
    //Defining Class Variables

    private JTextArea log = null;
    private PeerGroup SaEeDGroup = null;
    private String searchValue = null;
    protected ListRequestor reqestor = null;
    private JTable table = null;
    public static ContentAdvertisement[] contents = null;
    private boolean running = true;

    public SearchFile(PeerGroup group, String searchKey, JTextArea log, JTable table) {
        this.SaEeDGroup = group;
        this.searchValue = searchKey;
        this.log = log;
        this.table = table;

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
    public void run() //cause this thread to execute as long as needed to find 
    {                 // the Contents  
        while (true) {
            if (running == false) {
                break;
            }
            reqestor = new ListRequestor(SaEeDGroup, searchValue, log, table);
            reqestor.activateRequest();

            try {
                Thread.sleep(8 * 1000); //Time out for each search through network
            } catch (InterruptedException ie) {
                stopThread();
            }
        }
        printOnLog("[-]Searching for content is finished.\n");
    }

    public void stopThread() //This method will stop search Process
    {
        running = false;
        if (reqestor != null) {
            reqestor.cancel();
        }

    }

    public void killThread() //This method will Terminate the Search Thread
    {
        printOnLog("[-]Searching Thread is stopping.\n");
        running = false;
    }

    public ContentAdvertisement[] getContentAdvs() //Accessor to show found contents
    {
        return reqestor.searchResult;
    }
}
//inner class for search
class ListRequestor extends CachedListContentRequest {

    public static ContentAdvertisement[] searchResult = null;
    private JTextArea log = null;
    private JTable table = null;

    public ListRequestor(PeerGroup SaEeDGroup, String SubStr, JTextArea log, JTable table) {
        super(SaEeDGroup, SubStr);
        this.log = log;
        this.table = table;
    }

    @Override
    public void notifyMoreResults() //this method will notify user when new contents are found
    {
        printOnLog("[+]Searching for More Contents.\n");
        searchResult = getResults();
        //showing the results
        String[] titles = {"File Name", "Size Bytes", "Check Sum (CRC-32)"};
        //add new contents to Search table
        DefaultTableModel TableModel1 = new DefaultTableModel(titles, searchResult.length);
        table.setModel(TableModel1);

        for (int i = 0; i < searchResult.length; i++) {
            printOnLog("[*]Found: " + searchResult[i].getName() + "\n"
                    + "Size: " + searchResult[i].getLength() + " Bytes\n");
            table.setValueAt(searchResult[i].getName(), i, 0);
            table.setValueAt(searchResult[i].getLength(), i, 1);
            table.setValueAt(searchResult[i].getDescription(), i, 2);

        }
    }

    public ContentAdvertisement[] getContentAdvs()//acessor to return contents
    {
        return searchResult;
    }

    private void printOnLog(final String toPrint) {
         SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }
}
