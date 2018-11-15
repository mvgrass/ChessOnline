package Model.Pieces;


import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class Pawn extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public Pawn(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_pdt60.png":"/images/Pieces/Chess_plt60.png");

        this.color = color;
    }
}
