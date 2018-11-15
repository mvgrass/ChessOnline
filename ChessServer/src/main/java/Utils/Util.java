package Utils;

import model.Account.Account;
import model.Account.GameManager;
import model.Game.*;
import org.apache.commons.codec.digest.Sha2Crypt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import java.util.List;
import java.util.Random;

/**
 * Created by maxim on 05.10.18.
 */
public class Util {

    public static String generateSalt(){
        StringBuilder salt = new StringBuilder();
        Random rand = new Random();
        for(int i = 0; i<16; ++i) {
            if(rand.nextInt()%2==0)
                salt.append((char)('a'+rand.nextInt(26)));
            else
                salt.append((char)('a'+rand.nextInt(26)));
        }

        return salt.toString();
    }

    public static String hash(String string) {

        return Sha2Crypt.sha256Crypt(string.getBytes());
    }

    public static String hash(String string, String salt) {

        return Sha2Crypt.sha256Crypt(string.getBytes(), "$5$"+salt);
    }

    public static String generateAccessToken(String login, String date){
        return Sha2Crypt.sha256Crypt(login.getBytes()) + Sha2Crypt.sha256Crypt(date.getBytes());
    }

    public static String getInfoAboutMyAccountXML(Account account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Account");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account.getLogin());

            root.appendChild(login);

            Element friends = doc.createElement("friends");
            for(String friend : account.getFriends()){
                Element friendNode = doc.createElement("friend");
                friendNode.setTextContent(friend);
                friends.appendChild(friendNode);
            }

            root.appendChild(friends);

            Element inRequests = doc.createElement("inRequests");
            for(String request : account.getInRequest()){
                Element requestNode = doc.createElement("request");
                requestNode.setTextContent(request);
                inRequests.appendChild(requestNode);
            }

            root.appendChild(inRequests);

            Element outRequests = doc.createElement("outRequests");
            for(String request : account.getOutRequest()){
                Element requestNode = doc.createElement("request");
                requestNode.setTextContent(request);
                outRequests.appendChild(requestNode);
            }

            root.appendChild(outRequests);


            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getInfoAboutAccountXML(Account account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Account");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account.getLogin());

            root.appendChild(login);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getOnlineNotificationXML(String account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Online");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account);

            root.appendChild(login);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getOfflineNotificationXML(String account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Offline");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account);

            root.appendChild(login);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getAddingNotificationXML(String account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Adding");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account);

            root.appendChild(login);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getRemovingNotificationXML(String account){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Removing");

            doc.appendChild(root);

            Element login = doc.createElement("login");
            login.setTextContent(account);

            root.appendChild(login);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getMessageNotificationXML(String from, String message){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Message");

            Element fromNode = doc.createElement("from");
            fromNode.setTextContent(from);

            Element text = doc.createElement("text");
            text.setTextContent(message);

            root.appendChild(fromNode);
            root.appendChild(text);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getInviteNotificationXML(String from){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("Invite");

            Element fromNode = doc.createElement("from");
            fromNode.setTextContent(from);

            root.appendChild(fromNode);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getGameStartedNotificationXML(String against, String gameId,String color, Long duartion){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("GameStarted");

            Element Againts = doc.createElement("against");
            Againts.setTextContent(against);

            Element GameId = doc.createElement("gameId");
            GameId.setTextContent(gameId);

            Element Color = doc.createElement("color");
            Color.setTextContent(color);

            Element Duration = doc.createElement("duration");
            Duration.setTextContent(String.valueOf(duartion));

            root.appendChild(Againts);
            root.appendChild(GameId);
            root.appendChild(Color);
            root.appendChild(Duration);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getInviteTimedOutNotificationXML(String from){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("InviteTimeOut");

            Element fromNode = doc.createElement("from");
            fromNode.setTextContent(from);

            root.appendChild(fromNode);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getGameStateXML(String gameId, GameManager game){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("GameState");

            Element Turn = doc.createElement("Turn");
            Turn.setTextContent(game.getTurn() == Piece.COLOR.LIGHT?"Light":"Dark");

            Element GameId = doc.createElement("GameId");
            GameId.setTextContent(gameId);

            Element Board = doc.createElement("Board");
            List<List<Piece>> board = game.getBoard();
            for(int i = 7;i>=0;i--){
                for(int j = 0; j<8; j++){
                    Element piece = doc.createElement("Piece");

                    Piece p = board.get(i).get(j);

                    if(p == null){
                        piece.setTextContent("");
                    }else if(p.getClass() == Pawn.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lP");
                        else
                            piece.setTextContent("dP");
                    }else if(p.getClass() == Rogue.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lR");
                        else
                            piece.setTextContent("dR");
                    }else if(p.getClass() == Knight.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lKn");
                        else
                            piece.setTextContent("dKn");
                    }else if(p.getClass() == Bishop.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lB");
                        else
                            piece.setTextContent("dB");
                    }else if(p.getClass() == Queen.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lQ");
                        else
                            piece.setTextContent("dQ");
                    }else if(p.getClass() == King.class){
                        if(p.getColor() == Piece.COLOR.LIGHT)
                            piece.setTextContent("lK");
                        else
                            piece.setTextContent("dK");
                    }

                    Board.appendChild(piece);
                }
            }

            Element lastMove = doc.createElement("LastMove");
            lastMove.setTextContent(game.getLastMoveString());

            Element LightTime = doc.createElement("LightTimer");
            LightTime.setTextContent(String.valueOf(game.getLightsTimeLeft()));

            Element DarkTime = doc.createElement("DarkTimer");
            DarkTime.setTextContent(String.valueOf(game.getDarksTimeLeft()));

            root.appendChild(GameId);
            root.appendChild(Turn);
            root.appendChild(Board);
            root.appendChild(lastMove);
            root.appendChild(LightTime);
            root.appendChild(DarkTime);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getEndOfGameXML(String gameId, String winner, String reason){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement("GameEnd");

            Element GameId = doc.createElement("GameId");
            GameId.setTextContent(gameId);

            Element Winner = doc.createElement("Winner");
            Winner.setTextContent(winner);

            Element Reason = doc.createElement("Reason");
            Reason.setTextContent(reason);

            root.appendChild(GameId);
            root.appendChild(Winner);
            root.appendChild(Reason);

            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();

        }catch (ParserConfigurationException | TransformerException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getMessageTo(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            NodeList elements = doc.getElementsByTagName("To");
            if(elements!=null)
                return elements.item(0).getTextContent();


        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getMessageFrom(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            NodeList elements = doc.getElementsByTagName("From");
            if(elements!=null)
                return elements.item(0).getTextContent();


        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getGameId(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            NodeList elements = doc.getElementsByTagName("GameId");
            if(elements!=null)
                return elements.item(0).getTextContent();


        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getInviteDuration(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            NodeList elements = doc.getElementsByTagName("Duration");
            if(elements!=null)
                return elements.item(0).getTextContent();


        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }
        return null;
    }

    public static String getTextOfMessage(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            if(doc.getElementsByTagName("Message")!=null){
                NodeList elements = doc.getElementsByTagName("Text");
                if(elements!=null)
                    return elements.item(0).getTextContent();
            }

        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getHeaderOfMessage(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            return doc.getChildNodes().item(0).getNodeName();

        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }

        return null;
    }

    public static String getTransform(String message){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(message)));
            doc.getDocumentElement().normalize();

            NodeList Transform = doc.getElementsByTagName("Transform");

            if(Transform.getLength()>0){

                return Transform.item(0).getTextContent();

            }

        }catch (ParserConfigurationException|IOException |SAXException exc){
            exc.printStackTrace();
        }

        return null;
    }
}
