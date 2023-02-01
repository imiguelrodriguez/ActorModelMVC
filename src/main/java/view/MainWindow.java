package view;

import actors.Actor;
import control.Control;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainWindow extends JFrame {
    private JLabel titleLbl;
    private JPanel mainPanel;
    private JScrollPane tablePanel;
    private JTable actorTable;
    private JPanel buttonPanel;
    private JButton addActorBtn;
    private JButton getStatsBtn;
    private JButton runRingAppBtn;
    private JButton sendMessagesButton;
    private JButton pingPongAppButton;
    private JButton removeActorButton;
    private DefaultTableModel model;
    private ArrayList<Thread> threads = new ArrayList<>();

    public MainWindow () {
        this.setTitle("ActorModelMVC");
        this.setSize(700, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        String[] col = {"Name","Type","Queue occupancy"};
        this.model = new DefaultTableModel(null, col) {
                @Override
                public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        this.actorTable.setModel(model);
        this.actorTable.getTableHeader().setReorderingAllowed(false);
        this.actorTable.getColumnModel().getColumn(2).setCellRenderer(new ProgressBarRenderer());

        JTableHeader header = this.actorTable.getTableHeader();
        header.setBackground(new Color(178, 179, 199));
        this.addActorBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JTextField actorName = new JTextField();
                JComboBox<String> options = new JComboBox<>();
                String[] items = {"HelloWorldActor", "InsultActor", "ActorImp"};
                Arrays.stream(items).forEach(options::addItem);
                Object[] message = {
                        "Actor name:", actorName,
                        "Actor Type:", options
                };

                int option = JOptionPane.showConfirmDialog(null, message, "Add new actor", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        Control.addActor(actorName.getText(), (Actor) Class.forName((String) "actors."+options.getSelectedItem()).getConstructor().newInstance());
                        addEntry(actorName.getText(), (String) options.getSelectedItem());
                    } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException |
                             InstantiationException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                System.out.println(Control.getActorContext().lookup(actorName.getText()));
        }});

        removeActorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = actorTable.getSelectedRow();
                Control.quitActor((String) actorTable.getValueAt(row, 0));
                removeEntry(row);
            }
        });

        this.runRingAppBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, 1000, 10);
                JSpinner spinner = new JSpinner(sModel);

                int option = JOptionPane.showOptionDialog(null, spinner, "Enter the number of actors for the ring app", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (option == JOptionPane.OK_OPTION)
                {
                    try {
                        Control.runRingApp((Integer) spinner.getValue());
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });

        this.sendMessagesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int numberOfActors = Control.getActorContext().getAllActors().size();
                if (numberOfActors > 0) {
                    JComboBox<String> from = new JComboBox<>();
                    JComboBox<String> options = new JComboBox<>();
                    SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, 10000, 5);
                    JSpinner spinner = new JSpinner(sModel);
                    Set<String> actors = Control.getActorContext().getNames();
                    actors.forEach(options::addItem);
                    from.addItem("null");
                    actors.forEach(from::addItem);

                    Object[] message = {
                            "From", from,
                            "To:", options,
                            "How many messages?", spinner
                    };

                    int option = JOptionPane.showConfirmDialog(null, message, "Send messages", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                       String name = (String) options.getSelectedItem();
                       String frm = (String) from.getSelectedItem();
                       int nMessages = (int) spinner.getValue();
                       Control.sendMessages(frm, name, nMessages);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null, "There aren't actors in the ActorContext!",
                            "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.getStatsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int column = 0;
                int row = actorTable.getSelectedRow();
                if (row>=0) {
                    String name = actorTable.getModel().getValueAt(row, column).toString();
                    new StatsWindow(name);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Please, select an actor to view its stats.",
                            "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pingPongAppButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Control.runPingPongApp();
            }
        });

        this.setContentPane(this.mainPanel);
        Control.setView(this);
        setVisible(true);
    }

    public void addEntry(String name, String type) {
        this.model.addRow(new Object[] {name, type});
        int row = this.model.getRowCount()-1;
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    int value = Control.getQueueOccupancy(name);
                    try {
                        actorTable.getModel().setValueAt(value, row, 2);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        });
        th.start();


    }

    public void removeEntry(int row) {
        this.model.removeRow(row);
    }

}
