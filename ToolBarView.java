import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

// the edit toolbar
public class ToolBarView extends JPanel implements Observer {

    GameModel model;
    JButton undo = new JButton("Undo");
    JButton redo = new JButton("Redo");

    public ToolBarView(GameModel model) {

        this.model = model;
        model.addObserver(this);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        // prevent buttons from stealing focus
        undo.setFocusable(false);
        redo.setFocusable(false);

        add(undo);
        add(redo);

        updateButtons();

        undo.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
              model.undo();
            }
        });

        redo.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
              model.redo();
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
      updateButtons();
    }

    public void updateButtons(){
      if(model.canUndo()){
        undo.setEnabled(true);
      }else{
        undo.setEnabled(false);
      }

      if(model.canRedo()){
        redo.setEnabled(true);
      }else{
        redo.setEnabled(false);
      }
    }
}
