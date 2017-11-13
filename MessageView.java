import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class MessageView extends JPanel implements Observer {

    GameModel model;
    // status messages for game
    JLabel fuel = new JLabel("Fuel");
    JLabel speed = new JLabel("Speed");
    JLabel message = new JLabel("Message");

    public MessageView(GameModel model) {
        this.model = model;
        model.addObserver(this);
        model.ship.addObserver(this);

        // want the background to be black
        setBackground(Color.BLACK);

        setLayout(new FlowLayout(FlowLayout.LEFT));
        //Integer.toString(quantity)
        fuel.setText("Fuel:" + Integer.toString((int)model.ship.getFuel()));
        speed.setText("Speed:" + Integer.toString((int)model.ship.getSpeed()));
        message.setText("(paused)");

        add(fuel);
        add(speed);
        add(message);

        for (Component c: this.getComponents()) {
            c.setForeground(Color.WHITE);
            c.setPreferredSize(new Dimension(100, 20));
        }
    }


    @Override
    public void update(Observable o, Object arg) {
      //set fuel
      int fuel_val = (int)model.ship.getFuel();
      fuel.setText("Fuel:" + Integer.toString(fuel_val));
      if(fuel_val < 10){
          fuel.setForeground (Color.red);
      }
      //set speed
      double speed_val = model.ship.getSpeed();
      speed.setText("Speed:" + Double.toString(speed_val));
      if(speed_val < model.getSafeLandingSpeed()){
        speed.setForeground (Color.green);
      }else{
        speed.setForeground (Color.white);
      }

      if(model.landed){
        message.setText("LANDED!");
      }else if(model.crashed){
        message.setText("CRASH");
      }else if(model.ship.isPaused()){
        message.setText("(paused)");
      }else{
        message.setText("");
      }
    }
}
