package Model.Pieces;

import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class Bishop extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public Bishop(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_bdt60.png":"/images/Pieces/Chess_blt60.png");

        this.color = color;
    }
}
