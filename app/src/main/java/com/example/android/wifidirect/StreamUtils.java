package com.example.android.wifidirect;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by micha on 5/30/2016.
 */
public class StreamUtils {
    public static final String TAG = StreamUtils.class.getSimpleName();

    public static void sendBytes(byte[] myByteArray, OutputStream outputStream) throws IOException {
        sendBytes(myByteArray, outputStream, 0, myByteArray.length);
    }

    public static void sendBytes(byte[] myByteArray, OutputStream out, int start, int len) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        // Other checks if needed.

        // May be better to save the streams in the support class;
        // just like the socket variable.
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        // Again, probably better to store these objects references in the support class
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        Log.d(TAG, "Length of data is :" + len);
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }
}
