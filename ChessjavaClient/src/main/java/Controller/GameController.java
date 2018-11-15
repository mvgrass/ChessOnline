package Controller;

import Model.Pieces.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import requests.AccountWebSocketEndpoint;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by maxim on 03.11.18.
 */
public class GameController {

    class MyPane extends BorderPane {
        public int X;
        public int Y;

        ImageView imgView = new ImageView();

        MyPane(int X, int Y){
            super();
            this.X = X;
            this.Y = Y;

            this.setCenter(imgView);
        }

        public String getCell(){
            StringBuilder builder = new StringBuilder();

            builder.append((char) ((rotated)?'H'-(this.X - 1):'A'+(this.X-1)));
            builder.append((rotated)?Y:9-Y);

            return builder.toString();
        }

        public void setImage(Image img){
            imgView.setImage(img);
        }

        public Image getImage(){
            return imgView.getImage();
        }
    }

    private Object mutex = new Object();

    @FXML
    private GridPane board;

    @FXML
    private Label lightTimerLabel;

    @FXML
    private Label darkTimerLabel;

    @FXML
    private Label winnerLabel;

    @FXML
    private Label reasonLabel;

    @FXML
    private VBox resultVBox;

    @FXML
    private VBox movesLightVBox;

    @FXML
    private VBox movesDarkVBox;

    @FXML
    private ScrollPane LightScrollPane;

    @FXML
    private ScrollPane DarkScrollPane;

    private boolean rotated = false;

    private String gameId;

    private Color turn = Color.LIGHT;

    private AccountWebSocketEndpoint endpoint;

    volatile private Long lightsTimer;

    volatile private Long darksTimer;

    private boolean isEnd = false;

    private int enemyLastMoveX = -1;

    private int enemyLastMoveY = -1;

    public void setTurn(Color turn){
        this.turn = turn;
    }

    public void setEndpoint(AccountWebSocketEndpoint endpoint){
        this.endpoint = endpoint;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    private MainWindowController parent;

    public void setParent(MainWindowController controller){
        parent = controller;
    }

    public void setTime(Long lightsTimer, Long darksTimer){
        this.lightsTimer = lightsTimer;
        this.darksTimer = darksTimer;

        lightTimerLabel.setText(String.format("%02d:%02d",lightsTimer/1000/60,(lightsTimer/1000)%60));
        darkTimerLabel.setText(String.format("%02d:%02d",darksTimer/1000/60,(darksTimer/1000)%60));

    }

    private List<MyPane> cells = new ArrayList<>();


    public void initGame(Long time, boolean rotated){
        LightScrollPane.vvalueProperty().bind(movesLightVBox.heightProperty());
        DarkScrollPane.vvalueProperty().bind(movesDarkVBox.heightProperty());

        this.rotated = rotated;

        for(int i = 1;i<9;i++){
            for (int j = 1; j<9; j++){
                MyPane pane = new MyPane(j, i);
                if((pane.X+pane.Y)%2!=0)
                    pane.setStyle("-fx-background-color: rgb(255,210,96);");
                else
                    pane.setStyle("-fx-background-color: rgb(255,255,255);");

                pane.setOnDragDetected(mouseEvent -> {
                    if(pane.getImage()!=null
                            &&((!rotated&&((Colored)pane.getImage()).getColor()==Color.LIGHT)
                            ||((rotated&&((Colored)pane.getImage()).getColor()==Color.DARK)))){

                        if(enemyLastMoveX>=0&&enemyLastMoveX<8
                                &&enemyLastMoveY>=0&&enemyLastMoveY<8){
                            cells.get(8*enemyLastMoveY+enemyLastMoveX)
                                    .setStyle((enemyLastMoveX+enemyLastMoveY)%2!=0
                                            ?"-fx-background-color: rgb(255,210,96);"
                                            :"-fx-background-color: rgb(255,255,255);");

                            enemyLastMoveX = -1;
                            enemyLastMoveY = -1;
                        }

                        Dragboard db = pane.startDragAndDrop(TransferMode.ANY);

                        ClipboardContent content = new ClipboardContent();
                        content.putImage(pane.getImage());

                        db.setContent(content);

                    }
                });

                pane.setOnDragEntered(dragEvent -> {
                    if(pane.getImage()!=null
                            &&((!rotated&&((Colored)pane.getImage()).getColor()==Color.DARK)
                            ||((rotated&&((Colored)pane.getImage()).getColor()==Color.LIGHT)))){

                        pane.setStyle("-fx-background-color: rgba(255,0,0, 0.5);");

                    }else if(pane.getImage() == null){
                        pane.setStyle("-fx-background-color: rgba(0, 255, 0, 0.5);");
                    }

                    dragEvent.consume();
                });

                pane.setOnDragExited(dragEvent -> {
                    if((pane.X+pane.Y)%2!=0)
                        pane.setStyle("-fx-background-color: rgb(255,210,96);");
                    else
                        pane.setStyle("-fx-background-color: rgb(255,255,255);");

                    dragEvent.consume();
                });

                pane.setOnDragOver(dragEvent -> {
                    if (dragEvent.getGestureSource() != pane &&
                            dragEvent.getDragboard().hasImage()) {
                        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    dragEvent.consume();
                });

                pane.setOnDragDropped(dragEvent -> {
                    if(dragEvent.getGestureSource().getClass() == MyPane.class) {
                        dragEvent.setDropCompleted(true);

                        MyPane source = (MyPane) dragEvent.getGestureSource();

                        String transform = null;

                        if(source.getImage().getClass() == Pawn.class
                                && (pane.Y == 1||pane.Y == 8)){
                            ChoiceDialog<String> dialog = new ChoiceDialog<>();
                            dialog.setTitle("Transform");
                            dialog.setContentText("Choose piece: ");
                            dialog.getItems().add("Queen");
                            dialog.getItems().add("Rogue");
                            dialog.getItems().add("Bishop");
                            dialog.getItems().add("Knight");
                            dialog.setSelectedItem("Queen");

                            Optional<String> choice = dialog.showAndWait();

                            switch (choice.get()){
                                case "Queen":
                                    transform = "Q";
                                    break;
                                case "Rogue":
                                    transform = "R";
                                    break;
                                case "Bishop":
                                    transform = "B";
                                    break;
                                case "Knight":
                                    transform = "Kn";
                                    break;
                            }
                        }

                        if(endpoint!=null)
                            endpoint.sendMove(this.gameId, source.getCell(), pane.getCell(), transform);

                        dragEvent.consume();
                    }
                });

                pane.setOnDragDone(dragEvent ->
                {});

                cells.add(pane);
                board.add(pane, j, i);

            }
        }

        for(int i = 1; i<9;i++){
            StringBuilder textStr = new StringBuilder("");
            textStr.append((char)((rotated)?'H'-(i-1):'A'+(i-1)));
            Label text = new Label(textStr.toString());

            board.add(text, i, 0);
            GridPane.setHalignment(text, HPos.CENTER);
        }

        for(int i = 1; i<9;i++){
            Label text = new Label(String.valueOf(i));

            board.add(text, 0, (rotated)?i:9-i);
            GridPane.setHalignment(text, HPos.RIGHT);
        }

        List<String> initBoard = new ArrayList<>();

        initBoard.add("dR");
        initBoard.add("dKn");
        initBoard.add("dB");
        initBoard.add("dQ");
        initBoard.add("dK");
        initBoard.add("dB");
        initBoard.add("dKn");
        initBoard.add("dR");

        for(int i = 0;i<8;i++)
            initBoard.add("dP");

        for(int i = 0;i<32;i++)
            initBoard.add("");

        for(int i = 0;i<8;i++)
            initBoard.add("lP");

        initBoard.add("lR");
        initBoard.add("lKn");
        initBoard.add("lB");
        initBoard.add("lQ");
        initBoard.add("lK");
        initBoard.add("lB");
        initBoard.add("lKn");
        initBoard.add("lR");


        updateBoard(initBoard);

        this.setTime(time, time);

        LightsTicker lticker = new LightsTicker();
        DarksTicker dticker = new DarksTicker();

        lticker.setDaemon(true);
        dticker.setDaemon(true);

        lticker.start();
        dticker.start();
    }

    public void addLastMove(String move, Color color){
        if(color == Color.LIGHT) {
            Label label = new Label((movesLightVBox.getChildren().size()+1)+". "+move);
            label.setWrapText(true);
            movesLightVBox.getChildren().add(label);
        }
        else {
            Label label = new Label((movesDarkVBox.getChildren().size()+1)+". "+move);
            label.setWrapText(true);
            movesDarkVBox.getChildren().add(label);
        }

        if(rotated && color == Color.LIGHT){
            for(int i = move.length()-1; i>=0;i--){
                if(move.charAt(i)>='1'&&move.charAt(i)<='8'){
                    enemyLastMoveX = 7 - (move.charAt(i-1) - 'a');
                    enemyLastMoveY = move.charAt(i) - '1';
                    break;
                }
            }
            cells.get(8*enemyLastMoveY+enemyLastMoveX)
                    .setStyle("-fx-background-color: rgba(0, 255, 0, 0.3);");

        }else if(!rotated && color == Color.DARK){
            for(int i = move.length()-1; i>=0;i--){
                if(move.charAt(i)>='1'&&move.charAt(i)<='8'){
                    enemyLastMoveX = move.charAt(i-1) - 'a';
                    enemyLastMoveY = 7 - (move.charAt(i) - '1');
                    break;
                }
            }
            cells.get(8*enemyLastMoveY+enemyLastMoveX)
                    .setStyle("-fx-background-color: rgba(0, 255, 0, 0.3);");
        }
    }

    public void updateBoard(List<String> pieces) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String piece = pieces.get(8*i+j);
                switch (piece) {
                    case "dP":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Pawn.class
                                || ((Pawn) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Pawn(Color.DARK));
                        break;

                    case "lP":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Pawn.class
                                || ((Pawn) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Pawn(Color.LIGHT));
                        break;

                    case "dR":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Rogue.class
                                || ((Rogue) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Rogue(Color.DARK));
                        break;

                    case "lR":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Rogue.class
                                || ((Rogue) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Rogue(Color.LIGHT));
                        break;

                    case "dB":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Bishop.class
                                || ((Bishop) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Bishop(Color.DARK));
                        break;

                    case "lB":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Bishop.class
                                || ((Bishop) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Bishop(Color.LIGHT));
                        break;

                    case "dKn":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Knight.class
                                || ((Knight) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Knight(Color.DARK));
                        break;

                    case "lKn":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Knight.class
                                || ((Knight) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Knight(Color.LIGHT));
                        break;

                    case "dQ":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Queen.class
                                || ((Queen) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Queen(Color.DARK));
                        break;

                    case "lQ":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != Queen.class
                                || ((Queen) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new Queen(Color.LIGHT));
                        break;

                    case "dK":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != King.class
                                || ((King) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.DARK)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new King(Color.DARK));
                        break;

                    case "lK":
                        if (cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage() == null
                                || cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage().getClass() != King.class
                                || ((King) cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).getImage()).getColor() != Color.LIGHT)
                            cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(new King(Color.LIGHT));
                        break;

                    default:
                        cells.get((!rotated)?(8*i+j):(8*(7-i)+(7-j))).setImage(null);
                        break;
                }
            }
        }
    }

    public void endOfGame(String winner, String reason){
        if(!"".equals(winner)) {
            winnerLabel.setText(winner);
            reasonLabel.setText(reason);
        }else
            winnerLabel.setText("DRAW");

        resultVBox.setVisible(true);
        this.isEnd = true;
    }

    @FXML
    public void onGiveUpButtonClicked(){
        if(!isEnd) {
            isEnd = true;
            endpoint.sendGiveUp(gameId);
        }
    }

    @FXML
    public void onCloseButtonClicked(){
       parent.closeGame(gameId);
    }

    class LightsTicker extends Thread {

        public void run() {
            synchronized (mutex) {
                while (!isEnd && !isInterrupted()) {
                    try {
                        if (turn == Color.LIGHT) {
                            Thread.sleep(500);
                            lightsTimer = Math.max(lightsTimer - 500, 0);
                            Platform.runLater(()->{
                                lightTimerLabel.setText(String.format("%02d:%02d",lightsTimer/1000/60,(lightsTimer/1000)%60));
                            });
                        } else {
                            mutex.notify();
                            mutex.wait();
                        }
                    } catch (InterruptedException exc) {
                        mutex.notify();
                    }
                }
            }
        }
    }

    class DarksTicker extends Thread{

        public void run(){
            synchronized (mutex) {
                while (!isEnd&&!isInterrupted()) {
                    try {
                        if (turn == Color.DARK) {
                            Thread.sleep(500);
                            darksTimer = Math.max(darksTimer - 500, 0);
                            Platform.runLater(()->{
                                darkTimerLabel.setText(String.format("%02d:%02d",darksTimer/1000/60,(darksTimer/1000)%60));
                            });
                        }else{
                            mutex.notify();
                            mutex.wait();
                        }
                    }catch (InterruptedException exc){
                        mutex.notify();
                    }
                }
            }
        }
    }

    @FXML
    public void saveGame() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                    DocumentBuilder builder = factory.newDocumentBuilder();

                    Document doc = builder.newDocument();

                    Element root = doc.createElement("Game");

                    doc.appendChild(root);

                    Element winner = doc.createElement("Winner");
                    winner.setTextContent(winnerLabel.getText());
                    root.appendChild(winner);

                    Element reason = doc.createElement("Reason");
                    reason.setTextContent(reasonLabel.getText());
                    root.appendChild(reason);

                    Element lightMoves = doc.createElement("LightMoves");
                    for(int i = 0; i<movesLightVBox.getChildren().size();i++){
                        Element move = doc.createElement("move");
                        move.setTextContent(((Label)movesLightVBox.getChildren().get(i)).getText());
                        lightMoves.appendChild(move);
                    }
                    root.appendChild(lightMoves);

                    Element darkMoves = doc.createElement("DarkMoves");
                    for(int i = 0; i<movesDarkVBox.getChildren().size();i++){
                        Element move = doc.createElement("move");
                        move.setTextContent(((Label)movesDarkVBox.getChildren().get(i)).getText());
                        darkMoves.appendChild(move);
                    }

                    root.appendChild(darkMoves);

                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    StringWriter writer = new StringWriter();
                    transformer.transform(new DOMSource(doc), new StreamResult(writer));

                    String gameXML = writer.getBuffer().toString();

                    File file = new File("games/"+gameId+".xml");
                    file.createNewFile();
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
                        bufferedWriter.write(gameXML);
                    }

                } catch (ParserConfigurationException | IOException |TransformerException exc) {
                    exc.printStackTrace();
                }
            }
        }).start();

    }

}
