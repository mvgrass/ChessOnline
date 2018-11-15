package Model.Pieces;

import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class Queen extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public Queen(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_qdt60.png":"/images/Pieces/Chess_qlt60.png");

        this.color = color;
    }
}
