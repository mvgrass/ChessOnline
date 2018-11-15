package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class Bishop extends Piece {
    public Bishop(int x, int y, COLOR col){
        super(x,y,col);
    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves){
        int i,j;

        i = j = 1;
        while(this.getX()-i>=0&&this.getY()+j<8&&checkCell(this.getX()-i, this.getY()+j, desk, possibleMoves)){
            i++;j++;
        }

        i = j = 1;
        while(this.getX()+i<8&&this.getY()-j>=0&&checkCell(this.getX()+i, this.getY()-j, desk, possibleMoves)){
            i++;j++;
        }

        i = j = 1;
        while(this.getX()+i<8&&this.getY()+j<8&&checkCell(this.getX()+i, this.getY()+j, desk, possibleMoves)){
            i++;j++;
        }

        i = j = 1;
        while(this.getX()-i>=0&&this.getY()-j>=0&&checkCell(this.getX()-i, this.getY()-j, desk, possibleMoves)){
            i++;j++;
        }

    }
}
