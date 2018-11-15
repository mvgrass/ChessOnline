package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class Knight extends Piece {
    public Knight(int x, int y, COLOR col){
        super(x,y,col);
    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves){
        if(this.getX()-1>=0&&this.getY()-2>=0
            && this.checkCell(this.getX()-1, this.getY()-2, desk, possibleMoves)){}


        if(this.getX()+1<8&&this.getY()-2>=0
            && this.checkCell(this.getX()+1, this.getY()-2, desk, possibleMoves)){}

        if(this.getX()+2<8&&this.getY()-1>=0
            && this.checkCell(this.getX()+2, this.getY()-1, desk, possibleMoves)){}

        if(this.getX()+2<8&&this.getY()+1<8
            && this.checkCell(this.getX()+2, this.getY()+1, desk, possibleMoves)){}

        if(this.getX()+1<8&&this.getY()+2<8
            && this.checkCell(this.getX()+1, this.getY()+2, desk, possibleMoves)){}

        if(this.getX()-1>=0&&this.getY()+2<8
            && this.checkCell(this.getX()-1, this.getY()+2, desk, possibleMoves)){}

        if(this.getX()-2>=0&&this.getY()+1<8
            && this.checkCell(this.getX()-2, this.getY()+1, desk, possibleMoves)){}

        if(this.getX()-2>=0&&this.getY()-1>=0
            && this.checkCell(this.getX()-2, this.getY()-1, desk, possibleMoves)){}
    }
}
