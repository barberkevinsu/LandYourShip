import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.vecmath.*;
import java.lang.Math;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


// the actual game view
public class PlayView extends JPanel implements Observer {

    GameModel model;

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        int center_x = (int)this.getWidth()/2;
        int center_y = (int)this.getHeight()/2;
        int ship_width = model.ship_width;
        int ship_height = model.ship_height;

        //my ship's center position
        Point2d ship_position = model.ship.getPosition();

        //scale 3
        g2.translate(ship_position.x, ship_position.y);
        g2.translate(center_x - ship_position.x, center_y - ship_position.y);
        g2.scale(3, 3);
        g2.translate(-ship_position.x, -ship_position.y);


        Point2d landing_pad_coord = model.getPadCoord();
        Point2d landing_pad_size = model.getPadSize();
        int[] peak_xPoints = model.getPeakXPoints();
        int[] peak_yPoints = model.getPeakYPoints();

        //draw polygon
        g.setColor(Color.DARK_GRAY);
        g.fillPolygon(peak_xPoints, peak_yPoints, 22);

        //draw ship
        g.setColor(Color.BLUE);
        g.fillRect((int)ship_position.x - ship_width/2, (int)ship_position.y - ship_height/2, ship_width, ship_height);

        //draw landing pad
        g.setColor(Color.RED);
        g.drawRect((int)landing_pad_coord.x, (int)landing_pad_coord.y, (int)landing_pad_size.x, (int)landing_pad_size.y);
        g.fillRect((int)landing_pad_coord.x, (int)landing_pad_coord.y, (int)landing_pad_size.x, (int)landing_pad_size.y);
    }

    public PlayView(GameModel model) {
        setBackground(Color.LIGHT_GRAY);
        this.model = model;
        model.addObserver(this);

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
              model.move_my_ship(e.getKeyChar());
            }
        });
        // needs to be focusable for keylistener
        setFocusable(true);

        // want the background to be black
        setBackground(Color.LIGHT_GRAY);

    }


    @Override
    public void update(Observable o, Object arg) {
      repaint();
    }

}
