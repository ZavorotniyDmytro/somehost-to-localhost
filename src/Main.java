
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
//        Vector<Integer> ports = new Vector<>(List.of(26500, 8080, 8081, 8082));
//        Vector<String> hosts = new Vector<>(List.of("172.17.201.28", "0.0.0.0", "172.19.0.5", "127.0.0.1"));

        //test_connection(hosts, ports);


        String host = "172.17.201.28";
        int[] ports = {8081, 8082, 8084, 26500, 18080}; // Порты, которые нужно обрабатывать ProxyServer

        for (int port : ports) {
            startProxyServer(host, port);
        }

    }
    static void testConnection(Vector<String> hosts, Vector<Integer> ports){
        for (String host : hosts){
            for (Integer port : ports) {
                try {
                    System.out.println("TRYING - " + host + ':' + port);
                    Socket socket = new Socket(host, port);
                    if (socket.isConnected()){
                        System.out.println("CONNECTED\n");
                    }
                    socket.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage()+'\n');
                }
            }
        }
    }

    private static void startProxyServer(String host, int port) {
        Thread thread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Прокси-сервер работает на localhost:" + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    Thread requestHandlerThread = new Thread(() -> {
                        try {
                            handleClientRequest(clientSocket, host, port);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    requestHandlerThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private static void handleClientRequest(Socket clientSocket, String targetHost, int targetPort) throws IOException {
        InputStream clientInput = clientSocket.getInputStream();
        OutputStream clientOutput = clientSocket.getOutputStream();

        // Указываем хост и порт назначения
        Socket targetSocket = new Socket(targetHost, targetPort);

        // Перенаправляем запрос от клиента на целевой сервер
        Thread thread1 = new Thread(() -> {
            try {
                forwardData(clientInput, targetSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Перенаправляем ответ от целевого сервера клиенту
        Thread thread2 = new Thread(() -> {
            try {
                forwardData(targetSocket.getInputStream(), clientOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();
    }

    private static void forwardData(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        output.flush();
    }
}