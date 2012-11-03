package p2pfileshare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

//This class will create initialization configuration for class 
//and Sets Peers Share folder
public class FirstTimeCheck extends JFileChooser {

    /**
     * Creates a new instance of FirstTimeCheck
     */
    private boolean firstTime = true;
    public JTextArea log;
    private String SharedPath = "unknown";

    public FirstTimeCheck(JTextArea txt) {
        this.log = txt;
    }
    private void printOnLog(final String toPrint){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                log.append(toPrint);
            }
        });
    }

    public void searchForConfigFile() { //Search for Configuration file
        printOnLog("[+]Searching for Configuration file.\n");
        File configFile = new File("config.ini");
        if (configFile.exists() && configFile.isFile()) {
            printOnLog("[+]Configuration file Found.\n");
            readingConfigFile(configFile);
        } else {
            createConfigFile();
            printOnLog("[-]Configuration file !NOT! Found.\n");
        }
    }

    public void readingConfigFile(File file) //Starts reading Configuration file and finds shared path from it.
    {
        printOnLog("[+]Reading file: " + file.getName() + "\n");

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
            String str, path;
            int index;
            try {
                while ((str = in.readLine()) != null) {
                    if (str.startsWith("SharedFolder")) {
                        index = str.indexOf("=");
                        path = str.substring(index + 1);
                        printOnLog("[+]Shared Path is: " + path + "\n");
                        File temp = new File(path);
                        if (temp.exists()) {
                            printOnLog("[+]Shared Path Exists\n");
                            SharedPath = temp.getAbsolutePath();

                        } else {
                            printOnLog("[-]Path NOT Exists!!!\n");
                            createShareFolder(temp);
                        }
                    }
                }
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(FirstTimeCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FirstTimeCheck.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    public void createShareFolder(File pathname) //if the shared path doesnot exit this method will create it
    {
        printOnLog("[+]Creating Share Folder...\n");
        if (pathname.mkdir()) {
            printOnLog("[+]Shared Folder Successfully Created.\n");
            SharedPath = pathname.getAbsolutePath();
        }

    }

    public void createConfigFile() {
        File config = new File("config.ini");
        //Default path
        String Path = new String();
        printOnLog("**** Please Select Your Share Folder ****\n");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            printOnLog("[+]Selected Path is: " + chooser.getSelectedFile().getAbsolutePath() + "\n");
            Path = chooser.getSelectedFile().getAbsolutePath();
        }
        SharedPath = Path;
        boolean success;
        try {
            success = config.createNewFile();
            FileOutputStream out = new FileOutputStream(config);
            if (success) {
                printOnLog("[+]Config.ini file Successfully Created.\n");
                printOnLog("[+]Writing Data into Configuration File\n");
                String finalPath = "SharedFolder=" + Path;
                out.write(finalPath.getBytes());
                out.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(FirstTimeCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        readingConfigFile(config);
    }

    public String getSharedPath() {
        return SharedPath;
    }

    public boolean isFirstTime() //Search for initialization file, if not found assumes that it is
    {                           // the first time that program is being executed and will create 
        //Initialization File
        File configFile = new File("config.ini");
        if (configFile.exists() && configFile.isFile()) {
            firstTime = false;
        } else {
            firstTime = true;
        }
        return firstTime;
    }
}
