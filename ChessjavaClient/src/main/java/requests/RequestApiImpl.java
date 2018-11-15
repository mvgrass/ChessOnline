package requests;

import Model.Account;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maxim on 16.10.18.
 */
public class RequestApiImpl implements RequestAPI {

    private Logger logger = Logger.getLogger(RequestApiImpl.class);

    private CloseableHttpClient client;

    private String host;

    private BasicCookieStore cookieStore;

    private static RequestApiImpl INSTANCE = null;

    public static RequestApiImpl getInstance(){
        if(INSTANCE == null)
            INSTANCE = new RequestApiImpl();

        return INSTANCE;
    }

    private RequestApiImpl(){
        cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    public void setHost(String host){
        this.host = host;
    }


    @Override
    public void signIn(String login, String password) throws RequestException, WrongParameterException {
        HttpPost post = new HttpPost("http://"+host+"/api/account/signIn");

        List<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("login", login));
        params.add(new BasicNameValuePair("password", password));


        CloseableHttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(params));


            response = client.execute(post);

             if(response.getStatusLine().getStatusCode() == 400)
                 throw new WrongParameterException();
             else if(response.getStatusLine().getStatusCode() != 200)
                 throw new RequestException();

             logger.info("mvgrass-"+login+" signed in");
        }catch (IOException exc){
            throw  new RequestException();
        }
        finally {
            try {
                if (response != null)
                    response.close();
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void signUp(String login, String password) throws RequestException, WrongParameterException {
        HttpPost post = new HttpPost("http://"+host+"/api/account/signUp");

        List<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("login", login));
        params.add(new BasicNameValuePair("password", password));

        CloseableHttpResponse response= null;

        try {
            post.setEntity(new UrlEncodedFormEntity(params));

            response = client.execute(post);


            if(response.getStatusLine().getStatusCode() == 400)
                throw new WrongParameterException();
            else if(response.getStatusLine().getStatusCode() != 200)
                throw new RequestException();

            logger.info("mvgrass-"+login+" signed Up");
        }catch (IOException exc){
            throw  new RequestException();
        }finally {
            try {
                if (response != null)
                    response.close();
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }

    @Override
    public Account getMyAccountInfo() throws RequestException, UnauthorizedException {
        HttpGet get = new HttpGet("http://"+host+"/api/account/getMyAccountInfo");

        try {
            HttpResponse response = null;

            response = client.execute(get);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(response.getEntity().getContent());
            doc.getDocumentElement().normalize();

            Account acc = new Account();

            acc.setLogin(doc.getElementsByTagName("login").item(0).getTextContent());

            List<String> friends = acc.getFriends();
            NodeList childs = doc.getElementsByTagName("friends").item(0).getChildNodes();
            for(int i = 0; i<childs.getLength(); i++){
                if(!"".equals(childs.item(i).getTextContent()))
                    friends.add(childs.item(i).getTextContent());
            }

            List<String> inRequests = acc.getInRequests();
            childs = doc.getElementsByTagName("inRequests").item(0).getChildNodes();
            for(int i = 0; i<childs.getLength(); i++){
                if(!"".equals(childs.item(i).getTextContent()))
                    inRequests.add(childs.item(i).getTextContent());
            }

            List<String> outRequests = acc.getOutRequests();
            childs = doc.getElementsByTagName("outRequests").item(0).getChildNodes();
            for(int i = 0; i<childs.getLength(); i++){
                if(!"".equals(childs.item(i).getTextContent()))
                    outRequests.add(childs.item(i).getTextContent());
            }


            return acc;

        }catch (IOException|ParserConfigurationException|SAXException exc) {
            exc.printStackTrace();
            throw new RequestException();
        }
    }

    @Override
    public String getToken(){
        String token = null;

        for(Cookie cookie : cookieStore.getCookies()){
            if("Access_token".equals(cookie.getName())){
                token = cookie.getValue();
                break;
            }
        }

        return token;
    }

    @Override
    public void addFriend(String name) throws RequestException, WrongParameterException, UnauthorizedException {
        HttpPost post = new HttpPost("http://"+host+"/api/account/addFriend/"+name);

        CloseableHttpResponse response= null;

        try {

            response = client.execute(post);

            if(response.getStatusLine().getStatusCode() == 400)
                throw new WrongParameterException();
            else if(response.getStatusLine().getStatusCode() == 401)
                throw new UnauthorizedException();
            else if(response.getStatusLine().getStatusCode() != 200)
                throw new RequestException();
        }catch (IOException exc){
            throw  new RequestException();
        }finally {
            try {
                if (response != null)
                    response.close();
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }

    }

    @Override
    public void deleteFriend(String name) throws RequestException, WrongParameterException, UnauthorizedException {
        HttpDelete delete = new HttpDelete("http://"+host+"/api/account/deleteFriend/"+name);

        CloseableHttpResponse response= null;

        try {

            response = client.execute(delete);


            if(response.getStatusLine().getStatusCode() == 400)
                throw new WrongParameterException();
            else if(response.getStatusLine().getStatusCode() == 401)
                throw new UnauthorizedException();
            else if(response.getStatusLine().getStatusCode() != 200)
                throw new RequestException();
        }catch (IOException exc){
            throw  new RequestException();
        }finally {
            try {
                if (response != null)
                    response.close();
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void deleteFriendRequest(String name) throws RequestException, WrongParameterException, UnauthorizedException {
        HttpDelete delete = new HttpDelete("http://"+host+"/api/account/deleteFriendRequest/"+name);

        CloseableHttpResponse response= null;

        try {

            response = client.execute(delete);

            if(response.getStatusLine().getStatusCode() == 400)
                throw new WrongParameterException();
            else if(response.getStatusLine().getStatusCode() == 401)
                throw new UnauthorizedException();
            else if(response.getStatusLine().getStatusCode() != 200)
                throw new RequestException();
        }catch (IOException exc){
            throw  new RequestException();
        }finally {
            try {
                if (response != null)
                    response.close();
            }catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }

    @Override
    public String getHost(){
        return host;
    }
}
