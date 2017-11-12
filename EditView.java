import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Observable;
import java.util.Observer;
//added import
import javax.vecmath.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;

// the editable view of the terrain and landing pad
public class EditView extends JPanel implements Observer {

    //interface parts
    private GameModel model;
    //landing pad coordinate and width and height
    int landing_pad_x;
    int landing_pad_y;
    int landing_pad_w;
    int landing_pad_h;
    //coordinates of 20 peaks + left bottom corner + right bottom corner
    int[] peak_xPoints;
    int[] peak_yPoints;

    /***********************************************/

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //draw polygon
        g.setColor(Color.GRAY);
        g.fillPolygon(peak_xPoints, peak_yPoints, 22);

        //draw each circle
        g.setColor(Color.BLACK);
        for(int i=1; i<21; i++){
          g.drawOval(peak_xPoints[i]-15, peak_yPoints[i]-15, 29, 29);//documentation: width+1, height+1
        }

        //draw landing pad
        g.setColor(Color.RED);
        g.drawRect(landing_pad_x, landing_pad_y, landing_pad_w, landing_pad_h);
        g.fillRect(landing_pad_x, landing_pad_y, landing_pad_w, landing_pad_h);
    }

    public EditView(GameModel model) {
        this.model = model;
        model.addObserver(this);

        //set rectangle x, y, w, h
        landing_pad_x = 330;
        landing_pad_y = 100;
        landing_pad_w = 40;
        landing_pad_h = 10;

        //randomly set peaks
        Rectangle2D myWorld = model.getWorldBounds();
        int range_max = (int)(myWorld.getHeight());
        int range_min = (int)(myWorld.getHeight()/2);
        peak_xPoints = new int[22];//include left bottom corner and right bottom corner
        peak_yPoints = new int[22];//to draw polygon
        peak_xPoints[0] = 0;
        peak_yPoints[0] = (int)myWorld.getHeight();
        for(int i=1; i<21; i++){
          Random rand = new Random();
          //get y for peak(i)
          int peak_y = rand.nextInt((range_max - range_min) + 1) + range_min;
          //get x for peak(i)
          int peak_x = (int)((i-1) * (myWorld.getWidth()/19));
          peak_xPoints[i] = peak_x;
          peak_yPoints[i] = peak_y;
        }
        peak_xPoints[21] = (int)myWorld.getWidth();
        peak_yPoints[21] = (int)myWorld.getHeight();

        // want the background to be grey
        setBackground(Color.LIGHT_GRAY);

        //store landing pad's movement
        Point2d pad_startDragging = new Point2d();
        Point2d pad_stopDragging = new Point2d();

        //store peak's movement
        //x: index; y: coord-y movement
        Point2d peak_startDragging = new Point2d();
        Point2d peak_stopDragging = new Point2d();

        //offset vector for mouse pressed and pad coordinate
        Point2d pad_offset = new Point2d();
        Point2d peak_offset = new Point2d();

        //how much I moved to implement undo
        Point2d peak_moved_offset = new Point2d();
        Point2d pad_moved_offset = new Point2d();

        //give this view a mouse listenner
        this.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
              if(e.getClickCount() == 2){
                  System.out.println(e.getX());
                  landing_pad_x = e.getX() - 40/2;
                  landing_pad_y = e.getY() - 10/2;
                  model.setLandingPadCoord(landing_pad_x, landing_pad_y);
                  model.updateViews();
                  repaint();
              }
            }

            //once pressed, store the value
            @Override
            public void mousePressed(MouseEvent e) {
              boolean hitpad = false;//used for avoid circle and pad move together
              //if I pressed on the landing pad
              if(in_landingpad(e.getX(), e.getY())){
                hitpad = true;
                pad_startDragging.x = e.getX();
                pad_startDragging.y = e.getY();
                //use a offset vector to remember the relative distance between
                //mouse press position and landing pad coordination
                pad_offset.x = e.getX() - landing_pad_x;
                pad_offset.y = e.getY() - landing_pad_y;
              }else{
                pad_startDragging.x = -1;
                pad_startDragging.y = -1;
              }

              //if I pressed on one of the peaks
              int index_for_peak = in_which_peak(e.getX(), e.getY());
              if (index_for_peak != -1 && !hitpad) {
                peak_startDragging.x = index_for_peak;
                peak_startDragging.y = e.getY();
                peak_offset.x = index_for_peak;
                peak_offset.y = e.getY() - peak_yPoints[index_for_peak];
              }else{  //-1 means I did not pressed on any of peaks
                peak_startDragging.x = -1;
                peak_startDragging.y = -1;
              }
            }

            //once released, delete the value
            @Override
            public void mouseReleased(MouseEvent e) {
              //landing pad
              //if the last time I pressed in the landing pad
              if(pad_startDragging.x >= 0 && pad_startDragging.y >= 0){
                //if I did drag
                if(e.getX() - pad_offset.x!= pad_startDragging.x && e.getY() - pad_offset.y != pad_startDragging.y){
                  pad_stopDragging.x = e.getX() - pad_offset.x;
                  pad_stopDragging.y = e.getY() - pad_offset.y;
                  System.out.println("this is a success pad drag");
                  pad_moved_offset.x = pad_stopDragging.x - pad_startDragging.x;
                  pad_moved_offset.y = pad_stopDragging.y - pad_startDragging.y;
                }
              }else{
                pad_stopDragging.x = -1;
                pad_stopDragging.y = -1;
                System.out.println("Not a pad drag");
              }

              //peaks
              //if I pressed on a peak
              if(peak_startDragging.x >= 0){
                //if I did drag
                if(e.getY() != peak_startDragging.y){
                  peak_stopDragging.y = e.getY();
                  System.out.println("this is a success peak drag");
                  peak_moved_offset.y = peak_stopDragging.y - peak_startDragging.y;
                }
              }else{
                peak_stopDragging.x = -1;
                peak_stopDragging.y = -1;
                System.out.println("Not a peak drag");
              }
            }

        });

        this.addMouseMotionListener(new MouseAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
              if(pad_startDragging.x >= 0 && pad_startDragging.y >= 0){
                int potential_x = e.getX() - (int)pad_offset.x;
                int potential_y = e.getY() - (int)pad_offset.y;
                if(!model.outside_the_world(potential_x, potential_y, landing_pad_w, landing_pad_h)){
                  landing_pad_x = potential_x;
                  landing_pad_y = potential_y;
                  model.setLandingPadCoord(landing_pad_x, landing_pad_y);
                  model.updateViews();
                  repaint();
                }else{
                  System.out.println("boom!");
                }
              }

              if(peak_startDragging.x >= 0){
                int potential_y = e.getY() - (int)peak_offset.y;
                if(model.outside_the_world(0, potential_y, 0, 0) == false){
                  peak_yPoints[(int)peak_startDragging.x] = potential_y;
                  model.updateViews();
                  repaint();
                }
              }
            }
        });

    }

    @Override
    public void update(Observable o, Object arg) {

    }
    //hittest for landding pad
    public boolean in_landingpad(double x, double y){
      if(x >= landing_pad_x && x <= landing_pad_x + landing_pad_w){
        if(y >= landing_pad_y && y <= landing_pad_y + landing_pad_h){
          return true;
        }
      }
      return false;
    }

    //hittest for peaks. -1 for miss, otherwise return index
    public int in_which_peak(int x, int y){
      //estimate which peak according to x, avoid O(n)
      double i = x / (model.getWorldBounds().getWidth() / 19);
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
}
