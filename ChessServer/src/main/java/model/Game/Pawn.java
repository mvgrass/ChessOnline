package model.Game;

import java.util.List;
import java.util.Set;

/**
 * Created by maxim on 03.11.18.
 */
public class Pawn extends Piece {

    public Pawn(int x, int y, COLOR col) {
        super(x, y, col);

        isFirst = true;
    }

    @Override
    void checkDesk(List<List<Piece>> desk, Set<Integer> possibleMoves) {

        if (this.getColor() == COLOR.DARK) {
            if(this.getY()-1>=0) {
                if (desk.get(this.getY() - 1).get(this.getX()) == null) {
                    possibleMoves.add(8 * (this.getY() - 1) + this.getX());

                    if(this.isFirst&&desk.get(this.getY() - 2).get(this.getX()) == null)
                        possibleMoves.add(8 * (this.getY() - 2) + this.getX());
                }

            }

            if(this.getY()-1>=0&&this.getX()-1>=0)
                if (desk.get(this.getY() - 1).get(this.getX() - 1) != null && desk.get(this.getY() - 1).get(this.getX() - 1).getColor() == COLOR.LIGHT)
                    possibleMoves.add(8 * (this.getY() - 1) + this.getX() - 1);

            if(this.getY()-1>=0&&this.getX()+1<8)
                if (desk.get(this.getY() - 1).get(this.getX() + 1) != null && desk.get(this.getY() - 1).get(this.getX() + 1).getColor() == COLOR.LIGHT)
                    possibleMoves.add(8 * (this.getY() - 1) + this.getX() + 1);


            //en passant
            if(getY() == 3){
                if(getX()-1>=0
                        &&desk.get(getY()-1).get(getX()-1)==null
                        &&desk.get(getY()).get(getX()-1)!=null
                        &&desk.get(getY()).get(getX()-1).getColor() == COLOR.LIGHT
                        &&desk.get(getY()).get(getX()-1).getClass() == Pawn.class
                        )
                    possibleMoves.add(8*(getY()-1)+getX()-1);


                if(getX()+1 < 8
                        &&desk.get(getY()-1).get(getX()+1)==null
                        &&desk.get(getY()).get(getX()+1)!=null
                        &&desk.get(getY()).get(getX()+1).getColor() == COLOR.LIGHT
                        &&desk.get(getY()).get(getX()+1).getClass() == Pawn.class
                        )
                    possibleMoves.add(8*(getY()-1)+getX()+1);
            }

        } else {
            if(this.getY()+1<8) {
                if (desk.get(this.getY() + 1).get(this.getX()) == null) {
                    possibleMoves.add(8 * (this.getY() + 1) + this.getX());

                    if(this.isFirst&&desk.get(this.getY() + 2).get(this.getX()) == null)
                        possibleMoves.add(8 * (this.getY() + 2) + this.getX());
                }

            }

            if(this.getY()+1<8&&this.getX()-1>=0)
                if (desk.get(this.getY() + 1).get(this.getX() - 1) != null
                        && desk.get(this.getY() + 1).get(this.getX() - 1).getColor() == COLOR.DARK)
                    possibleMoves.add(8 * (this.getY() + 1) + this.getX() - 1);

            if(this.getY()+1<8&&this.getX()+1<8)
                if (desk.get(this.getY() + 1).get(this.getX() + 1) != null
                        && desk.get(this.getY() + 1).get(this.getX() + 1).getColor() == COLOR.DARK)
                    possibleMoves.add(8 * (this.getY() + 1) + this.getX() + 1);

            //en passant
            if(getY() == 4){
                if(getX()-1>=0
                        &&desk.get(getY()+1).get(getX()-1)==null
                        &&desk.get(getY()).get(getX()-1)!=null
                        &&desk.get(getY()).get(getX()-1).getColor() == COLOR.DARK
                        &&desk.get(getY()).get(getX()-1).getClass() == Pawn.class
                        )
                    possibleMoves.add(8*(getY()+1)+getX()-1);


                if(getX()+1 < 8
                        &&desk.get(getY()+1).get(getX()+1)==null
                        &&desk.get(getY()).get(getX()+1)!=null
                        &&desk.get(getY()).get(getX()+1).getColor() == COLOR.DARK
                        &&desk.get(getY()).get(getX()+1).getClass() == Pawn.class
                        )
                    possibleMoves.add(8*(getY()+1)+getX()+1);
            }
        }
    }
}
