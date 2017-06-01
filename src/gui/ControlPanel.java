package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import java.util.Properties;

/**
 * Created by baislsl on 17-5-11.
 */
public class ControlPanel extends Composite {

    private Properties props;
    private Button history, search, bookList;
    private ListPanel listPanel;
    private final static String backgroundPath = "res/pictures/background/control_panel_background.jpg";
    private final static String conditionPicPath = "res/pictures/button/book.png";
    private final static String searchPicPath = "res/pictures/button/search.png";
    private final static String historyPicPath = "res/pictures/button/history.png";

    public ControlPanel(Composite parent, Properties props) {
        super(parent, SWT.BORDER);
        this.props = props;
        init();
    }

    private void init() {
        this.setBackgroundImage(new Image(getDisplay(), backgroundPath));
        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.marginWidth = 40;
        rowLayout.marginHeight = 100;
        rowLayout.spacing = 50;
        rowLayout.pack = false;
        this.setLayout(rowLayout);
        bookList = new Button(this, SWT.PUSH);
        bookList.setImage(new Image(getDisplay(), conditionPicPath));
        bookList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                listPanel.turnBookFrame();
            }
        });
        search = new Button(this, SWT.PUSH);
        // search.setText(props.getProperty("search"));
        search.setImage(new Image(getDisplay(), searchPicPath));
        search.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                listPanel.turnSearchFrame();
            }
        });
        history = new Button(this, SWT.PUSH);
        // history.setText(props.getProperty("history"));
        history.setImage(new Image(getDisplay(), historyPicPath));
        history.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                listPanel.turnHistory();
            }
        });

        GridData data = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
        this.setData(data);
        this.setSize(this.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public void setListPanel(ListPanel listPanel) {
        this.listPanel = listPanel;
    }
}

