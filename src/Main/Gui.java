package Main;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame{

    private JCheckBox isMemberbox;
    private JCheckBox deleteFirst;
    private JCheckBox hopWorlds;


    public Gui(Main reference){
        init(reference);
    }

    public void init(Main reference) {

        JButton startBtn = new JButton("Start script");
        JTextField text_field = new JTextField();
        isMemberbox = new JCheckBox("Member?");
        hopWorlds = new JCheckBox("Hop Worlds?");
        JComboBox<Integer> speed_selection = new JComboBox<>(new Integer[]{100, 200, 300, 400, 500});



        this.setTitle("Select option");
        this.setLayout(new BorderLayout());
        this.setSize(300, 150);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.add(text_field, BorderLayout.CENTER);
        this.add(isMemberbox, BorderLayout.NORTH);
        this.add(startBtn, BorderLayout.PAGE_END);
        this.add(hopWorlds, BorderLayout.EAST);
        this.add(speed_selection, BorderLayout.LINE_START);


        this.setVisible(true);

        startBtn.addActionListener(e -> {
            if(isMemberbox.isSelected()){
                reference.member = true;
            }
            if(hopWorlds.isSelected()){
                reference.hop = true;
            }
            reference.preset_message = text_field.getText();
            reference.isStarted = true;
            setVisible(false);
        });

        speed_selection.addActionListener(
                e -> reference.typing_speed = (int) speed_selection.getSelectedItem());

    }
}
