package Model.Pieces;

import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class King extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public King(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_kdt60.png":"/images/Pieces/Chess_klt60.png");

        this.color = color;
    }
}
