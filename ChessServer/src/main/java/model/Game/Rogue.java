package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class Rogue extends Piece {

    public Rogue(int x, int y, COLOR col){
        super(x,y,col);

        this.isFirst = true;
    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves){
        int i;

        i = 1;
        while(this.getX()-i>=0&&checkCell(this.getX()-i, this.getY(), desk, possibleMoves)) {
            i++;
        }

        i = 1;
        while(this.getX()+i<8&&checkCell(this.getX()+i, this.getY(), desk, possibleMoves)) {
            i++;
        }

        i = 1;
        while(this.getY()-i>=0&&checkCell(this.getX(), this.getY()-i, desk, possibleMoves)) {
            i++;
        }

        i = 1;
        while(this.getY()+i<8&&checkCell(this.getX(), this.getY()+i, desk, possibleMoves)) {
            i++;
        }
    }
}
