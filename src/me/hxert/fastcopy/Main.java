package me.hxert.fastcopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.1
 * @author Hxert
 * @data Aug 29, 2020
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Running");
        FastIO fc = new FastIO();
        long time = System.currentTimeMillis();
        //fc.copy("C:/demo.txt", "C:/demo_copy.txt");
        //System.out.println(fc.readByLine("C:/demo2.txt").get(1));
        System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
    }
    
    
}

class FastIO{
    
    public boolean copy(String sourceFile, String NewFile){
        try {
            return this.copy(new FileInputStream(sourceFile), new FileOutputStream(NewFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean copy(File sourceFile, File NewFile){
        try {
            return this.copy(new FileInputStream(sourceFile), new FileOutputStream(NewFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        try {
            fcInput.close();
            fcOutput.close();
            fsInput.close();
            fsOutput.close();
        } catch (IOException ex) {
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        return true;
    }
    
    public List<String> readByLine(String file){
        try {
            return this.readByLine(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public List<String> readByLine(FileInputStream fsInput){
        //定义单次读入的字节数 用于大文件读取/内存不足情景
        //Define the number of bytes read at a time.
        int limitReadLineSize = 1024*1024*20;
        FileChannel fcInput = fsInput.getChannel();
        //存放返回的结果集 读取超内存大文件时无需使用
        //Returned result set.Don use it when reading big files(Out of memory).
        List<String> resultList = new ArrayList<>();
        //初始化ByteBuffer
        //Initialize ByteBuffer
        ByteBuffer dstRead = ByteBuffer.allocate(limitReadLineSize);
        //存放单行结果 一行大小不应超出内存
        //One line should not exceed the memory size!
        List<Byte> resultCache = new ArrayList<>();
        byte[] byteCache;
        try {
            while(fcInput.read(dstRead) != -1){
                int len = dstRead.position();
                //复位position
                dstRead.flip();
                byte[] results = new byte[len];
                dstRead.get(results);
                dstRead.clear();
                for(int i = 0; i < results.length;i++){
                    /*
                        由于中文字符占用＞1字节(一般UTF-8下为3 GBK为2) 若从中分割将产生乱码
                        此处加入对换行符的识别与前后的处理 拼接完整字节以免乱码
                        
                        Because Chinese uses more than 1 byte.
                        This avoids the appearance of garbled characters.
                        (Also applicable to Japanese and Korean.)
                    */
                    switch(results[i]){
                        case '\n':
                            /* 处理\r\n */
                            /* \r\n */
                            if(resultCache.isEmpty()){
                                continue;
                            }
                            byteCache = new byte[resultCache.size()];
                            for(int o = 0;o < resultCache.size();o++){
                                byteCache[o] = resultCache.get(o);
                            }
                            /* 如果处理超内存大文件 直接将字符串数据做参数调用其他方法 注释下行 */
                            /* Don use it when reading big files. */
                            resultList.add(new String(byteCache,"UTF-8"));
                            resultCache.clear();
                            break;
                        case '\r':
                            byteCache = new byte[resultCache.size()];
                            for(int o = 0;o < resultCache.size();o++){
                                byteCache[o] = resultCache.get(o);
                            }
                            /* 如果处理超内存大文件 直接将字符串数据做参数调用其他方法 注释下行 */
                            /* Don use it when reading big files. */
                            resultList.add(new String(byteCache,"UTF-8"));
                            resultCache.clear();
                            break;
                        default:
                            resultCache.add(results[i]);
                            break;
                    }
                    
                }
            }
            /* 处理最后一行 */
            /* Process the last line. */
            byteCache = new byte[resultCache.size()];
            for(int o = 0;o < resultCache.size();o++){
                byteCache[o] = resultCache.get(o);
            }
            /* 如果处理超内存大文件 直接将字符串数据做参数调用其他方法 注释下行 */
            /* Don use it when reading big files. */
            resultList.add(new String(byteCache,"UTF-8"));
            
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fcInput.close();
            fsInput.close();
        } catch (IOException ex) {
            Logger.getLogger(FastIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* 如果处理超内存大文件 无需返回结果集 */
        /* Need not return when reading big files(Out of memory). */
        return resultList;
    }
}
