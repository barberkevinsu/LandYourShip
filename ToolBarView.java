import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

// the edit toolbar
public class ToolBarView extends JPanel implements Observer {

    JButton undo = new JButton("Undo");
    JButton redo = new JButton("Redo");

    public ToolBarView(GameModel model) {

        setLayout(new FlowLayout(FlowLayout.LEFT));

        // prevent buttons from stealing focus
        undo.setFocusable(false);
        redo.setFocusable(false);

        add(undo);
        add(redo);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
