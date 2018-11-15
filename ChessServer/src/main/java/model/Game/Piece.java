package model.Game;

import java.util.List;
import java.util.Set;


abstract public class Piece {
    public enum COLOR {DARK, LIGHT};

    protected boolean isFirst;

    public void setFirst(boolean f){isFirst = f;};
    public boolean isFirst(){return isFirst;}

    Piece(int x, int y, COLOR col){
        setX(x);
        setY(y);
        setColor(col);
    }

    private int x;
    private int y;

    private COLOR color;

    int getX(){return x;}
    int getY(){return y;}
    public COLOR getColor(){return this.color;}

    void setX(int x){this.x = x;}
    void setY(int y){this.y = y;}
    void setColor(COLOR col){this.color = col;}

    abstract void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves);

    protected boolean checkCell(int X, int Y, List<List<Piece>> desk, Set<Integer> possibleMoves){
        Piece fig = desk.get(Y).get(X);
        if(fig==null){
            possibleMoves.add(Y*8+X);
            return true;
        }
        else if(fig.getColor()!=this.getColor()) {
            possibleMoves.add(Y*8+X);
            return false;
        }

        return false;
    }

    void moveOn(int X, int Y){
        this.setX(X);
        this.setY(Y);

        this.setFirst(false);
    }
}

