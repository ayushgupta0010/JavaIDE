import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public class JavaIDE {
	private static int findLastNonWordChar(String text, int index) {
		while (--index >= 0) {
			if (String.valueOf(text.charAt(index)).matches("\\W")) {
				break;
			}
		}
		return index;
	}

	private static int findFirstNonWordChar(String text, int index) {
		while (index < text.length()) {
			if (String.valueOf(text.charAt(index)).matches("\\W")) {
				break;
			}
			index++;
		}
		return index;
	}

	static final StyleContext cont = StyleContext.getDefaultStyleContext();
	static final AttributeSet attrKeywords = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
			new Color(214, 30, 19));
	static final AttributeSet attrSpecials = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
			new Color(66, 103, 178));
	static final AttributeSet attrExceptions = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
			new Color(6, 78, 112));
	static final AttributeSet attrOrdinary = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
			Color.BLACK);
	static DefaultStyledDocument doc = new DefaultStyledDocument() {
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
			super.insertString(offset, str, a);

			String text = getText(0, getLength());
			int before = findLastNonWordChar(text, offset);
			if (before < 0)
				before = 0;
			int after = findFirstNonWordChar(text, offset + str.length());
			int wordL = before;
			int wordR = before;

			while (wordR <= after) {
				if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
					if (text.substring(wordL, wordR).matches("(\\W)*(" + keywords + ")")) {
						setCharacterAttributes(wordL, wordR - wordL, attrKeywords, false);
					} else if (text.substring(wordL, wordR).matches("(\\W)*(" + specials + ")")) {
						setCharacterAttributes(wordL + 1, wordR - wordL - 1, attrSpecials, false);
					} else if (text.substring(wordL, wordR).matches("(\\W)*(" + exceptions + ")")) {
						setCharacterAttributes(wordL, wordR - wordL, attrExceptions, false);
					} else {
						setCharacterAttributes(wordL, wordR - wordL, attrOrdinary, false);
					}
					wordL = wordR;
				}
				wordR++;
			}
		}

		public void remove(int offs, int len) throws BadLocationException {
			super.remove(offs, len);

			String text = getText(0, getLength());
			int before = findLastNonWordChar(text, offs);
			if (before < 0)
				before = 0;
			int after = findFirstNonWordChar(text, offs);

			if (text.substring(before, after).matches("(\\W)*(" + keywords + ")")) {
				setCharacterAttributes(before, after - before, attrKeywords, false);
			} else if (text.substring(before, after).matches("(\\W)*(" + exceptions + ")")) {
				setCharacterAttributes(before, after - before, attrExceptions, false);
			} else if (text.substring(before, after).matches("(\\W)*(" + specials + ")")) {
				setCharacterAttributes(before, after - before, attrSpecials, false);
			} else {
				setCharacterAttributes(before, after - before, attrOrdinary, false);
			}
		}
	};

	static JFrame frame = new JFrame("Java IDE");

	static JTextPane editorArea = new JTextPane(doc);
	static JTextPane outputArea = new JTextPane();

	static JButton newButton = new JButton("New File");
	static JButton openButton = new JButton("Open File");
	static JButton saveButton = new JButton("Save File");
	static JButton compileButton = new JButton("Compile");
	static JButton runButton = new JButton("Run");

	static JPanel buttonsPanel = new JPanel();

	static JScrollPane editorPane = new JScrollPane(editorArea);
	static JScrollPane outputPane = new JScrollPane(outputArea);

	static JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPane, outputPane);

	static JFileChooser fileChooser = new JFileChooser();

	static String filePath = "";

	private static JLabel createLabel(String labelName) {
		JLabel label = new JLabel(labelName);
		label.setFont(new Font("Comic Sans MS", Font.BOLD + Font.ITALIC, 25));
		label.setForeground(Color.BLACK);
		return label;
	}

	static final String auto_text = "import java.util.Scanner;\npublic class *class_name*\n{\n\tpublic static void main(String args[])\n\t{\n\t\tScanner sc = new Scanner(System.in);\n\t\tSystem.out.println(\" *Your message* \");\n\t}\n}";

	static final String keywords = "abstract|continue|for|new|switch|assert|default|goto|package|synchronized|boolean|do|if|private|this|break|double|implements|protected|throw|byte|else|import|public|throws|case|enum|instanceof|return|transient|catch|extends|int|short|try|char|final|interface|static|void|class|finally|long|strictfp|volatile|const|float|native|super|while|String";

	static final String specials = "System|Scanner|Math|out|print|println|StringBuffer|StringBuilder|Character|Integer|Short|Byte|Long|Double|Float|BufferedReader|BufferedWriter|InputStreamReader|InputStreamWriter|OutputStreamReader|OutputStreamWriter|File|FileWriter|FileReader|Date|Arrays|PrintWriter|FileInputStream|FileOutputStream|DataInputStream|DataOutputStream|StringTokenizer|true|false";

	static final String exceptions = "Exception|InputMismatchException|ArithmeticException|ArrayIndexOutOfBoundsException|ClassNotFoundException|FileNotFoundException|IOException|InterruptedException|NoSuchFieldException|NoSuchMethodException|NullPointerException|NumberFormatException|RuntimeException|StringIndexOutOfBoundsException|StackOverflowError|EOFException";

	public static void main(String args[]) {
		CompoundUndoManager um = new CompoundUndoManager(editorArea);
		TextLineNumber tln = new TextLineNumber(editorArea);

		editorArea.setText(auto_text);

		tln.setBorderGap(15);
		tln.setCurrentLineForeground(Color.BLACK);
		tln.setDigitAlignment(TextLineNumber.LEFT);
		tln.setMinimumDisplayDigits(5);
		tln.setUpdateFont(true);
		editorPane.setRowHeaderView(tln);

		newButton.setPreferredSize(new Dimension(125, 45));
		openButton.setPreferredSize(new Dimension(125, 45));
		saveButton.setPreferredSize(new Dimension(125, 45));
		compileButton.setPreferredSize(new Dimension(125, 45));
		runButton.setPreferredSize(new Dimension(125, 45));

		editorPane.setColumnHeaderView(createLabel("Editor - Untitled"));
		outputPane.setColumnHeaderView(createLabel("Output"));

		splitPane.setResizeWeight(0.2);
		editorArea.setFont(new Font("Comic Sans MS", Font.BOLD + Font.ITALIC, 27));
		outputArea.setFont(new Font("Comic Sans MS", Font.BOLD + Font.ITALIC, 25));

		outputArea.setEditable(false);

		buttonsPanel.add(newButton);
		buttonsPanel.add(openButton);
		buttonsPanel.add(saveButton);

		buttonsPanel.add(compileButton);
		buttonsPanel.add(runButton);

		frame.add(splitPane, BorderLayout.CENTER);
		frame.add(buttonsPanel, BorderLayout.PAGE_END);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		editorArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_Z) && e.isControlDown()) {
					try {
						um.undo();
					} catch (CannotUndoException ex) {
						// Do nothing
					}

				} else if ((e.getKeyCode() == KeyEvent.VK_Y) && e.isControlDown()) {
					try {
						um.redo();
					} catch (CannotRedoException ex) {
						// Do nothing
					}
				} else if ((e.getKeyCode() == KeyEvent.VK_N) && e.isControlDown()) {
					clearEditor();
				} else if ((e.getKeyCode() == KeyEvent.VK_O) && e.isControlDown()) {
					filePath = open();
				} else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown()) {
					save(filePath);

				} else if ((e.getKeyCode() == KeyEvent.VK_C) && e.isControlDown() && e.isShiftDown()) {
					compile(filePath);

				} else if ((e.getKeyCode() == KeyEvent.VK_R) && e.isControlDown() && e.isShiftDown()) {
					run(filePath);
				}
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		newButton.addActionListener((ActionEvent) -> {
			clearEditor();
		});

		openButton.addActionListener((ActionEvent) -> {
			filePath = open();
		});

		saveButton.addActionListener((ActionEvent) -> {
			save(filePath);
		});

		compileButton.addActionListener((ActionEvent) -> {
			compile(filePath);
		});

		runButton.addActionListener((ActionEvent) -> {
			run(filePath);
		});

	}

	public static void clearEditor() {
		filePath = "";
		editorArea.setText(auto_text);
		editorPane.setColumnHeaderView(createLabel("Editor - Untitled"));
	}

	public static void newFile() {
		int option = fileChooser.showSaveDialog(frame);
		String path = "";
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				FileWriter file = new FileWriter(fileChooser.getSelectedFile());
				file.write(editorArea.getText());
				path = fileChooser.getSelectedFile().getPath();
				file.close();
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "An error occurred. Try again.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		editorPane.setColumnHeaderView(createLabel("Editor - " + path));
		filePath = path;
	}

	public static String open() {
		int option = fileChooser.showOpenDialog(frame);
		String path = "";
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				Scanner sc = new Scanner(file);
				sc.useDelimiter("\\Z");
				editorArea.setText(sc.next());
				path = file.getPath();
				sc.close();
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		editorPane.setColumnHeaderView(createLabel("Editor - " + path));
		return path;
	}

	public static void save(String path) {
		if (path == "") {
			newFile();
		} else {
			try {
				FileWriter file = new FileWriter(path);
				file.write(editorArea.getText());
				file.close();
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "An error occurred. Try again.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static void compile(String path) {
		if (path == "") {
			outputArea.setText("No program to compile.");
		} else {
			try {
				Process process = Runtime.getRuntime().exec("javac " + path);
				BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line = "", output = "";

				while ((line = error.readLine()) != null) {
					output += line + "\n";
				}
				if (output.length() == 0) {
					output = "Program compiled successfully.";
				}
				outputArea.setText(output);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static String getClassName(String path) {
		int slashIndex = path.lastIndexOf("\\");
		String className = "";
		try {
			className = path.substring(slashIndex + 1, path.lastIndexOf("."));
			path = path.substring(0, slashIndex);
		} catch (StringIndexOutOfBoundsException e) {
			outputArea.setText("No program to run.");
		}
		return className;
	}

	public static void run(String path) {
		if (path == "") {
			outputArea.setText("No program to run.");
			return;
		}
		String code = editorArea.getText();
		String input = "";
		if (code.contains("nextByte") || code.contains("nextShort") || code.contains("nextInt")
				|| code.contains("nextLong") || code.contains("nextFloat") || code.contains("nextDouble")
				|| code.contains("next") || code.contains("nextLine")) {
			JTextArea inputTextArea = new JTextArea(10, 50);
			JScrollPane inputPane = new JScrollPane(inputTextArea);
			int option = JOptionPane.showConfirmDialog(frame, inputPane, "Enter input(s) here",
					JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				input = inputTextArea.getText();
			} else {
				JOptionPane.showMessageDialog(frame, "Program execution stopped because you did not input anything.");
				return;
			}
		}
		String dir = path.substring(0, path.lastIndexOf("\\") + 1);
		java.util.List<String> list = new ArrayList<String>();
		list.add("java");
		list.add("-cp");
		list.add(dir);
		list.add(getClassName(filePath));
		ProcessBuilder pb = new ProcessBuilder(list);
		try {
			Process proc = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			if (input != "") {
				OutputStream os = proc.getOutputStream();
				os.write(input.getBytes());
				os.write(System.lineSeparator().getBytes());
				os.flush();
			}
			String progOutput = br.readLine();
			String output = "";
			String progError = error.readLine();
			String runError = "";

			while (progError != null) {
				runError += progError + "\n";
				progError = error.readLine();
			}
			outputArea.setText(runError);

			while (progOutput != null) {
				output += progOutput + "\n";
				progOutput = br.readLine();
			}
			outputArea.setText(outputArea.getText() + output);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

/**
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. TextLineNumber
 * supports wrapped lines and will highlight the line number of the current line
 * in the text component.
 *
 * This class was designed to be used as a component added to the row header of
 * a JScrollPane.
 */
class TextLineNumber extends JPanel implements CaretListener, DocumentListener, PropertyChangeListener {
	public final static float LEFT = 0.0f;
	public final static float CENTER = 0.5f;
	public final static float RIGHT = 1.0f;

	private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

	// Text component this TextTextLineNumber component is in sync with

	private JTextComponent component;

	// Properties that can be changed

	private boolean updateFont;
	private int borderGap;
	private Color currentLineForeground;
	private float digitAlignment;
	private int minimumDisplayDigits;

	// Keep history information to reduce the number of times the component
	// needs to be repainted

	private int lastDigits;
	private int lastHeight;
	private int lastLine;

	private HashMap<String, FontMetrics> fonts;

	/**
	 * Create a line number component for a text component. This minimum display
	 * width will be based on 3 digits.
	 *
	 * @param component the related text component
	 */
	public TextLineNumber(JTextComponent component) {
		this(component, 3);
	}

	/**
	 * Create a line number component for a text component.
	 *
	 * @param component            the related text component
	 * @param minimumDisplayDigits the number of digits used to calculate the
	 *                             minimum width of the component
	 */
	public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
		this.component = component;

		setFont(component.getFont());

		setBorderGap(5);
		setCurrentLineForeground(Color.RED);
		setDigitAlignment(RIGHT);
		setMinimumDisplayDigits(minimumDisplayDigits);

		component.getDocument().addDocumentListener(this);
		component.addCaretListener(this);
		component.addPropertyChangeListener("font", this);
	}

	/**
	 * Gets the update font property
	 *
	 * @return the update font property
	 */
	public boolean getUpdateFont() {
		return updateFont;
	}

	/**
	 * Set the update font property. Indicates whether this Font should be updated
	 * automatically when the Font of the related text component is changed.
	 *
	 * @param updateFont when true update the Font and repaint the line numbers,
	 *                   otherwise just repaint the line numbers.
	 */
	public void setUpdateFont(boolean updateFont) {
		this.updateFont = updateFont;
	}

	/**
	 * Gets the border gap
	 *
	 * @return the border gap in pixels
	 */
	public int getBorderGap() {
		return borderGap;
	}

	/**
	 * The border gap is used in calculating the left and right insets of the
	 * border. Default value is 5.
	 *
	 * @param borderGap the gap in pixels
	 */
	public void setBorderGap(int borderGap) {
		this.borderGap = borderGap;
		Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
		setBorder(new CompoundBorder(OUTER, inner));
		lastDigits = 0;
		setPreferredWidth();
	}

	/**
	 * Gets the current line rendering Color
	 *
	 * @return the Color used to render the current line number
	 */
	public Color getCurrentLineForeground() {
		return currentLineForeground == null ? getForeground() : currentLineForeground;
	}

	/**
	 * The Color used to render the current line digits. Default is Coolor.RED.
	 *
	 * @param currentLineForeground the Color used to render the current line
	 */
	public void setCurrentLineForeground(Color currentLineForeground) {
		this.currentLineForeground = currentLineForeground;
	}

	/**
	 * Gets the digit alignment
	 *
	 * @return the alignment of the painted digits
	 */
	public float getDigitAlignment() {
		return digitAlignment;
	}

	/**
	 * Specify the horizontal alignment of the digits within the component. Common
	 * values would be:
	 * <ul>
	 * <li>TextLineNumber.LEFT
	 * <li>TextLineNumber.CENTER
	 * <li>TextLineNumber.RIGHT (default)
	 * </ul>
	 * 
	 * @param currentLineForeground the Color used to render the current line
	 */
	public void setDigitAlignment(float digitAlignment) {
		this.digitAlignment = digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
	}

	/**
	 * Gets the minimum display digits
	 *
	 * @return the minimum display digits
	 */
	public int getMinimumDisplayDigits() {
		return minimumDisplayDigits;
	}

	/**
	 * Specify the mimimum number of digits used to calculate the preferred width of
	 * the component. Default is 3.
	 *
	 * @param minimumDisplayDigits the number digits used in the preferred width
	 *                             calculation
	 */
	public void setMinimumDisplayDigits(int minimumDisplayDigits) {
		this.minimumDisplayDigits = minimumDisplayDigits;
		setPreferredWidth();
	}

	/**
	 * Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth() {
		Element root = component.getDocument().getDefaultRootElement();
		int lines = root.getElementCount();
		int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

		// Update sizes when number of digits in the line number changes

		if (lastDigits != digits) {
			lastDigits = digits;
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int width = fontMetrics.charWidth('0') * digits;
			Insets insets = getInsets();
			int preferredWidth = insets.left + insets.right + width;

			Dimension d = getPreferredSize();
			d.setSize(preferredWidth, HEIGHT);
			setPreferredSize(d);
			setSize(d);
		}
	}

	/**
	 * Draw the line numbers
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Determine the width of the space available to draw the line number

		FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
		Insets insets = getInsets();
		int availableWidth = getSize().width - insets.left - insets.right;

		// Determine the rows to draw within the clipped bounds.

		Rectangle clip = g.getClipBounds();
		int rowStartOffset = component.viewToModel(new Point(0, clip.y));
		int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

		while (rowStartOffset <= endOffset) {
			try {
				if (isCurrentLine(rowStartOffset))
					g.setColor(getCurrentLineForeground());
				else
					g.setColor(getForeground());

				// Get the line number as a string and then determine the
				// "X" and "Y" offsets for drawing the string.

				String lineNumber = getTextLineNumber(rowStartOffset);
				int stringWidth = fontMetrics.stringWidth(lineNumber);
				int x = getOffsetX(availableWidth, stringWidth) + insets.left;
				int y = getOffsetY(rowStartOffset, fontMetrics);
				g.drawString(lineNumber, x, y);

				// Move to the next row

				rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
			} catch (Exception e) {
				break;
			}
		}
	}

	/*
	 * We need to know if the caret is currently positioned on the line we are about
	 * to paint so the line number can be highlighted.
	 */
	private boolean isCurrentLine(int rowStartOffset) {
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();

		if (root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition))
			return true;
		else
			return false;
	}

	/*
	 * Get the line number to be drawn. The empty string will be returned when a
	 * line of text has wrapped.
	 */
	protected String getTextLineNumber(int rowStartOffset) {
		Element root = component.getDocument().getDefaultRootElement();
		int index = root.getElementIndex(rowStartOffset);
		Element line = root.getElement(index);

		if (line.getStartOffset() == rowStartOffset)
			return String.valueOf(index + 1);
		else
			return "";
	}

	/*
	 * Determine the X offset to properly align the line number when drawn
	 */
	private int getOffsetX(int availableWidth, int stringWidth) {
		return (int) ((availableWidth - stringWidth) * digitAlignment);
	}

	/*
	 * Determine the Y offset for the current row
	 */
	private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException {
		// Get the bounding rectangle of the row

		Rectangle r = component.modelToView(rowStartOffset);
		int lineHeight = fontMetrics.getHeight();
		int y = r.y + r.height;
		int descent = 0;

		// The text needs to be positioned above the bottom of the bounding
		// rectangle based on the descent of the font(s) contained on the row.

		if (r.height == lineHeight) // default font is being used
		{
			descent = fontMetrics.getDescent();
		} else // We need to check all the attributes for font changes
		{
			if (fonts == null)
				fonts = new HashMap<String, FontMetrics>();

			Element root = component.getDocument().getDefaultRootElement();
			int index = root.getElementIndex(rowStartOffset);
			Element line = root.getElement(index);

			for (int i = 0; i < line.getElementCount(); i++) {
				Element child = line.getElement(i);
				AttributeSet as = child.getAttributes();
				String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
				Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
				String key = fontFamily + fontSize;

				FontMetrics fm = fonts.get(key);

				if (fm == null) {
					Font font = new Font(fontFamily, Font.PLAIN, fontSize);
					fm = component.getFontMetrics(font);
					fonts.put(key, fm);
				}

				descent = Math.max(descent, fm.getDescent());
			}
		}

		return y - descent;
	}

	//
	// Implement CaretListener interface
	//
	@Override
	public void caretUpdate(CaretEvent e) {
		// Get the line the caret is positioned on

		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		int currentLine = root.getElementIndex(caretPosition);

		// Need to repaint so the correct line number can be highlighted

		if (lastLine != currentLine) {
			// repaint();
			getParent().repaint();
			lastLine = currentLine;
		}
	}

	//
	// Implement DocumentListener interface
	//
	@Override
	public void changedUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentChanged();
	}

	/*
	 * A document change may affect the number of displayed lines of text. Therefore
	 * the lines numbers will also change.
	 */
	private void documentChanged() {
		// View of the component has not been updated at the time
		// the DocumentEvent is fired

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					int endPos = component.getDocument().getLength();
					Rectangle rect = component.modelToView(endPos);

					if (rect != null && rect.y != lastHeight) {
						setPreferredWidth();
						// repaint();
						getParent().repaint();
						lastHeight = rect.y;
					}
				} catch (BadLocationException ex) {
					/* nothing to do */ }
			}
		});
	}

	//
	// Implement PropertyChangeListener interface
	//
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof Font) {
			if (updateFont) {
				Font newFont = (Font) evt.getNewValue();
				setFont(newFont);
				lastDigits = 0;
				setPreferredWidth();
			} else {
				// repaint();
				getParent().repaint();
			}
		}
	}
}

/*
 ** This class will merge individual edits into a single larger edit. That is,
 * characters entered sequentially will be grouped together and undone as a
 * group. Any attribute changes will be considered as part of the group and will
 * therefore be undone when the group is undone.
 */
class CompoundUndoManager extends UndoManager implements DocumentListener {
	private UndoManager undoManager;
	private CompoundEdit compoundEdit;
	private JTextComponent textComponent;
	private UndoAction undoAction;
	private RedoAction redoAction;

	// These fields are used to help determine whether the edit is an
	// incremental edit. The offset and length should increase by 1 for
	// each character added or decrease by 1 for each character removed.

	private int lastOffset;
	private int lastLength;

	public CompoundUndoManager(JTextComponent textComponent) {
		this.textComponent = textComponent;
		undoManager = this;
		undoAction = new UndoAction();
		redoAction = new RedoAction();
		textComponent.getDocument().addUndoableEditListener(this);
	}

	/*
	 ** Add a DocumentLister before the undo is done so we can position the Caret
	 * correctly as each edit is undone.
	 */
	public void undo() {
		textComponent.getDocument().addDocumentListener(this);
		super.undo();
		textComponent.getDocument().removeDocumentListener(this);
	}

	/*
	 ** Add a DocumentLister before the redo is done so we can position the Caret
	 * correctly as each edit is redone.
	 */
	public void redo() {
		textComponent.getDocument().addDocumentListener(this);
		super.redo();
		textComponent.getDocument().removeDocumentListener(this);
	}

	/*
	 ** Whenever an UndoableEdit happens the edit will either be absorbed by the
	 * current compound edit or a new compound edit will be started
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		// Start a new compound edit

		if (compoundEdit == null) {
			compoundEdit = startCompoundEdit(e.getEdit());
			return;
		}

		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		// Check for an attribute change

		AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit();

		if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
			if (offsetChange == 0) {
				compoundEdit.addEdit(e.getEdit());
				return;
			}
		}

		// Check for an incremental edit or backspace.
		// The Change in Caret position and Document length should both be
		// either 1 or -1.

		// int offsetChange = textComponent.getCaretPosition() - lastOffset;
		// int lengthChange = textComponent.getDocument().getLength() - lastLength;

		if (offsetChange == lengthChange && Math.abs(offsetChange) == 1) {
			compoundEdit.addEdit(e.getEdit());
			lastOffset = textComponent.getCaretPosition();
			lastLength = textComponent.getDocument().getLength();
			return;
		}

		// Not incremental edit, end previous edit and start a new one

		compoundEdit.end();
		compoundEdit = startCompoundEdit(e.getEdit());
	}

	/*
	 ** Each CompoundEdit will store a group of related incremental edits (ie. each
	 * character typed or backspaced is an incremental edit)
	 */
	private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
		// Track Caret and Document information of this compound edit

		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();

		// The compound edit is used to store incremental edits

		compoundEdit = new MyCompoundEdit();
		compoundEdit.addEdit(anEdit);

		// The compound edit is added to the UndoManager. All incremental
		// edits stored in the compound edit will be undone/redone at once

		addEdit(compoundEdit);

		undoAction.updateUndoState();
		redoAction.updateRedoState();

		return compoundEdit;
	}

	/*
	 * The Action to Undo changes to the Document. The state of the Action is
	 * managed by the CompoundUndoManager
	 */
	public Action getUndoAction() {
		return undoAction;
	}

	/*
	 * The Action to Redo changes to the Document. The state of the Action is
	 * managed by the CompoundUndoManager
	 */
	public Action getRedoAction() {
		return redoAction;
	}

	//
	// Implement DocumentListener
	//
	/*
	 * Updates to the Document as a result of Undo/Redo will cause the Caret to be
	 * repositioned
	 */
	public void insertUpdate(final DocumentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int offset = e.getOffset() + e.getLength();
				offset = Math.min(offset, textComponent.getDocument().getLength());
				textComponent.setCaretPosition(offset);
			}
		});
	}

	public void removeUpdate(DocumentEvent e) {
		textComponent.setCaretPosition(e.getOffset());
	}

	public void changedUpdate(DocumentEvent e) {
	}

	class MyCompoundEdit extends CompoundEdit {
		public boolean isInProgress() {
			// in order for the canUndo() and canRedo() methods to work
			// assume that the compound edit is never in progress

			return false;
		}

		public void undo() throws CannotUndoException {
			// End the edit so future edits don't get absorbed by this edit

			if (compoundEdit != null)
				compoundEdit.end();

			super.undo();

			// Always start a new compound edit after an undo

			compoundEdit = null;
		}
	}

	/*
	 * Perform the Undo and update the state of the undo/redo Actions
	 */
	class UndoAction extends AbstractAction {
		public UndoAction() {
			putValue(Action.NAME, "Undo");
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.undo();
				textComponent.requestFocusInWindow();
			} catch (CannotUndoException ex) {
			}

			updateUndoState();
			redoAction.updateRedoState();
		}

		private void updateUndoState() {
			setEnabled(undoManager.canUndo());
		}
	}

	/*
	 * Perform the Redo and update the state of the undo/redo Actions
	 */
	class RedoAction extends AbstractAction {
		public RedoAction() {
			putValue(Action.NAME, "Redo");
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.redo();
				textComponent.requestFocusInWindow();
			} catch (CannotRedoException ex) {
			}

			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			setEnabled(undoManager.canRedo());
		}
	}
}