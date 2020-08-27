package me.hxert.fastcopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Hxert
 * @data Aug 28, 2020
 */
public class Main {

    public static void main(String[] args) {
        FastCopy fc = new FastCopy();
        long time = System.currentTimeMillis();
        //fc.copy("C:/demo.txt", "C:/demo_copy.txt");
        System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
    }
    
    
}

class FastCopy{
    
    public boolean copy(String sourceFile, String NewFile){
        try {
            return this.copy(new FileInputStream(sourceFile), new FileOutputStream(NewFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FastCopy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean copy(File sourceFile, File NewFile){
        try {
            return this.copy(new FileInputStream(sourceFile), new FileOutputStream(NewFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FastCopy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    
    public boolean copy(FileInputStream fsInput, FileOutputStream fsOutput){
        FileChannel fcInput;
        FileChannel fcOutput;
        
        fcInput = fsInput.getChannel();
        fcOutput = fsOutput.getChannel();
        
        long position = 0L;
        long length;
        long count;
        
        try {
            //get the file size.
            //获取文件大小
            length = fcInput.size();
        } catch (IOException ex) {
            Logger.getLogger(FastCopy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        
        try{
            while(length > 0){
                //transfers bytes from this channel's file to the given writable byte channel.
                //尝试将字节从源通道传输到目标通道
                //count: The number of bytes, possibly zero, that were actually transferred
                //count: 成功(实际)传输的字节数
                count = fcInput.transferTo(position, length, fcOutput);
                if(count > 0){
                    position = position + count;
                    length = length - count;
                }
            } 
        }catch(IllegalArgumentException mIllegalArgumentException){
            //the preconditions on the parameters do not hold.
            return false;
        }catch(NonWritableChannelException mNonWritableChannelException){
            //this channel was not opened for reading.
            //通道未打开读取
            return false;
        }catch(NonReadableChannelException mNonReadableChannelException){
            //the target channel was not opened for writing.
            //通道未打开写入
            return false;
        }catch(ClosedChannelException mClosedChannelException){
            //either this channel or the target channel is closed.
            //源通道或目标通道已关闭
            return false;
        }catch(IOException ex){
            Logger.getLogger(FastCopy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
        
    }
}
