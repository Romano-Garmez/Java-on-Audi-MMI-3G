package org.placelab.util.swt;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.placelab.collections.ArrayList;
import org.placelab.collections.Iterator;

public class QuestionBox extends Dialog {

    protected ArrayList questions;
    protected ArrayList responseBoxes;
    protected boolean submit = false;
    protected String message;
    protected Hashtable answers;
    protected String[] buttons;
    protected int buttonChosen = -1;
    
    public static class QuestionPackage {
        public String question;
        public String[] defaultAnswers;
        public int length;
        public boolean isPassword;
        public boolean isSelected;
        public boolean isEditable;
        
        /**
         * This constructor will generate a question text box
         * @param question the question to ask above the box
         * @param defaultAnswer the default answer, if any
         * @param length the length of characters allowed in the box
         * @param isPassword use a secure text entry box
         */
        public QuestionPackage(String question,
                String defaultAnswer, int length, boolean isPassword) {
            this.question = question;
            if(defaultAnswer == null) defaultAnswer = "";
            this.defaultAnswers = new String[] { defaultAnswer };
            this.length = length;
            this.isPassword = isPassword;
        }
        
        /**
         * This constructer will generate a question with a drop
         * down box containing the given answers.
         * @param question the question to ask above the box
         * @param answers the answers to be contained in the drop down
         * @param isEditable whether or not to allow the user to enter
         * 	a custom answer.
         */
        public QuestionPackage(String question,
                		String[] answers,
                		boolean isEditable) {
            this.question = question;
            this.defaultAnswers = answers;
        }
        
        /**
         * This constructor will generate a Yes/No question with
         * a checkbox
         */
        public QuestionPackage(String question,
                boolean defaultSelection) {
            this.question = question;
            this.isSelected = defaultSelection;
        }
        
        // returns a code corresponding to the box type
        // 1: text box
        // 2: drop down
        // 3: checkbox
        public int boxType() {
            if(this.defaultAnswers == null) return 3;
            else if(this.defaultAnswers.length == 1) return 1;
            else return 2;
        }
    }
    
    /**
     * Questions are a list of QuestionBox.QuestionPackages that you want to ask
     *
     */
    public QuestionBox(Shell parent, ArrayList questions, String msg) {
        super(getParent(parent), 0);
        this.questions = questions;
        this.message = msg;
        buttons = new String[] { "Cancel", "Submit" };
    }
    
    /**
     * Before calling open(), you may set the buttons names.
     * The last button in the array will be the default button.
     * Call getButtonChosen after open() returns to get the
     * index of the chosen button.
     */
    public void setButtons(String[] buttonNames) {
        this.buttons = buttonNames;
    }
    
    public int getButtonChosen() {
        return buttonChosen;
    }
    
    private static Shell getParent(Shell parent) {
        if(parent == null) {
            // make a temporary parent for the dialog
            Display display = Display.getCurrent();
            parent = new Shell(display, 0);
        }
        return parent;
    }
    
    /**
     * Returns a Hashtable where questions key to answers.
     */
    public Object open() {
        int width = 0, height = 0;
        Shell parent = getParent();
        final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(message);
        final ArrayList responseBoxes = new ArrayList(questions.size());
        RowLayout layout = new RowLayout();
        layout.type = SWT.VERTICAL;
        layout.marginLeft = 10;
        layout.marginRight = 5;
        shell.setLayout(layout);
        //Group qGroup = new Group(shell, SWT.SHADOW_NONE);
        //RowLayout gLayout = new RowLayout();
        //gLayout.type = SWT.VERTICAL;
        for(Iterator i = questions.iterator(); i.hasNext(); ) {
            QuestionPackage p = (QuestionPackage)i.next();
            String q = p.question;
            Label qLabel = new Label(shell, SWT.HORIZONTAL | SWT.LEFT);
            qLabel.setText(q);
            Control box = null;
            if(p.boxType() == 1) {
                // text box
                String a = p.defaultAnswers[0];
	            box = new Text(shell, SWT.SINGLE);
	            ((Text) box).setText(a);
	            if(p.isPassword) ((Text) box).setEchoChar('*');
            } else if(p.boxType() == 2) {
                // drop down box
                box = new Combo(shell, SWT.NONE);
                ((Combo)box).setItems(p.defaultAnswers);
                ((Combo)box).select(0);
            } else {
                box = new Button(shell, SWT.CHECK);
                ((Button)box).setSelection(p.isSelected);
            }
            box.setData("QuestionPackage", p);
            responseBoxes.add(box);
        }
        Composite buttonComposite = new Composite(shell, 0);
        GridLayout buttonsLayout = new GridLayout();
        buttonsLayout.numColumns = buttons.length;
        buttonsLayout.marginWidth = 0;
        buttonComposite.setLayout(buttonsLayout);
        for(int i = 0; i < buttons.length; i++) {
            Button b = new Button(buttonComposite, SWT.PUSH);
            b.setText(buttons[i]);
            final int index = i;
            b.addSelectionListener(new SelectionListener() {
	            public void widgetSelected(SelectionEvent e) {
	                QuestionBox.this.buttonChosen = index;
	                answers = new Hashtable();
	                for(Iterator i = responseBoxes.iterator(); i.hasNext();) {
	                    Control answerBox = (Control)i.next();
	                    QuestionPackage p = 
	                        (QuestionPackage)answerBox.getData("QuestionPackage");
	                    Object answer = null;
	                    if(p.boxType() == 1) {
	                        answer = ((Text)answerBox).getText();
	                    } else if(p.boxType() == 2) {
	                        Combo c = (Combo)answerBox;
	                        int index = c.getSelectionIndex();
	                        if(index >= 0 && index < c.getItemCount()) {
	                            answer = c.getItem(index);
	                        } else {
	                            answer = "";
	                        }
	                    } else {
	                        answer = new Boolean(((Button)answerBox).getSelection());
	                    }
	                    answers.put(p.question, answer);
	                }
	                shell.close();
	            }
	            public void widgetDefaultSelected(SelectionEvent e) { }
            });
            if(i == buttons.length - 1) shell.setDefaultButton(b);
        }
        shell.pack();
        shell.open();
        Display display = parent.getDisplay();
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return answers;
    }
    
}
