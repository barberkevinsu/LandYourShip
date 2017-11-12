import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.undo.*;
import javax.vecmath.*;

public class GameModel extends Observable {

    public GameModel(int fps, int width, int height, int peaks) {

        ship = new Ship(60, width/2, 50);

        worldBounds = new Rectangle2D.Double(0, 0, width, height);

        // anonymous class to monitor ship updates
        ship.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                setChangedAndNotify();
            }
        });
    }

    // World
    // - - - - - - - - - - -
    public final Rectangle2D getWorldBounds() {
        return worldBounds;
    }

    Rectangle2D.Double worldBounds;
    //fields for landing pad
    int landing_pad_x;
    int landing_pad_y;

    //my undo manager
    private UndoManager undoManager;
    // Ship
    // - - - - - - - - - - -

    public Ship ship;

    // Observerable
    // - - - - - - - - - - -


    //uodate views
    public void updateViews() {
      setChanged();
      notifyObservers();
    }

    public boolean outside_the_world(int x, int y, int w, int h){
      if(x < 0 || x + w > worldBounds.getWidth() ){
        return true;
      }else if(y < 0 || y + h > worldBounds.getHeight() ){
        return true;
      }else{
        return false;
      }
    }

    //set the landing pad's coordinate in model
    //just for easy access when game playing
    public void setLandingPadCoord(int x, int y){
      this.landing_pad_x = x;
      this.landing_pad_y = y;
    }
}
