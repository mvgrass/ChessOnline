package Model.Pieces;

import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class Knight extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public Knight(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_kndt60.png":"/images/Pieces/Chess_knlt60.png");

        this.color = color;
    }
}
