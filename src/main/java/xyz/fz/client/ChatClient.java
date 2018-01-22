package xyz.fz.client;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import xyz.fz.client.util.BaseProperties;
import xyz.fz.client.util.RSAUtil;

public class ChatClient {

    private static String pubKey = BaseProperties.get("chat.pub.key");

    private static Socket socket = null;

    private static ChatMessageHandler chatMessageHandler = new ChatMessageHandler() {
        @Override
        public void handle(String message) {
            System.out.println(message);
        }
    };

    public static void setChatMessageHandler(ChatMessageHandler chatMessageHandler) {
        ChatClient.chatMessageHandler = chatMessageHandler;
    }

    private static Socket getSocket() throws Exception {
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.reconnectionAttempts = 2;
        options.reconnectionDelay = 1000;
        options.timeout = 500;

        String token = DateTime.now().toString("yyyyMMdd");
        options.query = "token=" + Base64.encodeBase64URLSafeString(RSAUtil.encryptByPublicKey(token.getBytes("utf-8"), pubKey));

        final Socket socket = IO.socket(BaseProperties.get("chat.server.url"), options);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            public void call(Object... args) {
                System.out.println("socket connected..." + socket.id() + "#" + socket.toString());
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            public void call(Object... args) {
                System.out.println("socket disconnected..." + socket.toString());
            }
        });

        socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            public void call(Object... args) {
                for (Object message : args) {
                    String deMessage = null;
                    try {
                        deMessage = new String(RSAUtil.decryptByPublicKey(Base64.decodeBase64(message.toString()), pubKey), "utf-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (StringUtils.isNotBlank(deMessage)) {
                        chatMessageHandler.handle(deMessage);
                    }
                }
            }
        });

        socket.connect();

        return socket;
    }

    public static void send(String message) throws Exception {
        if (socket == null) {
            socket = getSocket();
        }
        socket.send(message);
    }

    public static void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    public interface ChatMessageHandler {
        void handle(String message);
    }

    public static void main(String[] args) throws Exception {
        send("hello@" + DateTime.now());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                disconnect();
//            }
//        }).start();
    }
}
