import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

//enhanced:
//Add fuel function:
//  There will be a random number (1 - 5) of fuel tanks located in the upper sky.
//  Each fuel tank has ramdom amount of fuel from 1 to 10.
//  After ship moved to the tank, the tank will disappear, the fuel will be adde to the ship.
//note: A3Enhanced has exactly the same content as A3Basic except these comments.
//      You can just run A3Basic to check the enhanced functionality.

public class A3Enhanced extends JPanel {

    A3Enhanced() {
        // create the model
        GameModel model = new GameModel(60, 700, 200, 20);

        JPanel playView = new PlayView(model);
        JPanel editView = new EditView(model);
        editView.setPreferredSize(new Dimension(700, 200));

        // layout the views
        setLayout(new BorderLayout());

        add(new MessageView(model), BorderLayout.NORTH);

        // nested Border layout for edit view
        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BorderLayout());
        editPanel.add(new ToolBarView(model), BorderLayout.NORTH);
        editPanel.add(editView, BorderLayout.CENTER);
        add(editPanel, BorderLayout.SOUTH);

        // main playable view will be resizable
        add(playView, BorderLayout.CENTER);

        // for getting key events into PlayView
        playView.requestFocusInWindow();

    }

    public static void main(String[] args) {
        // create the window
        JFrame f = new JFrame("A3Basic"); // jframe is the app window
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 600); // window size
        f.setContentPane(new A3Enhanced()); // add main panel to jframe
        f.setVisible(true); // show the window
    }
}
