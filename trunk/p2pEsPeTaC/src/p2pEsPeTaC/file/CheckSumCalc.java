package p2pEsPeTaC.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

//This Class generates CRC-32 Check sum to make sure that files which transfered 
//are not corrupted
public class CheckSumCalc {

    private long Result = 0;

    /**
     * Creates a new instance of CheckSumCalc
     */
    public CheckSumCalc() {
    }

    public String getFileSum(File filename)//this method will return String of CheckSum file :-)
    {
        //CRC-32  check sum

        CheckedInputStream cis;
        try {
            cis = new CheckedInputStream(new FileInputStream(filename), new Adler32());
            byte[] tempBuf = new byte[512];
            try {
                while (cis.read(tempBuf) >= 0) {
                }
            } catch (IOException ex) {
                
                Logger.getLogger(CheckSumCalc.class.getName()).log(Level.SEVERE, null, ex);
            }
            Result = cis.getChecksum().getValue();
        } catch (FileNotFoundException ex) {
            System.out.println("[-]File not Found :-(");
            Logger.getLogger(CheckSumCalc.class.getName()).log(Level.SEVERE, null, ex);
        }


        return Long.toHexString(Result);

    }
}
