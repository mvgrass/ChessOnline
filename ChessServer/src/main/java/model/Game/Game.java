package model.Game;


import java.util.*;

/**
 * Created by maxim on 01.11.18.
 */
public class Game {
    private List<List<Piece>> desk;
    private List<List<Piece>> copyDesk;

    private Piece.COLOR turn;

    private List<Piece> Lights = new LinkedList<>();
    private List<Piece> Darks = new LinkedList<>();

    private boolean mate = false;
    private boolean draw = false;

    private int lastMoveStartX = -1;
    private int lastMoveStartY = -1;
    private int lastMoveEndX = -1;
    private int lastMoveEndY = -1;

    private String lastMoveAsString;

    public Game(){
        desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            desk.add(new ArrayList<Piece>(8));
            List<Piece> subArr = desk.get(i);
            for (int j = 0;j<8;j++)
                subArr.add(null);

        }

        for(int i = 0;i<8;i++){

            desk.get(1).set(i, new Pawn(i, 1, Piece.COLOR.LIGHT));
            Lights.add(desk.get(1).get(i));


            desk.get(6).set(i, new Pawn(i, 6 , Piece.COLOR.DARK));
            Darks.add(desk.get(6).get(i));
        }

        List<Piece> subLights = desk.get(0);
        List<Piece> subDarks = desk.get(7);

        subDarks.set(0, new Rogue(0, 7, Piece.COLOR.DARK));
        subDarks.set(7, new Rogue(7, 7, Piece.COLOR.DARK));
        subLights.set(0, new Rogue(0, 0, Piece.COLOR.LIGHT));
        subLights.set(7, new Rogue(7, 0, Piece.COLOR.LIGHT));

        subDarks.set(1, new Knight(1, 7, Piece.COLOR.DARK));
        subDarks.set(6, new Knight(6, 7, Piece.COLOR.DARK));
        subLights.set(1, new Knight(1, 0, Piece.COLOR.LIGHT));
        subLights.set(6, new Knight(6, 0, Piece.COLOR.LIGHT));

        subDarks.set(2, new Bishop(2, 7, Piece.COLOR.DARK));
        subDarks.set(5, new Bishop(5, 7, Piece.COLOR.DARK));
        subLights.set(2, new Bishop(2, 0, Piece.COLOR.LIGHT));
        subLights.set(5, new Bishop(5, 0, Piece.COLOR.LIGHT));

        subDarks.set(3, new Queen(3, 7, Piece.COLOR.DARK));
        subDarks.set(4, new King(4, 7, Piece.COLOR.DARK));

        subLights.set(3, new Queen(3, 0, Piece.COLOR.LIGHT));
        subLights.set(4, new King(4, 0, Piece.COLOR.LIGHT));

        for(int i = 0;i<8;i++){
            Darks.add(subDarks.get(i));
            Lights.add(subLights.get(i));
        }

        turn = Piece.COLOR.LIGHT;

    }

    public Game(List<List<Piece>> desk, Piece.COLOR turn){
        this.desk = desk;
        this.turn = turn;

        for(int i = 0;i<8;i++)
            for(int j = 0;j<8;j++){
                if(desk.get(i).get(j)!=null){
                    if(desk.get(i).get(j).getColor()==Piece.COLOR.LIGHT)
                        Lights.add(desk.get(i).get(j));
                    else
                        Darks.add(desk.get(i).get(j));
                }
            }
    }

    public Piece.COLOR turn(){return this.turn;}

    private void setLastMove(int X, int Y, int endX, int endY){
        this.lastMoveStartX = X;
        this.lastMoveStartY = Y;
        this.lastMoveEndX = endX;
        this.lastMoveEndY = endY;
    }

    public List<List<Piece>> getDesk(){
        return this.desk;
    }

    public String getLastMoveString(){
        return lastMoveAsString;
    }

    public boolean isDraw(){
        return this.draw;
    }

    public void move(int startX, int startY, int endX, int endY, Piece new_fig) throws ForbiddenMoveException{
        copyDesk = new ArrayList<>();
        for(int i = 0;i<8;i++)
            copyDesk.add( new ArrayList<>(desk.get(i)));

        Piece f = copyDesk.get(startY).get(startX);

        if(f == null || f.getColor()!=turn)
            throw new ForbiddenMoveException();

        Set<Integer> possibleMoves = new HashSet<>();

        f.checkDesk(desk, possibleMoves);

        if(!possibleMoves.contains(8*endY + endX)){
            throw new ForbiddenMoveException();
        }

        if(f.getClass() == King.class
                && f.isFirst()
                && (endX == 2 || endX == 6) ){
            if(Castling(startX, startY, endX, endY)){
                f.moveOn(endX, endY);
                desk.get(endY).set(endX, f);
                desk.get(startY).set(startX, null);

                int rogueX = (endX == 2)?0:7;
                int rogueY = startY;
                int rogueEndX = (endX == 2)?3:5;
                int rogueEndY = rogueY;
                desk.get(rogueY).get(rogueX).moveOn(rogueEndX, rogueEndY);
                desk.get(rogueEndY).set(rogueEndX, desk.get(rogueY).get(rogueX));
                desk.get(rogueY).set(rogueX, null);

                setLastMove(startX, startY, endX, endY);

                this.lastMoveAsString = (endX == 6)?"O-O":"O-O-O";

                if(postCheck()) {
                    changeTurn();
                    this.mate = this.Mate();
                }else{
                    changeTurn();
                    this.draw = this.Draw();
                }
            }else
                throw new ForbiddenMoveException();

        }else if(enPassant(startX, startY, endX, endY)){

            boolean possible = false;
            if(turn == Piece.COLOR.DARK){
                if(startY == 3 ){
                    if(startX-1==endX
                            &&lastMoveStartX == endX
                            &&lastMoveStartY == 1
                            &&lastMoveEndX == endX
                            &&lastMoveEndY == endY + 1)
                        possible = true;


                    if(startX+1 == endX
                            &&lastMoveStartX == endX
                            &&lastMoveStartY == 1
                            &&lastMoveEndX == endX
                            &&lastMoveEndY == endY + 1
                            )
                        possible = true;
                }
            }else{
                if(startY == 4){

                    if(startX-1 == endX
                            &&lastMoveStartX == endX
                            &&lastMoveStartY == 6
                            &&lastMoveEndX == endX
                            &&lastMoveEndY == endY - 1)
                        possible = true;

                    if(startX + 1 == endX
                            &&lastMoveStartX == endX
                            &&lastMoveStartY == 6
                            &&lastMoveEndX == endX
                            &&lastMoveEndY == endY - 1)
                        possible = true;
                }
            }

            if(possible == false)
                throw new ForbiddenMoveException();

            Piece pawn = copyDesk.get(startY).get(startX);
            pawn.moveOn(endX, endY);
            copyDesk.get(endY).set(endX, pawn);
            copyDesk.get(startY).set(startX, null);

            Piece del = copyDesk.get(startY).get(endX);

            if(turn == Piece.COLOR.LIGHT){
                Darks.remove(del);
            }else{
                Lights.remove(del);
            }

            copyDesk.get(startY).set(endX, null);

            if(preCheck()){
                if(turn == Piece.COLOR.LIGHT){
                    Darks.add(del);
                }else{
                    Lights.add(del);
                }

                throw new ForbiddenMoveException();
            }else {
                desk = copyDesk;
            }

            setLastMove(startX, startY, endX, endY);
            StringBuilder lastmove = new StringBuilder();
            lastmove.append((char)('a'+startX));
            lastmove.append((char)('1'+startY));
            lastmove.append('-');
            lastmove.append((char)('a'+endX));
            lastmove.append((char)('1'+endY));
            this.lastMoveAsString = lastmove.toString();

            if(postCheck()) {
                changeTurn();
                this.mate = this.Mate();
            }else{
                changeTurn();
                this.draw = this.Draw();
            }
        } else {

            Piece del = desk.get(endY).get(endX);

            if (del != null) {
                if (del.getColor() == Piece.COLOR.LIGHT)
                    Lights.remove(del);
                else
                    Darks.remove(del);
            }

            boolean isFirst = f.isFirst();
            f.moveOn(endX, endY);

            copyDesk.get(endY).set(endX, f);
            copyDesk.get(startY).set(startX, null);

            if(preCheck()){
                if(del!=null){
                    if (del.getColor() == Piece.COLOR.LIGHT)
                        Lights.add(del);
                    else
                        Darks.add(del);
                }

                f.moveOn(startX, startY);
                f.setFirst(isFirst);

                throw new ForbiddenMoveException();
            }else
                desk = copyDesk;

            setLastMove(startX, startY, endX, endY);
            StringBuilder lastmove = new StringBuilder();
            String fig = "";
            if(f.getClass() == Rogue.class)
                fig = "R";
            else if(f.getClass() == Bishop.class)
                fig = "B";
            else if(f.getClass() == Knight.class)
                fig = "Kn";
            else if(f.getClass() == Queen.class)
                fig = "Q";
            else if(f.getClass() == King.class)
                fig = "K";
            lastmove.append(fig);
            lastmove.append((char)('a'+startX));
            lastmove.append((char)('1'+startY));
            lastmove.append('-');
            lastmove.append(fig);
            lastmove.append((char)('a'+endX));
            lastmove.append((char)('1'+endY));

            if(f.getClass() == Pawn.class
                    &&(endY == 0|| endY==7)) {
                transform(endX, endY, new_fig);


                String new_fig_str = "";
                if(new_fig.getClass() == Rogue.class)
                    new_fig_str = "R";
                else if(new_fig.getClass() == Bishop.class)
                    new_fig_str = "B";
                else if(new_fig.getClass() == Knight.class)
                    new_fig_str = "Kn";
                else if(new_fig.getClass() == Queen.class)
                    new_fig_str = "Q";
                else if(new_fig.getClass() == King.class)
                    new_fig_str = "K";

                lastmove.append(new_fig_str);
            }

            this.lastMoveAsString = lastmove.toString();

            if(postCheck()) {
                changeTurn();
                this.mate = this.Mate();
            }else{
                changeTurn();
                this.draw = this.Draw();
            }
        }
    }


    private void emulateMove(int startX, int startY, int endX, int endY) throws ForbiddenMoveException{

        copyDesk = new ArrayList<>();
        for(int i = 0;i<8;i++)
            copyDesk.add( new ArrayList<>(desk.get(i)));

        Piece f = copyDesk.get(startY).get(startX);

        boolean isFirst = f.isFirst();

        if(f == null || f.getColor()!=turn)
            throw new ForbiddenMoveException();

        Set<Integer> possibleMoves = new HashSet<>();

        f.checkDesk(desk, possibleMoves);


        if(!possibleMoves.contains(8*endY + endX)){
            throw new ForbiddenMoveException();
        }

        if(f.getClass() == King.class
                && f.isFirst()
                && (endX == 2 || endX == 6) ){
            if(!Castling(startX, startY, endX, endY))
                throw new ForbiddenMoveException();

        }else if(enPassant(startX, startY, endX, endY)){
            f.moveOn(endX, endY);
            copyDesk.get(endY).set(endX, f);
            copyDesk.get(startY).set(startX, null);

            Piece del = copyDesk.get(startY).get(endX);

            if(turn == Piece.COLOR.LIGHT){
                Darks.remove(del);
            }else{
                Lights.remove(del);
            }

            copyDesk.get(startY).set(endX, null);

            if(preCheck()){
                if(turn == Piece.COLOR.LIGHT){
                    Darks.add(del);
                }else{
                    Lights.add(del);
                }

                copyDesk.get(endY).set(endX, null);
                copyDesk.get(startY).set(startX, f);
                f.moveOn(startX, startY);

                throw new ForbiddenMoveException();
            }

            if(turn == Piece.COLOR.LIGHT){
                Darks.add(del);
            }else{
                Lights.add(del);
            }

            copyDesk.get(endY).set(endX, null);
            copyDesk.get(startY).set(startX, f);
            f.moveOn(startX, startY);
        } else {

            Piece del = desk.get(endY).get(endX);

            if (del != null) {
                if (del.getColor() == Piece.COLOR.LIGHT)
                    Lights.remove(del);
                else
                    Darks.remove(del);
            }

            f.moveOn(endX, endY);

            f.setFirst(isFirst);

            copyDesk.get(endY).set(endX, f);
            copyDesk.get(startY).set(startX, null);

            if(preCheck()){
                if(del!=null){
                    if (del.getColor() == Piece.COLOR.LIGHT)
                        Lights.add(del);
                    else
                        Darks.add(del);

                    copyDesk.get(endY).set(endX, del);
                }

                copyDesk.get(endY).set(endX, null);
                copyDesk.get(startY).set(startX, f);
                f.moveOn(startX, startY);
                f.setFirst(isFirst);

                throw new ForbiddenMoveException();
            }

            if(del!=null){
                if (del.getColor() == Piece.COLOR.LIGHT)
                    Lights.add(del);
                else
                    Darks.add(del);

                copyDesk.get(endY).set(endX, del);

            }else
                copyDesk.get(endY).set(endX, null);

            copyDesk.get(startY).set(startX, f);
            f.moveOn(startX, startY);
            f.setFirst(isFirst);
        }
    }

    private void changeTurn(){
        if(this.turn()==Piece.COLOR.LIGHT)
            this.turn = Piece.COLOR.DARK;
        else
            this.turn = Piece.COLOR.LIGHT;
    }

    public void transform(int X, int Y, Piece new_fig){

        if(new_fig == null){
            new_fig = new Queen(X, Y, turn);
        }

        if(desk.get(Y).get(X).getColor()==Piece.COLOR.LIGHT) {
            Lights.remove(desk.get(Y).get(X));
            Lights.add(new_fig);
        }
        else {
            Darks.remove(desk.get(Y).get(X));
            Darks.add(new_fig);
        }
        desk.get(Y).set(X, new_fig);
    }

    private boolean preCheck(){

        Piece.COLOR whom = turn;

        King k = null;
        List<Piece>enemy;
        if(whom == Piece.COLOR.LIGHT){
            enemy = Darks;
        }else{
            enemy = Lights;
        }

        find:
        {
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                    if (copyDesk.get(i).get(j) != null && copyDesk.get(i).get(j).getClass() == King.class && copyDesk.get(i).get(j).getColor() == whom) {
                        k = (King) copyDesk.get(i).get(j);
                        break find;
                    }
        }

        Set<Integer> possibleCells;
        for(int i = 0;i< enemy.size();i++){
            possibleCells = new HashSet<>();
            enemy.get(i).checkDesk(copyDesk,possibleCells);
            if(possibleCells.contains(8*k.getY()+k.getX())) {
                return true;
            }
        }

        return false;
    }

    private boolean postCheck(){

        Piece.COLOR whom = turn;

        King k = null;
        List<Piece>pieces;
        if(whom == Piece.COLOR.DARK){
            pieces = Darks;
        }else{
            pieces = Lights;
        }

        find:
        {
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                    if (desk.get(i).get(j) != null && desk.get(i).get(j).getClass() == King.class && desk.get(i).get(j).getColor() != whom) {
                        k = (King) desk.get(i).get(j);
                        break find;
                    }
        }

        Set<Integer> possibleCells;
        for(int i = 0;i< pieces.size();i++){
            possibleCells = new HashSet<>();
            pieces.get(i).checkDesk(desk,possibleCells);
            if(possibleCells.contains(8*k.getY()+k.getX())) {
                return true;
            }
        }

        return false;
    }

    public boolean Mate() {
        Piece.COLOR whom = turn;

        List<Piece> Pieces;
        Pieces = (whom == Piece.COLOR.LIGHT)?Lights:Darks;

        Set<Integer> possibleCells;
        for(int i = 0;i<Pieces.size();i++){
            possibleCells = new HashSet<>();
            Piece fig = Pieces.get(i);
            fig.checkDesk(desk,possibleCells);

            //Блокировка рокировки при проверке на мат
            if(fig!=null&&fig.getClass()==King.class&&fig.isFirst()){
                possibleCells.remove(2);
                possibleCells.remove(6);
                possibleCells.remove(62);
                possibleCells.remove(58);
            }

            boolean isMate = true;

            for(Iterator<Integer> cellIter = possibleCells.iterator(); cellIter.hasNext();){
                Integer cell  = cellIter.next();
                try {
                    this.emulateMove(fig.getX(), fig.getY(), cell % 8, cell / 8);
                    isMate = false;
                    break;
                }
                catch(ForbiddenMoveException exc){
                }


            }


            if(!isMate)
                return false;
        }

        return true;
    }

    private boolean Draw(){
       if(this.Darks.size() == 1 && this.Lights.size() == 1){
           return true;
       } else{
           Piece.COLOR whom = turn;

           List<Piece> Pieces;
           Pieces = (whom == Piece.COLOR.LIGHT)?Lights:Darks;

           Set<Integer> possibleCells;
           for(int i = 0;i<Pieces.size();i++){
               possibleCells = new HashSet<>();
               Piece fig = Pieces.get(i);
               fig.checkDesk(desk,possibleCells);

               boolean isDraw = true;

               for(Iterator<Integer> cellIter = possibleCells.iterator(); cellIter.hasNext();){
                   Integer cell  = cellIter.next();
                   try {
                       this.emulateMove(fig.getX(), fig.getY(), cell % 8, cell / 8);
                       isDraw = false;
                       break;
                   }
                   catch(ForbiddenMoveException exc){
                   }


               }


               if(!isDraw)
                   return false;
           }

           return true;
       }
    }

    public boolean isMate(){return this.mate;}

    private boolean Castling(int chosenX, int chosenY, int X, int Y){
        if(this.preCheck())
            return false;

        if(X == 6){
            try{
                emulateMove(chosenX, chosenY, 5, chosenY);
            }catch (ForbiddenMoveException exc){
                return false;
            }
        }

        if(X==2){
            try{
                emulateMove(chosenX, chosenY, 3, chosenY);
            }catch (ForbiddenMoveException exc){
                return false;
            }
        }

        Piece king = this.copyDesk.get(chosenY).get(chosenX);
        Piece rogue = (X == 6)?this.copyDesk.get(chosenY).get(7):this.copyDesk.get(chosenY).get(0);

        if(X==6) {
            king.moveOn(X, Y);

            rogue.moveOn(X-1, Y);

            this.copyDesk.get(chosenY).set(chosenX, null);
            this.copyDesk.get(chosenY).set(7, null);

            this.copyDesk.get(Y).set(X, king);
            this.copyDesk.get(Y).set(X-1, rogue);

            return !preCheck();

        }else{
            king.moveOn(X, Y);

            rogue.moveOn(X+1, Y);

            this.copyDesk.get(chosenY).set(chosenX, null);
            this.copyDesk.get(chosenY).set(0, null);

            this.copyDesk.get(Y).set(X, king);
            this.copyDesk.get(Y).set(X+1, rogue);

            return !preCheck();
        }

    }

    private boolean enPassant(int X, int Y, int endX, int endY){
        if(turn == Piece.COLOR.DARK){
            if(Y == 3
                    && desk.get(Y).get(X).getClass() == Pawn.class
                    && (endX == X-1 || endX == X+1)
                    && (endY == Y-1)
                    && endX>=0
                    && endX<8){
                if(X-1==endX
                        &&desk.get(Y-1).get(X-1)==null
                        &&desk.get(Y).get(X - 1)!=null
                        &&desk.get(Y).get(X-1).getColor() == Piece.COLOR.LIGHT
                        &&desk.get(Y).get(X-1).getClass() == Pawn.class
                        )
                    return true;


                if(X+1 == endX
                        &&desk.get(Y-1).get(X+1)==null
                        &&desk.get(Y).get(X + 1)!=null
                        &&desk.get(Y).get(X+1).getColor() == Piece.COLOR.LIGHT
                        &&desk.get(Y).get(X+1).getClass() == Pawn.class
                        )
                    return true;
            }
        }else{
            if(Y == 4
                    && desk.get(Y).get(X).getClass() == Pawn.class
                    && (endX == X-1 || endX == X+1)
                    && (endY == Y+1)
                    && endX>=0
                    && endX<8){

                if(X-1 == endX
                        &&desk.get(Y+1).get(X-1)==null
                        &&desk.get(Y).get(X-1)!=null
                        &&desk.get(Y).get(X-1).getColor() == Piece.COLOR.DARK
                        &&desk.get(Y).get(X-1).getClass() == Pawn.class
                        )
                    return true;

                if(X+1 == endX
                        &&desk.get(Y+1).get(X+1)==null
                        &&desk.get(Y).get(X+1)!=null
                        &&desk.get(Y).get(X+1).getColor() == Piece.COLOR.DARK
                        &&desk.get(Y).get(X+1).getClass() == Pawn.class
                        )
                    return true;
            }
        }

        return false;
    }

    private Piece get(int x, int y){
        return desk.get(y).get(x);
    }


}
