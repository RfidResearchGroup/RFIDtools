package cn.dxl.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 这是将UART转换为TCP服务的一个工具!
 */
public class TCPCom {

    /**
     * 我们开启了套接字服务之后，
     * 在有相应的socket发送数据过来时
     * 会进行链接!
     */
    public interface TcpDataListener {
        void onDataTran(byte[] data);
    }

    // 存放的是所有的客户端!
    private List<Socket> allClient = new ArrayList<>();
    // 客户端的数据来到时的回调!
    private TcpDataListener listener;
    // 服务
    private ServerSocket serverSocket;
    // 处理子线程的传输操作!
    private ExecutorService service = Executors.newFixedThreadPool(10);

    /**
     * 客户端回应的数据的回调!
     *
     * @return 当前被设置的回调!
     */
    public TcpDataListener getListener() {
        return listener;
    }

    /**
     * 设置当前的客户端数据的处理回调!
     *
     * @param listener 数据处理回调!
     */
    public void setListener(TcpDataListener listener) {
        this.listener = listener;
    }

    /**
     * 根据端口开始一个转发服务!
     */
    public void start(int port) throws IOException {
        if (serverSocket != null && !serverSocket.isClosed())
            serverSocket = new ServerSocket(port);
        new Thread() {
            @Override
            public void run() {
                while (isServerRunning()) {
                    try {
                        final Socket socket = serverSocket.accept();
                        // 缓存到全局!
                        if (!allClient.contains(socket)) allClient.add(socket);
                        // 直接创建子线程进行数据读取操作!
                        new Thread() {
                            byte[] buffer = new byte[1024];

                            @Override
                            public void run() {
                                while (socket.isConnected()) {
                                    // 读取数据!
                                    try {
                                        InputStream is = socket.getInputStream();
                                        int len = is.read(buffer);
                                        if (len > 0 && listener != null) {
                                            // 回调通知处理来自TCP的数据!
                                            listener.onDataTran(Arrays.copyOf(buffer, len));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 停止当前的转发服务!
     */
    public void stop() {
        if (isServerRunning()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isServerRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * 向TCP端的所有客户端发送数据
     * 一般是数据从外部UART来，发往所有链接的子Socket的
     */
    public void send(final byte[] data) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                Iterator<Socket> socketIterator = allClient.iterator();
                while (socketIterator.hasNext()) {
                    Socket socket = socketIterator.next();
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        socketIterator.remove();
                    }
                }
            }
        });
    }
}
