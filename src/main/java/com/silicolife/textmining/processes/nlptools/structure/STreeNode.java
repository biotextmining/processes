/*
 *  STreeNode.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 07/08/2001
 *
 *  $Id: STreeNode.java 17881 2014-04-18 17:10:44Z markagreenwood $
 */

package com.silicolife.textmining.processes.nlptools.structure;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.document.structure.IParsingToken;


@SuppressWarnings("serial")
public class STreeNode extends DefaultMutableTreeNode {

  private static int nextID = 0;

  private int level;            // level in the syntax tree
  private int nodeID;           //ID of the node

  private long start, end;       //the start and end nodes for this annotation
  private IParsingToken parsingToken;     //the annotation that's created during import/export
                        //not to be used otherwise. During import span is set to
                        //be the same as the annotation span. During export the
                        //annotation span is set to be the same as the span.

  public STreeNode(IParsingToken parsingToken) {
    level = -1;
    nodeID = nextID++;
    //span = annot.getSpans().getElementAt(0);
    //get the first span, there should be no others
    this.parsingToken = parsingToken;
    this.start = parsingToken.getStartOffset();
    this.end = parsingToken.getEndOffset();
  }// public STreeNode(Annotation annot)

  public STreeNode(long start, long end,IParsingToken parsingToken) {
    level = -1;
    nodeID = nextID++;
    this.parsingToken = parsingToken;
    this.start = start;
    this.end = end;
  }// public STreeNode(int start, int end)
//
//  public STreeNode() {
//    level = -1;
//    nodeID = nextID++;
//    start = 0;
//    end = 0;
//  }// public STreeNode()

  @Override
  public int getLevel() {
    return level;
  }// public int getLevel()

  public void setLevel(long level) {
    this.level = (int) level;
  }// public void setLevel(int level)

  public void setLevel(int level) {
    this.level = level;
  }// public void setLevel(int level)

  public int getID() {
	  return nodeID;
  }// public int getID()

  public long getStart() {
    return start;
  }// public int getStart()

  public void setStart(long start) {
    this.start = start;
  }// public void setStart(int start)

  public long getEnd() {
    return end;
  }// public int getEnd()

  public void setEnd(long end) {
    this.end = end;
  }// public void setEnd(int end)

  /**
    * This also sets the span to match the annotation span!
    */
  public void setAnnotation(IParsingToken parsingToken) {
    this.parsingToken = parsingToken;
    this.start = parsingToken.getStartOffset();
    this.end = parsingToken.getEndOffset();
  }// public void setAnnotation(Annotation annot)

  public IParsingToken getAnnotation() {
    return parsingToken;
  }// public Annotation getAnnotation()

  public void disconnectChildren() {
    for (Iterator<?> i = this.children.iterator(); i.hasNext(); )
    	((STreeNode) i.next()).setParent(null);
    this.children.clear();
  }// public void disconnectChildren()

  public String getBackgoundColor()
  {
	  if(this.parsingToken==null)
		  return null;
	  if(this.parsingToken.getProperties() == null)
		 return null;
	  return this.parsingToken.getProperties().getProperty(GlobalNames.color);
  }
  
  public String getForegroundColor()
  {
	  if(this.parsingToken==null)
		  return null;
	  if(this.parsingToken.getProperties() == null)
		 return null;
	  return this.parsingToken.getProperties().getProperty(GlobalNames.backgoundcolor);
  }
} // STreeNode

