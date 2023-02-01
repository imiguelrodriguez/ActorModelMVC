package view;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressBarRenderer() {
        super(0, 1000);
        setValue(0);
        setStringPainted(true);

    }
    @Override
    public String getString() {
        int value = getValue();
        return value +"/"+ getMaximum();

    }
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        int progress = 0;
        if (value instanceof Float) {
            progress = Math.round((Float) value);
        } else if (value instanceof Integer) {
            progress = (Integer) value;
        }
        setValue(progress);
        return this;
    }
}