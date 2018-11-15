import org.junit.Assert;
import org.junit.Test;
import model.Game.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxim on 03.11.18.
 */
public class GameTest {

    @Test(expected = ForbiddenMoveException.class)
    public void testWrongMove() throws ForbiddenMoveException{
        Game game = new Game();

        //Queen trying move like Knight
        game.move(3, 0, 4, 2, null);
    }

    @Test
    public void moveTest1() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(1).set(7,new Pawn(7, 1, Piece.COLOR.LIGHT));
        desk.get(0).set(7,new King(7, 0, Piece.COLOR.LIGHT));
        desk.get(7).set(0,new King(0, 7, Piece.COLOR.DARK));

        Game game = new Game(desk, Piece.COLOR.LIGHT);

        game.move(7, 1, 7, 3, null);
        Assert.assertNotNull(game.getDesk().get(3).get(7));
        Assert.assertNull(game.getDesk().get(1).get(7));
    }


    @Test
    public void castlingTest() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(7,new Rogue(7, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(4, 7, 6, 7, null);
        Assert.assertEquals(game.getDesk().get(7).get(6).getClass(), King.class);
        Assert.assertEquals(game.getDesk().get(7).get(5).getClass(), Rogue.class);
        Assert.assertNull(game.getDesk().get(7).get(4));
        Assert.assertNull(game.getDesk().get(7).get(7));
    }

    @Test(expected = ForbiddenMoveException.class)
    public void castlingFailTest1() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(7,new Rogue(7, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(4,new Rogue(4, 0, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(4, 7, 6, 7, null);
    }

    @Test(expected = ForbiddenMoveException.class)
    public void castlingFailTest2() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(7,new Rogue(7, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(5,new Rogue(5, 0, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(4, 7, 6, 7, null);
    }

    @Test(expected = ForbiddenMoveException.class)
    public void castlingFailTest3() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(7,new Rogue(7, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(6,new Rogue(6, 0, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(4, 7, 6, 7, null);
    }

    @Test
    public void eatTest() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(6,new Rogue(6, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(6,new Rogue(6, 0, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(6, 7, 6, 0, null);
        Assert.assertEquals(game.getDesk().get(0).get(6).getClass(), Rogue.class);
        Assert.assertEquals(game.getDesk().get(0).get(6).getColor(), Piece.COLOR.DARK);
    }

    @Test(expected = ForbiddenMoveException.class)
    public void failEatTest() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(3,new Rogue(3, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(3,new Rogue(3, 0, Piece.COLOR.LIGHT));
        desk.get(7).set(0, new Queen(0, 7, Piece.COLOR.LIGHT));

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(3, 7, 3, 0, null);
    }

    @Test
    public void transformTest() throws ForbiddenMoveException{
        List<List<Piece>> desk = new ArrayList<>();
        for(int i = 0;i<8;i++) {
            List<Piece> subList = new ArrayList<>();
            for (int j = 0; j<8; j++)
                subList.add(null);
            desk.add(new ArrayList<>(subList));
        }

        desk.get(7).set(3,new Rogue(3, 7, Piece.COLOR.DARK));
        desk.get(7).set(4,new King(4, 7, Piece.COLOR.DARK));
        desk.get(0).set(0,new King(0, 0, Piece.COLOR.LIGHT));
        desk.get(0).set(3,new Rogue(3, 0, Piece.COLOR.LIGHT));
        desk.get(1).set(4, new Pawn(4, 1, Piece.COLOR.DARK));
        desk.get(1).get(4).setFirst(false);

        Game game = new Game(desk, Piece.COLOR.DARK);
        game.move(4, 1, 3, 0, new Knight(0, 3, Piece.COLOR.DARK));
        Assert.assertEquals(game.getDesk().get(0).get(3).getClass(), Knight.class);
    }

    @Test
    public void mateTest1() throws ForbiddenMoveException{
        Game game = new Game();

        game.move(4, 1, 4, 3, null);
        game.move(0, 6, 0, 4, null);
        game.move(3, 0, 5, 2, null);
        game.move(1, 7, 2, 5, null);
        game.move(5, 0, 2, 3, null);
        game.move(7, 6, 7, 4, null);
        game.move(5, 2, 5, 6, null);

        Assert.assertTrue(game.isMate());
    }

    @Test
    public void PassantTest() throws ForbiddenMoveException{
        Game game = new Game();
        game.move(4, 1, 4, 3, null);
        game.move(0, 6, 0, 4, null);
        game.move(4, 3, 4, 4, null);
        game.move(3, 6, 3, 4, null);
        game.move(4, 4, 3, 5, null);

        Assert.assertEquals(game.getDesk().get(5).get(3).getClass(), Pawn.class);
        Assert.assertNull(game.getDesk().get(4).get(3));
    }

    @Test(expected = ForbiddenMoveException.class)
    public void failPassantTest() throws ForbiddenMoveException{
        Game game = new Game();
        game.move(4, 1, 4, 3, null);
        game.move(0, 6, 0, 4, null);
        game.move(4, 3, 4, 4, null);
        game.move(3, 6, 3, 5, null);
        game.move(0, 1, 0, 2, null);
        game.move(3, 5, 3, 4, null);
        game.move(4, 4, 3, 5, null);
    }
}
