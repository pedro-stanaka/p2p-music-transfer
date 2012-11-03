package p2pfileshare;

import java.io.File;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.jxta.peergroup.PeerGroup;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.client.GetContentRequest;

//This class runs as Thread and start Downloading the File as soon as called
public class DownloadFile extends Thread {

    private PeerGroup SaEeDGroup = null;
    protected GetRemoteFile myDonwloader = null;
    private JTextArea log;

    private void printOnLog(final String toPrint) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }

    public DownloadFile(PeerGroup group, ContentAdvertisement contentAdv, File destination, JTextArea log,
            JProgressBar progress) {
        this.log = log;
        printOnLog("[+]Starting Download Object.\n");
        //inner classes used here for better performance
        myDonwloader = new GetRemoteFile(group, contentAdv, destination, this.log, progress);

    }
}
//inner class which handles download requestes
class GetRemoteFile extends GetContentRequest {

    private JProgressBar progressBar = null;
    private JTextArea log = null;
    private boolean downloadFinished = false;

    public GetRemoteFile(PeerGroup group, ContentAdvertisement contentAdv, File destination, JTextArea log,
            JProgressBar progress) {
        super(group, contentAdv, destination);
        this.progressBar = progress;
        this.log = log;

        printOnLog("[+]Download in Progress.\n");
    }

    @Override
    public void notifyUpdate(int percentage) //this method will notify about download progress
    {
        progressBar.setValue(percentage);
    }

    @Override
    public void notifyDone()//this method will return message about download process 
    {
        printOnLog("[+]Donwloading Process is sucessfully finished.\n");
    }

    @Override
    public void notifyFailure()//this method will return message if download failed 
    {
        printOnLog("[-] O download falhou devido a uma exceção no sistema! ç.ç ");
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
