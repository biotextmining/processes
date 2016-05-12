package com.silicolife.textmining.processes.nlptools.structure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import com.silicolife.textmining.core.datastructures.exceptions.SintaticTreeViewException;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.nlptools.DeepParsingLabels;
import com.silicolife.textmining.core.datastructures.nlptools.PartOfSpeechLabels;
import com.silicolife.textmining.core.datastructures.utils.Coordinates;
import com.silicolife.textmining.core.interfaces.core.document.structure.IParsingToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;

/**
 * Adapt from Gate 8 Sintatic Tree View
 * 
 * @author Hugo Costa
 *
 */
public class SyntaxTreeViewerPane extends JPanel implements Scrollable{

	private static final long serialVersionUID = 1L;

	// display all buttons x pixels apart horizontally
	private int horizButtonGap = 5;
	// display buttons at diff layers x pixels apart vertically
	private int vertButtonGap = 50;
	// number of pixels to be used as increment by scroller
	private int maxUnitIncrement = 10;

	// the HashSet with the coordinates of the lines to draw
	private Set<Coordinates> lines = new HashSet<Coordinates>();

	// all leaf nodes
	private HashMap<Integer, STreeNode> leaves = null;

	// all non-terminal nodes
	private HashMap<Integer, STreeNode> nonTerminals = null;

	// all buttons corresponding to any node
	private HashMap<Integer, JButton> buttons = null;

	// all selected buttons
	private Vector<JButton> selection = null;

	// all annotations to be displayed
	private List<IParsingToken> parsingNodes;
	// the document to which the annotations belong
	private ISentence sentence = null;

	public SyntaxTreeViewerPane(ISentence sentence) throws SintaticTreeViewException {
		super();
		this.sentence = sentence;
		leaves = new HashMap<Integer, STreeNode>();
		nonTerminals = new HashMap<Integer, STreeNode>();
		buttons = new HashMap<Integer, JButton>();
		selection = new Vector<JButton>();
		this.setLayout(null);
		utterances2Trees();
		annotations2Trees();
		repaint();
	}


	protected void paintComponent(Graphics g) {
		super.paintComponent( g);
		drawLines(g);
	}


	private void drawLines(Graphics g) {

		for (Iterator<Coordinates> i = lines.iterator(); i.hasNext(); ) {
			Coordinates coords = i.next();

			g.drawLine( coords.getX1(),
					coords.getY1(),
					coords.getX2(),
					coords.getY2());
		}
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return maxUnitIncrement;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width - maxUnitIncrement;
		else
			return visibleRect.height - maxUnitIncrement;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


	/**
	 * Converts the annotations into treeNodes
	 * @throws SintaticTreeViewException 
	 */
	private void annotations2Trees() throws SintaticTreeViewException {
		Map<String, JButton> processed = new HashMap<String, JButton>(); //for all processed annotations

		parsingNodes = this.sentence.getParsingTokens();
		if (parsingNodes == null || parsingNodes.isEmpty())
			throw new SintaticTreeViewException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.sintatictreeview.err.noparsingnodes"));
		// sort them from left to right first
		Collections.sort(parsingNodes, new OffsetComparator());
		for(int i=0;i<parsingNodes.size();i++)
		{
			IParsingToken parsingToken = parsingNodes.get(i);

			List<IParsingToken> children = parsingToken.getConsists();
			//check if it's a leaf
			if (children == null ||
					children.isEmpty())
			{

				STreeNode leaf = findLeaf(parsingToken.getStartOffset(), parsingToken.getEndOffset());
				if (leaf == null) {//not found
					throw new SintaticTreeViewException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.sintatictreeview.err.canfinfleafnodeforannotation")+": " + parsingToken.toString());
				}

				JButton button = buttons.get(new Integer(leaf.getID()));
				selection.clear();
				selection.add(button);

				//then create the non-terminal with the category
				STreeNode node = new STreeNode(parsingToken);
				node.add(leaf);
				node.setLevel(1);
				node.setUserObject(parsingToken.getCategory());
				nonTerminals.put(new Integer(node.getID()), node);
				JButton parentButton = createCentralButton(node);
				addLines(node);

				//finally add to the processed annotations
				processed.put(parsingToken.getUID(), parentButton);

			} //if

		} //loop through children

		//loop through the rest of the nodes
		for(int i=0;i<parsingNodes.size();i++)
		{
			
			IParsingToken parsingToken = parsingNodes.get(i);
			if (processed.containsKey(parsingToken.getUID()))
				continue;
			processChildrenAnnots(parsingToken, processed);
		} //process all higher nodes

		selection.clear();

		this.scrollRectToVisible(new
				Rectangle(0, getHeight()- (int) getVisibleRect().getHeight(),
						(int) getVisibleRect().getWidth(), (int) getVisibleRect().getHeight()));
	} //annotations2Trees

	@SuppressWarnings("unchecked")
	private JButton processChildrenAnnots(IParsingToken parsingToken, Map<String, JButton> processed) {
		selection.clear();
		Vector<JButton> childrenButtons = new Vector<JButton>();

		List<IParsingToken> children = parsingToken.getConsists();

		for (int i=0;i<children.size();i++) {
			IParsingToken child = children.get(i);
			JButton childButton;

			if (processed.containsKey(child.getUID()))
				childButton = processed.get(child.getUID());
			else
			{
				childButton = processChildrenAnnots(child, processed);
			}
			childrenButtons.add(childButton);
		}

		selection = (Vector<JButton>) childrenButtons.clone();
		STreeNode parent = createParentNode(parsingToken.getCategory(),parsingToken);
		nonTerminals.put(new Integer(parent.getID()), parent);
		JButton parentButton = createCentralButton(parent);
		addLines(parent);

		processed.put(parsingToken.getUID(), parentButton);
		selection.clear();
		return parentButton;
	}

	private STreeNode findLeaf(Long start, Long end) {
		for (Iterator<STreeNode> i = leaves.values().iterator(); i.hasNext(); ) {
			STreeNode node = i.next();
			if (node.getStart() == start.intValue() &&
					node.getEnd() == end.intValue()
					)
				return node;
		}

		return null;
	}


	/**
	 * Converts the given utterances into a set of leaf nodes for annotation
	 * @throws SintaticTreeViewException 
	 */
	private void utterances2Trees() throws SintaticTreeViewException {

		if ( this.sentence == null) {
			throw new SintaticTreeViewException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.sintatictreeview.err.nosentence"));
		}

		// set the utterance offset correctly.
		List<IParsingToken> allTokens = this.sentence.getParsingTokens();
		List<IParsingToken> tokensAS = new ArrayList<IParsingToken>();
		for(IParsingToken token:allTokens)
		{
			if(token.getConsists()==null || token.getConsists().isEmpty())
			{
				tokensAS.add(token);
			}
		}
		if (tokensAS == null || tokensAS.isEmpty()) {
			throw new SintaticTreeViewException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.sintatictreeview.err.nosentencetokens"));
		}

		Insets insets = this.getInsets();
		// the starting X position for the buttons
		int buttonX = insets.left;

		// the starting Y position
		int buttonY = this.getHeight() - 20 - insets.bottom;
		//if no tokens to match, do nothing
		if (tokensAS.isEmpty())
			return;
		Collections.sort(tokensAS, new OffsetComparator());

		//loop through the tokens
		for (int i= 0; i< tokensAS.size(); i++) {
			IParsingToken tokenAnnot = tokensAS.get(i);
			Long tokenBegin = tokenAnnot.getStartOffset();
			Long tokenEnd = tokenAnnot.getEndOffset();

			// create the leaf node
			STreeNode node = new STreeNode(tokenBegin.longValue(), tokenEnd.longValue(),tokenAnnot);

			// make it a leaf
			node.setAllowsChildren(false);

			// set the text
			node.setUserObject(tokenAnnot.getText());
			node.setLevel(0);

			// add to hash table of leaves
			leaves.put(new Integer(node.getID()), node);

			// create the corresponding button
			buttonX = createButton4Node(node, buttonX, buttonY);

		} //while
	
		this.setSize(buttonX, buttonY + 20 + insets.bottom);
		// this.resize(buttonX, buttonY + 20 + insets.bottom);
		this.setPreferredSize(this.getSize());

	} // utterance2Trees

	/**
	 * Returns the X position where another button can start if necessary.
	 * To be used to layout only the leaf buttons. All others must be created
	 * central to their children using createCentralButton.
	 */
	private int createButton4Node(STreeNode node, int buttonX, int buttonY) {
		String cat = (String) node.getUserObject();
		JButton button = new JButton(cat);
		if( node.getBackgoundColor()!=null)
		{
			button.setForeground(Color.decode(node.getBackgoundColor()));
			button.setFont(button.getFont().deriveFont(Font.BOLD));
		}
		if( node.getForegroundColor()!=null)
		{
			button.setBackground(Color.decode(node.getForegroundColor()));
		}
		button.setBorderPainted(false);
		button.setMargin(new Insets(0,0,0,0));
		Dimension buttonSize = button.getPreferredSize();
		button.setSize(buttonSize);
		buttonY = buttonY - buttonSize.height;
		button.setLocation(buttonX, buttonY);
		button.setActionCommand("" + node.getID());
		button.setVisible(true);
		button.setEnabled(true);
		this.add(button);
		buttons.put(new Integer(node.getID()), button);
		buttonX +=  buttonSize.width + horizButtonGap;
		return buttonX;

	}// private int createButton4Node(STreeNode node, int buttonX, int buttonY)

	private String getPOSTAggingCategoryByShortName(String cat) {
		for(PartOfSpeechLabels label:PartOfSpeechLabels.values())
		{
			if(label.value().equals(cat))
			{
				return label.getDescription();
			}
		}
		return null;
	}

	
	private String getDeepCategoryByShortName(String cat) {
		for(DeepParsingLabels label:DeepParsingLabels.values())
		{
			if(label.value().equals(cat))
			{
				return label.getDescription();
			}
		}
		return null;
	}

	private JButton createCentralButton(STreeNode newNode) {
		String category = (String) newNode.getUserObject();
		FocusButton button = new FocusButton(category);
		button.setBorderPainted(false);
		int buttonWidth,
		buttonHeight,
		buttonX = 0,
		buttonY =0;
		Dimension buttonSize = button.getPreferredSize();

		buttonWidth = buttonSize.width;
		buttonHeight = buttonSize.height;

		int left = this.getWidth(), right =0 , top = this.getHeight();

		// determine the left, right, top
		for (Iterator<JButton> i = selection.iterator(); i.hasNext(); ) {
			JButton childButton = i.next();

			if (left > childButton.getX())
				left = childButton.getX();
			if (childButton.getX() + childButton.getWidth() > right)
				right = childButton.getX() + childButton.getWidth();
			if (childButton.getY() < top)
				top = childButton.getY();
		}
		buttonX = (left + right) /2 - buttonWidth/2;
		buttonY = top - vertButtonGap;
		button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
		button.setActionCommand("" + newNode.getID());
		String categoryDeep = getPOSTAggingCategoryByShortName(category);
		String categoryPOS = getDeepCategoryByShortName(category);
		if(categoryDeep!=null)
		{
			button.setToolTipText(categoryDeep);
		}
		else if(categoryPOS!=null)
		{
			button.setToolTipText(categoryPOS);

		}
		this.add(button);
		// add to hashmap of buttons
		buttons.put(new Integer(newNode.getID()), button);
		// check if we need to resize the panel
		if (buttonY < 0) {
			this.setSize(this.getWidth(), this.getHeight() + 5* (- buttonY));
			this.setPreferredSize(this.getSize());
			shiftButtonsDown(5* (-buttonY));
		}

		return button;
	}// private JButton createCentralButton(STreeNode newNode)

	private void shiftButtonsDown(int offset) {
		for (Iterator<JButton> i = buttons.values().iterator(); i.hasNext(); ) {
			JButton button = i.next();
			button.setBounds(		button.getX(),
					button.getY() + offset,
					button.getWidth(),
					button.getHeight());
		} // for loop through buttons

		for (Iterator<Coordinates> k = lines.iterator(); k.hasNext(); ) {
			Coordinates coords = k.next();
			coords.setY1(coords.getY1() + offset);
			coords.setY2(coords.getY2() + offset);
		}
	}// private void shiftButtonsDown(int offset)

	

	private void addLines(STreeNode newNode) {

		JButton newButton = buttons.get(new Integer(newNode.getID()));
		int nbX = newButton.getX() + newButton.getWidth()/2;
		int nbY = newButton.getY() + newButton.getHeight();

		for (Iterator<JButton> i = selection.iterator(); i.hasNext(); ) {
			JButton selButton = i.next();

			//I create it a rect but it will in fact be used as x1, y1, x2, y2 for the
			//draw line. see drawLines.
			Coordinates coords = new Coordinates(
					nbX,
					nbY,
					selButton.getX() + selButton.getWidth()/2,
					selButton.getY());

			lines.add(coords);
		}

	} // addLines

	/**
	 * Create a parent node for all selected non-terminal nodes
	 */
	protected STreeNode createParentNode(String text, IParsingToken annot) {
		STreeNode  parentNode = new STreeNode(annot);

		long level = -1;
		for (Iterator<JButton> i = selection.iterator(); i.hasNext(); ) {
			JButton button = i.next();
			Integer id = new Integer(button.getActionCommand());

			STreeNode child = nonTerminals.get(id);

			if (level < child.getLevel())
				level = child.getLevel();

			parentNode.add(child);
		} //for

		parentNode.setLevel(level+1);
		parentNode.setUserObject(text);

		return parentNode;
	}


	/**
	 * Focus Button
	 * 	 
	 */
	private static class FocusButton extends JButton {

		private static final long serialVersionUID = 1L;

		public FocusButton(String text) {
			super(text);
		}

		@SuppressWarnings("unused")
		public FocusButton() {
			super();
		}

		@SuppressWarnings("unused")
		public FocusButton(Icon icon) {
			super(icon);
		}

		@SuppressWarnings("unused")
		public FocusButton(String text, Icon icon) {
			super(text, icon);
		}// public FocusButton

		//  public boolean isManagingFocus() {
		//    return true;
		//  }// public boolean isManagingFocus()

		@Override
		public void processComponentKeyEvent(KeyEvent e) {
			super.processComponentKeyEvent(e);

			//I need that cause I get all events here, so I only want to process
			//when it's a release event. The reason is that for keys like <DEL>
			//key_typed never happens
			if (e.getID() != KeyEvent.KEY_RELEASED)
				return;

		}// public void processComponentKeyEvent(KeyEvent e)

	}
	
}// class SyntaxTreeViewer


