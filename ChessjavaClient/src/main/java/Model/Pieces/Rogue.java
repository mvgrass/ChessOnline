package Model.Pieces;

import javafx.scene.image.Image;

/**
 * Created by maxim on 03.11.18.
 */
public class Rogue extends Image implements Colored{
    public Color getColor() {
        return color;
    }

    private Color color;

    public Rogue(Color color){
        super((color==Color.DARK)?"/images/Pieces/Chess_rdt60.png":"/images/Pieces/Chess_rlt60.png");

        this.color = color;
    }
}
