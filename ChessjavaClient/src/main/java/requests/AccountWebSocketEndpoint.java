package requests;

import Controller.MainWindowController;
import javafx.application.Platform;
import org.apache.log4j.Logger;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.websocket.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by maxim on 20.10.18.
 */


public class AccountWebSocketEndpoint extends Endpoint
        implements MessageHandler.Whole<String>
{

    private Logger logger = Logger.getLogger(AccountWebSocketEndpoint.class);

    private Session session;

    private MainWindowController controller;

    private Pinger pinger;

    private Long lastPong;

    public AccountWebSocketEndpoint(String host, String token, MainWindowController controller) throws URISyntaxException, DeploymentException,
            IOException
    {
        super();
        this.controller = controller;
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator(){
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Access_token", Arrays.asList(token));
            }
        };
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(configurator).build();
        ClientManager clientManager = ClientManager.createClient(JdkClientContainer.class.getName());
        clientManager.getProperties().put(ClientProperties.HANDSHAKE_TIMEOUT, 5000);

        clientManager.connectToServer(this,
                clientEndpointConfig,
                new URI("ws://" + host + "/onlineService"));
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        session.addMessageHandler(this);
        session.addMessageHandler(new MessageHandler.Whole<PongMessage>() {
            @Override
            public void onMessage(PongMessage pong) {
                lastPong = System.currentTimeMillis();
            }
        });
        lastPong = System.currentTimeMillis();
        pinger = new Pinger(this);
        pinger.setDaemon(true);
        pinger.start();
        Platform.runLater(()->{
            controller.statusLabel.setText("Connected");
        });

        logger.info("mvgrass-Connected to online Service");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        this.pinger.interrupt();
        Platform.runLater(()->{
            controller.statusLabel.setText("Disconnected");
            if(closeReason.getCloseCode()!=CloseReason.CloseCodes.NORMAL_CLOSURE)
                controller.reconnectToServer();
        });

        logger.info("mvgrass-Disconnected from online Service");
    }

    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);
        logger.debug("mvgrass-Socket Error", thr);
    }

    public void close(){
        try {
            session.close();
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }

    public void sendMessage(String to, String message){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Message");

            doc.appendChild(root);

            Element To = doc.createElement("To");
            To.setTextContent(to);

            root.appendChild(To);

            Element text = doc.createElement("Text");
            text.setTextContent(message);
            root.appendChild(text);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    public void sendInvite(String to, String duration){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Invite");

            doc.appendChild(root);

            Element To = doc.createElement("To");
            To.setTextContent(to);

            root.appendChild(To);

            Element Dur = doc.createElement("Duration");
            Dur.setTextContent(duration);
            root.appendChild(Dur);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    public void acceptInvite(String from){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("AcceptInvite");

            doc.appendChild(root);

            Element From = doc.createElement("From");
            From.setTextContent(from);

            root.appendChild(From);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    public void sendMove(String gameId, String start, String end, String transform){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Move");

            doc.appendChild(root);

            Element GameId = doc.createElement("GameId");
            GameId.setTextContent(gameId);

            Element Start = doc.createElement("From");
            Start.setTextContent(start);

            Element End = doc.createElement("To");
            End.setTextContent(end);

            Element Transform = doc.createElement("Transform");
            Transform.setTextContent(transform);

            root.appendChild(GameId);
            root.appendChild(Start);
            root.appendChild(End);
            root.appendChild(Transform);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    public void sendGiveUp(String gameId){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("GiveUp");

            doc.appendChild(root);

            Element GameId = doc.createElement("GameId");
            GameId.setTextContent(gameId);


            root.appendChild(GameId);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    public void sendGetUpdate(String gameId){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("GetUpdate");

            doc.appendChild(root);

            Element GameId = doc.createElement("GameId");
            GameId.setTextContent(gameId);


            root.appendChild(GameId);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            String messageXML = writer.getBuffer().toString();

            new Thread(()->{
                session.getAsyncRemote().sendText(messageXML);
            }).start();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }
    }

    @Override
    public void onMessage(String s) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(s)));
            doc.getDocumentElement().normalize();

            String type = doc.getChildNodes().item(0).getNodeName();
            String content;
            String from;
            switch (type){
                case "Online":
                    content = doc.getElementsByTagName("login").item(0).getTextContent();
                    Platform.runLater(()->{controller.onFriendOnline(content);});
                    break;
                case "Offline":
                    content = doc.getElementsByTagName("login").item(0).getTextContent();
                    Platform.runLater(()->{controller.onFriendOffline(content);});
                    break;
                case "Adding":
                    content = doc.getElementsByTagName("login").item(0).getTextContent();
                    Platform.runLater(()->{controller.onAddingFriend(content);});
                    break;
                case "Removing":
                    content = doc.getElementsByTagName("login").item(0).getTextContent();
                    Platform.runLater(()->{controller.onRemovingFriend(content);});
                    break;
                case "Message":
                    content = doc.getElementsByTagName("text").item(0).getTextContent();
                    from = doc.getElementsByTagName("from").item(0).getTextContent();
                    Platform.runLater(()->{controller.onMessage(from, content);});
                    break;
                case "Invite":
                    from = doc.getElementsByTagName("from").item(0).getTextContent();
                    Platform.runLater(()->{controller.onInvite(from);});
                    break;
                case "InviteTimeOut":
                    from = doc.getElementsByTagName("from").item(0).getTextContent();
                    Platform.runLater(()->{controller.onRemoveInvite(from);});
                    break;

                case "GameStarted":{
                    String against = doc.getElementsByTagName("against").item(0).getTextContent();
                    String gameId = doc.getElementsByTagName("gameId").item(0).getTextContent();
                    String color = doc.getElementsByTagName("color").item(0).getTextContent();
                    Long duration = Long.valueOf(doc.getElementsByTagName("duration").item(0).getTextContent());

                    Platform.runLater(()->{controller.addNewTab(against, color, gameId, duration);});

                    break;
                }

                case "GameState":{
                    String gameId = doc.getElementsByTagName("GameId").item(0).getTextContent();
                    String turn  = doc.getElementsByTagName("Turn").item(0).getTextContent();
                    NodeList pieces = doc.getElementsByTagName("Board").item(0).getChildNodes();

                    List<String> board = new LinkedList<>();
                    for(int i = 0; i< pieces.getLength(); i++){
                        board.add(pieces.item(i).getTextContent());
                    }

                    String lastMove = doc.getElementsByTagName("LastMove").item(0).getTextContent();

                    Long lightTimer = Long.valueOf(doc.getElementsByTagName("LightTimer").item(0).getTextContent());
                    Long darkTimer = Long.valueOf(doc.getElementsByTagName("DarkTimer").item(0).getTextContent());

                    Platform.runLater(()->{
                        controller.updateGame(gameId, turn, board, lastMove, lightTimer, darkTimer);
                    });
                    break;
                }

                case "GameEnd":{
                    String gameId = doc.getElementsByTagName("GameId").item(0).getTextContent();
                    String winner = doc.getElementsByTagName("Winner").item(0).getTextContent();
                    String reason = doc.getElementsByTagName("Reason").item(0).getTextContent();

                    Platform.runLater(()-> {
                        controller.endOfGame(gameId, winner, reason);
                    });
                    break;
                }
            }

        }catch (ParserConfigurationException|IOException|SAXException exc){
            exc.printStackTrace();
        }
    }

    class Pinger extends Thread{
        AccountWebSocketEndpoint endpoint;

        public Pinger(AccountWebSocketEndpoint endpoint){
            this.endpoint = endpoint;
        }

        public void run(){
            try {
                while (!isInterrupted()) {
                    Thread.sleep(15000);
                    if (System.currentTimeMillis() - endpoint.lastPong > 30000) {
                        endpoint = null;
                        logger.warn("mvgrass-Server is not answering ping");
                        Platform.runLater(() -> {
                            controller.statusLabel.setText("Disconnected");
                            controller.reconnectToServer();
                        });
                        break;
                    } else {
                        endpoint.session.getAsyncRemote().sendPing(ByteBuffer.wrap("Ping".getBytes()));
                        logger.info("mvgrass-Server was pinged");

                    }
                }
            }catch (IOException exc){
                exc.printStackTrace();
            }
            catch (InterruptedException exc){

            }
        }
    }
}
