import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

// the actual game view
public class PlayView extends JPanel implements Observer {

    public PlayView(GameModel model) {

        // needs to be focusable for keylistener
        setFocusable(true);

        // want the background to be black
        setBackground(Color.BLACK);

    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
