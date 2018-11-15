package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class Queen extends Piece {
    public Queen(int x, int y, COLOR col){
        super(x,y,col);
    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves){
        int i;
        int j;

        i = 1;
        while(this.getX()-i>=0&&checkCell(this.getX()-i, this.getY(), desk, possibleMoves)) {
            i++;
        }

        i = 1;
        while(this.getX()+i<8&&checkCell(this.getX()+i, this.getY(), desk, possibleMoves)) {
            i++;
        }

        j = 1;
        while(this.getY()-j>=0&&checkCell(this.getX(), this.getY()-j, desk, possibleMoves)) {
            j++;
        }

        j = 1;
        while(this.getY()+j<8&&checkCell(this.getX(), this.getY()+j, desk, possibleMoves)) {
            j++;
        }

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
