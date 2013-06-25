package study;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * interruptをオーバーライドして標準的でないキャンセルをカプセル化する
 *
 * List 7-12
 */
public class ReaderThread extends Thread {
    private static final int BUFZ = 64;
    private final Socket socket;
    private final InputStream in;

    public ReaderThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
    }

    @Override
    public void interrupt() {
        try {
            socket.close();
        } catch (IOException e) {
            // 無視する
            e.printStackTrace();
        } finally {
            super.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[BUFZ];
            while(true) {
                int count = in.read(buf);
                if(count < 0) {
                    break;
                } else if(count > 0) {
                    processBuffer(buf, count);
                }
            }
        } catch (IOException e) {
            /* スレッドを終わらせる */
            e.printStackTrace();
        }
    }

    private void processBuffer(byte[] buf, int count) {
        // FIXME
    }
}
