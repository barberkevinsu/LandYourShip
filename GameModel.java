import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.undo.*;
import javax.vecmath.*;
import java.util.Random;
import java.lang.Math;

public class GameModel extends Observable {
    //my fields
    Rectangle2D.Double worldBounds;
    //fields for landing pad
    Point2d landing_pad_coord;
    Point2d landing_pad_size;
    //coordinates of 20 peaks + left bottom corner + right bottom corner
    int peaks;
    int[] peak_xPoints;
    int[] peak_yPoints;
    Polygon terrain_poly;
    //my undo manager
    private UndoManager undoManager;

    // Ship
    public Ship ship;
    Rectangle2D ship_rect;//bounded with Ship, used to detect if intersect with polygon

    //ship size
    int ship_width;
    int ship_height;

    //store if it needs reset
    boolean landed = false;
    boolean crashed = false;


    public GameModel(int fps, int width, int height, int peaks) {

        ship = new Ship(this, 60, width/2, 50);
        ship_width = 10;
        ship_height = 10;
        this.ship_rect = new Rectangle2D.Double();
        this.ship_rect.setRect(width - ship_width/2, height - ship_height/2, ship_width, ship_height);

        worldBounds = new Rectangle2D.Double(0, 0, width, height);

        undoManager = new UndoManager();
        //set up landing pad coord
        landing_pad_coord = new Point2d();
        landing_pad_coord.x = 330;
        landing_pad_coord.y = 100;

        //set up landing pad size
        landing_pad_size = new Point2d();
        landing_pad_size.x = 40;
        landing_pad_size.y = 10;



        //randomly set peaks
        this.peaks = peaks;
        int range_max = (int)(worldBounds.getHeight());
        int range_min = (int)(worldBounds.getHeight()/2);
        peak_xPoints = new int[peaks + 2];//include left bottom corner and right bottom corner
        peak_yPoints = new int[peaks + 2];//to draw polygon
        peak_xPoints[0] = 0;
        peak_yPoints[0] = (int)worldBounds.getHeight();
        for(int i=1; i<peaks + 1; i++){
          Random rand = new Random();
          //get y for peak(i)
          int peak_y = rand.nextInt((range_max - range_min) + 1) + range_min;
          //get x for peak(i)
          int peak_x = (int)((i-1) * (worldBounds.getWidth()/(peaks - 1)));
          peak_xPoints[i] = peak_x;
          peak_yPoints[i] = peak_y;
        }
        peak_xPoints[peaks + 1] = (int)worldBounds.getWidth();
        peak_yPoints[peaks + 1] = (int)worldBounds.getHeight();
        terrain_poly = new Polygon(peak_xPoints, peak_yPoints, peaks+2);

        // anonymous class to monitor ship updates
        ship.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                setChangedAndNotify();
            }
        });
    }

    public void land_on_pad() {
      double cur_speed = this.ship.getSpeed();
      Rectangle2D my_pad_rect = new Rectangle2D.Double(landing_pad_coord.x, landing_pad_coord.y, landing_pad_size.x, landing_pad_size.y);
      if(my_pad_rect.intersects(ship_rect)){
        if(cur_speed < getSafeLandingSpeed()){
          System.out.println("safe landed");
          landed = true;
          this.ship.setPaused(true);
        }else{
          System.out.println("too fast: crash");
          crashed = true;
          this.ship.setPaused(true);
        }
      }
    }
    //called by playview keyPressed
    public void move_my_ship(char e){
      switch (e) {
        case 'w':
          this.ship.thrustUp();
          this.ship.setChangedAndNotify();
          break;

        case 'a':
          this.ship.thrustLeft();
          this.ship.setChangedAndNotify();
          break;

        case 's':
          this.ship.thrustDown();
          this.ship.setChangedAndNotify();
          break;

        case 'd':
          this.ship.thrustRight();
          this.ship.setChangedAndNotify();
          break;

        case ' ':
          if(landed || crashed){
            this.ship.setPaused(true);
            landed = false;
            crashed = false;
            this.ship.reset(new Point2d(350, 50));
            break;
          }
          if(this.ship.isPaused()){
            this.ship.setPaused(false);
          }else{
            this.ship.setPaused(true);
          }//note: setPaused will setChangedAndNotify
          break;

        default:
          Point2d pos = this.ship.getPosition();
          break;
      }
    }

    //detect crash
    public void crash_or_lost() {
      if(this.terrain_poly.intersects(this.ship_rect) || !worldBounds.contains(this.ship_rect)){
        this.ship.setPaused(true);
        crashed = true;
      }
    }

    // World
    public final Rectangle2D getWorldBounds() {
        return worldBounds;
    }

    // get safe speed
    public double getSafeLandingSpeed() {
      return this.ship.getSafeLandingSpeed();
    }

    // get landing pad coordinates
    public Point2d getPadCoord(){
      return landing_pad_coord;
    }

    // get landing pad size
    public Point2d getPadSize(){
      return landing_pad_size;
    }

    // get peak's x y
    public int[] getPeakXPoints(){
      return peak_xPoints;
    }

    public int[] getPeakYPoints(){
      return peak_yPoints;
    }

    //get peak's y value
    public int getPeakValue(int index){
      return peak_yPoints[index];
    }

    //update views
    public void setChangedAndNotify() {
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
    //set ship Rectangle2D
    public void setShipRect(double x, double y, double w, double h){
      this.ship_rect.setRect(x, y, w, h);
    }

    //set the landing pad's coordinate in model
    //just for easy access when game playing
    public void setLandingPadCoord(int x, int y){
      this.landing_pad_coord.x = x;
      this.landing_pad_coord.y = y;
    }

    //set peak y value function
    public void setPeakValue(int i, int y){
      this.peak_yPoints[i] = y;
      updateTerrainPoly();
    }

    public void updateTerrainPoly(){
      this.terrain_poly.reset();
      this.terrain_poly = new Polygon(peak_xPoints, peak_yPoints, peaks+2);
    }

    //hittest for landding pad
    public boolean in_landingpad(double x, double y){
      if(x >= landing_pad_coord.x && x <= landing_pad_coord.x + landing_pad_size.x){
        if(y >= landing_pad_coord.y && y <= landing_pad_coord.y + landing_pad_size.y){
          return true;
        }
      }
      return false;
    }

    //hittest for peaks. -1 for miss, otherwise return index
    public int in_which_peak(int x, int y){
      //estimate which peak according to x, avoid O(n)
      double i = x / (this.getWorldBounds().getWidth() / (peaks - 1));
      int lower_i = (int)(Math.floor(i));
      int upper_i = (int)(Math.ceil(i));
      int lower_x = peak_xPoints[lower_i + 1];//+1 because bottom left corner point is in [0]
      int lower_y = peak_yPoints[lower_i + 1];
      int upper_x = peak_xPoints[upper_i + 1];
      int upper_y = peak_yPoints[upper_i + 1];

      if(x <= lower_x + 15){
        if(y <= lower_y + 15 && y >= lower_y - 15){
          return lower_i + 1;
        }else{
          return -1;
        }
      }else if(x >= upper_x - 15){
        if(y <= upper_y + 15 && y >= upper_y - 15){
          return upper_i + 1;
        }else{
          return -1;
        }
      }else{
        return -1;
      }
    }

    //Pad: undo, redo methods
    public void setPadUndoable(int change_in_x, int change_in_y){

      UndoableEdit undoableEdit = new AbstractUndoableEdit() {

  			// Method that is called when we must redo the undone action
  			public void redo() throws CannotRedoException {
  				super.redo();
  				landing_pad_coord.x += change_in_x;
          landing_pad_coord.y += change_in_y;
          setChangedAndNotify();
  				System.out.println("Pad: redo value");
  			}

  			public void undo() throws CannotUndoException {
  				super.undo();
          landing_pad_coord.x -= change_in_x;
          landing_pad_coord.y -= change_in_y;
          setChangedAndNotify();
  				System.out.println("Pad: undo value");
  			}
  		};


  		// Add this undoable edit to the undo manager
  		undoManager.addEdit(undoableEdit);
    }

    //peaks: undo redo method
    public void setPeakUndoable(int index, int change_in_y){

      UndoableEdit undoableEdit = new AbstractUndoableEdit() {

        // Method that is called when we must redo the undone action
        public void redo() throws CannotRedoException {
          super.redo();
          peak_yPoints[index] += change_in_y;
          setChangedAndNotify();
          System.out.println("Peak: redo value");
        }

        public void undo() throws CannotUndoException {
          super.undo();
          peak_yPoints[index] -= change_in_y;
          setChangedAndNotify();
          System.out.println("Peak: undo value");
        }
      };

      // Add this undoable edit to the undo manager
      undoManager.addEdit(undoableEdit);
    }

    // undo and redo methods
  	// - - - - - - - - - - - - - -

  	public void undo() {
  		if (canUndo()){
  			undoManager.undo();
      }else{
        System.out.println("cannot undo");
      }
      setChangedAndNotify();
  	}

  	public void redo() {
  		if (canRedo()){
  			undoManager.redo();
      }else{
        System.out.println("cannot redo");
      }
      setChangedAndNotify();
  	}

  	public boolean canUndo() {
  		return undoManager.canUndo();
  	}

  	public boolean canRedo() {
  		return undoManager.canRedo();
  	}

}
