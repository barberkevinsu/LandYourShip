import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Observable;
import java.util.Observer;
//added import
import javax.vecmath.*;
import java.util.Random;
import java.lang.Math;

// the editable view of the terrain and landing pad
public class EditView extends JPanel implements Observer {

    //interface parts
    private GameModel model;

    /***********************************************/

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Point2d landing_pad_coord = model.getPadCoord();
        Point2d landing_pad_size = model.getPadSize();
        int[] peak_xPoints = model.getPeakXPoints();
        int[] peak_yPoints = model.getPeakYPoints();

        //draw polygon
        g.setColor(Color.DARK_GRAY);
        g.fillPolygon(peak_xPoints, peak_yPoints, 22);

        //draw each circle
        g.setColor(Color.BLACK);
        for(int i=1; i<21; i++){
          g.drawOval(peak_xPoints[i]-15, peak_yPoints[i]-15, 29, 29);//documentation: width+1, height+1
        }

        //draw landing pad
        g.setColor(Color.RED);
        g.drawRect((int)landing_pad_coord.x, (int)landing_pad_coord.y, (int)landing_pad_size.x, (int)landing_pad_size.y);
        g.fillRect((int)landing_pad_coord.x, (int)landing_pad_coord.y, (int)landing_pad_size.x, (int)landing_pad_size.y);
    }

    public EditView(GameModel model) {
        this.model = model;
        model.addObserver(this);

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
                  int potential_x = e.getX() - (int)(model.getPadSize().x/2);
                  int potential_y = e.getY() - (int)(model.getPadSize().y/2);
                  int old_x = (int)model.getPadCoord().x;
                  int old_y = (int)model.getPadCoord().y;
                  model.setLandingPadCoord(potential_x, potential_y);
                  model.setPadUndoable(potential_x - old_x, potential_y - old_y);
                  model.setChangedAndNotify();
              }
            }

            //once pressed, store the value
            @Override
            public void mousePressed(MouseEvent e) {
              boolean hitpad = false;//used for avoid circle and pad move together
              //if I pressed on the landing pad
              if(model.in_landingpad(e.getX(), e.getY())){
                hitpad = true;
                //remember where it started to drag
                pad_startDragging.x = model.getPadCoord().x;
                pad_startDragging.y = model.getPadCoord().y;
                //use a offset vector to remember the relative distance between
                //mouse press position and landing pad coordination
                pad_offset.x = e.getX() - model.getPadCoord().x;
                pad_offset.y = e.getY() - model.getPadCoord().y;
              }else{
                pad_startDragging.x = -1;
                pad_startDragging.y = -1;
              }

              //if I pressed on one of the peaks
              int index_for_peak = model.in_which_peak(e.getX(), e.getY());
              if (index_for_peak != -1 && !hitpad) {
                //remember where it started to drag
                peak_startDragging.x = index_for_peak;
                peak_startDragging.y = model.getPeakValue(index_for_peak);
                peak_offset.x = index_for_peak;
                peak_offset.y = e.getY() - model.getPeakValue(index_for_peak);
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
                  //set change in x and y, then set undoable
                  int change_in_x = (int)(pad_stopDragging.x - pad_startDragging.x);
                  int change_in_y = (int)(pad_stopDragging.y - pad_startDragging.y);
                  model.setPadUndoable(change_in_x, change_in_y);
                  //only set undoable when there is a "real drag"
                }else{
                  pad_stopDragging.x = -1;
                  pad_stopDragging.y = -1;
                  System.out.println("Not a pad drag");
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
                  peak_stopDragging.y = e.getY() - peak_offset.y;
                  System.out.println("this is a success peak drag");
                  //set change in y, then set undoable
                  int change_in_y = (int)(peak_stopDragging.y - peak_startDragging.y);
                  model.setPeakUndoable((int)peak_offset.x, change_in_y);
                }
              }else{
                peak_stopDragging.x = -1;
                peak_stopDragging.y = -1;
                System.out.println("Not a peak drag");
              }

              model.setChangedAndNotify();
            }

        });

        this.addMouseMotionListener(new MouseAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
              if(pad_startDragging.x >= 0 && pad_startDragging.y >= 0){
                int potential_x = e.getX() - (int)pad_offset.x;
                int potential_y = e.getY() - (int)pad_offset.y;
                if(!model.outside_the_world(potential_x, potential_y, (int)model.landing_pad_size.x, (int)model.landing_pad_size.y)){
                  model.setLandingPadCoord(potential_x, potential_y);
                  model.setChangedAndNotify();
                }else{
                  System.out.println("boom!");
                }
              }

              if(peak_startDragging.x >= 0){
                int potential_x = (int)peak_offset.x;
                int potential_y = e.getY() - (int)peak_offset.y;
                if(model.outside_the_world(0, potential_y, 0, 0) == false){
                  model.setPeakValue(potential_x, potential_y);
                  model.setChangedAndNotify();
                }
              }
            }
        });

    }

    @Override
    public void update(Observable o, Object arg) {
      repaint();
    }

}
