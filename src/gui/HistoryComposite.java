package gui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import util.Action;
import util.History;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.Properties;

/**
 * Created by baislsl on 17-4-28.
 */
public class HistoryComposite extends Composite {
    private History history;
    private Properties props;

    HistoryComposite(Composite parent, History history, Properties props) {
        super(parent, SWT.BORDER | SWT.FILL);
        this.history = history;
        this.props = props;
        this.setLayout(new GridLayout(5, false));
        init();
    }

    private void init() {
        Label action = new Label(this, SWT.BORDER);
        action.setText(props.getProperty("action." + history.action.name().toLowerCase()));
        action.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        Color color;
        switch (history.action) {
            case BORROW:
                color = getDisplay().getSystemColor(SWT.COLOR_YELLOW);
                break;
            case RETURN:
                color = getDisplay().getSystemColor(SWT.COLOR_GREEN);
                break;
            default:    // RENEW
                color = getDisplay().getSystemColor(SWT.COLOR_RED);
                break;
        }
        this.setBackground(color);
        addLabel(color, props.getProperty("user.id"), history.user_id);
        addLabel(color, props.getProperty("book.title"), history.book.title);
        addLabel(color, props.getProperty("book.id"), history.book.id);
        addLabel(color, props.getProperty("history.date"), history.date);
    }

    private void addLabel(Color color, String name, String text) {
        Label nameLabel = new Label(this, SWT.NONE);
        nameLabel.setText(name);
        nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        nameLabel.setBackground(color);

        Label textLabel = new Label(this, SWT.NONE);
        textLabel.setText(text);
        textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        textLabel.setBackground(color);
    }
}
