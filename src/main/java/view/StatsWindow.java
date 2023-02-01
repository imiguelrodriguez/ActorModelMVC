package view;

import actors.ActorImp;
import control.Control;

import javax.swing.*;

public class StatsWindow extends JFrame {
    private JPanel mainPanel;
    private JLabel titleLbl;
    private JLabel processedLbl;
    private JLabel sentLbl;
    private JLabel processedValue;
    private JLabel sentValue;

    public StatsWindow(String name) {
        this.setTitle("Stats window");
        this.titleLbl.setText(this.titleLbl.getText()+ " " + name);
        this.setSize(500, 500);

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    processedValue.setText(Control.getProcessedMessages(name));
                    sentValue.setText(Control.getSentMessages(name));
                }
            }
        });

        th.start();

        this.setContentPane(this.mainPanel);
        setVisible(true);
    }

}
