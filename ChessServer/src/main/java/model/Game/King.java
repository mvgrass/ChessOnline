package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class King extends Piece {

    public King(int x, int y, COLOR col){
        super(x,y,col);

        this.isFirst = true;

    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves){
        if(this.getX()-1>=0
            && this.checkCell(this.getX()-1, this.getY(), desk,possibleMoves)){}


        if(this.getX()+1<8
            && this.checkCell(this.getX()+1, this.getY(), desk, possibleMoves)){}

        if(this.getY()+1<8
            && this.checkCell(this.getX(), this.getY()+1, desk, possibleMoves)){}

        if(this.getY()-1>=0
            && this.checkCell(this.getX(), this.getY()-1, desk, possibleMoves)){}

        if(this.getX()-1>=0&&this.getY()-1>=0
            && this.checkCell(this.getX()-1, this.getY()-1, desk, possibleMoves)){}

        if(this.getX()-1>=0&&this.getY()+1<8
            && this.checkCell(this.getX()-1, this.getY()+1, desk, possibleMoves)){}

        if(this.getX()+1<8&&this.getY()-1>=0
            && this.checkCell(this.getX()+1, this.getY()-1, desk, possibleMoves)){}

        if(this.getX()+1<8&&this.getY()+1<8
            && this.checkCell(this.getX()+1, this.getY()+1, desk, possibleMoves)){}


        if(isFirst()){
            if(this.getColor()==COLOR.DARK){
                Piece fig1 = desk.get(7).get(7);
                Piece fig2 = desk.get(7).get(0);
                if(desk.get(7).get(5)==null&&desk.get(7).get(6)==null&&fig1!=null&&fig1.isFirst())
                    possibleMoves.add(62);

                if(desk.get(7).get(1)==null&&desk.get(7).get(2)==null&&desk.get(7).get(3)==null&&fig2!=null&&fig2.isFirst())
                    possibleMoves.add(58);
            }else{
                Piece fig1 = desk.get(0).get(7);
                Piece fig2 = desk.get(0).get(0);
                if(desk.get(0).get(5)==null&&desk.get(0).get(6)==null&&fig1!=null&&fig1.isFirst())
                    possibleMoves.add(6);

                if(desk.get(0).get(1)==null&&desk.get(0).get(2)==null&&desk.get(0).get(3)==null&&fig2!=null&&fig2.isFirst())
                    possibleMoves.add(2);

            }
        }

    }

}
