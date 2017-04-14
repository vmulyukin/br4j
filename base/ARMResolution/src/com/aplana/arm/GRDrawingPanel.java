/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.arm;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class GRDrawingPanel extends JPanel implements MouseListener,MouseMotionListener, Scrollable {
		
	private static final long serialVersionUID = -4828383198869178357L;
	private int maxUnitIncrement = 25;	
	private GraphicsResolution parent;
	private boolean isDrawSign;
	private boolean isDrawExMark;
	private boolean isDrawQuMark;
	private boolean isDrawCheckMark;
	private boolean isDrawCustom;

	BufferedImage buffer;
	Graphics2D g2;
	Point prev_point;
	
	GRDrawingPanel(GraphicsResolution parent){	
		try {
			this.parent = parent;
			setBackground(Color.lightGray);
							
			setPreferredSize(new Dimension(700, 600));
			setDoubleBuffered(true);
			//setOpaque(false);
			System.out.println("����������� GRDrawingPanel ��������.");
			
		} catch (Exception e) {
			ExceptionLoggerFile.Log(e);
		}
		
	}

	protected void finalize() throws Throwable {
		//do finalization here
		super.finalize(); //not necessary if extending Object.
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	DrawingData cur_drawdata;
	@SuppressWarnings("unchecked")
	private void pointsDrawing(MouseEvent e) {
		DrawingData drawdata = new DrawingData();
		UndoData data = new UndoData();
		data.add(drawdata);
		cur_drawdata = drawdata; 
		
		parent.setCurdata(data);		
		parent.getData().push(parent.getCurdata());
		parent.getUndo_data().clear();
		
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK) {
			drawdata.color = Color.white;
			setCursor(parent.eraser);
		} else {
			setCursor(parent.pencil);
			drawdata.color = getForeground();			
		}

		
		int x1 = e.getX();
        int y1 = e.getY();
        drawdata.points.add(new Point(x1, y1));                
        drawdata.size = parent.getBrush_size();
        
		//TODO new       
		int bsize = parent.getBrush_size();
		g2.setStroke(new BasicStroke(bsize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		g2.setColor(drawdata.color);
		g2.drawLine(x1, y1, x1, y1);
		
		
		//repaint(x1-10, y1-10, x1+10, y1+10);
        repaint();
	}
	
	private void addColorSizePositionInfo(UndoData data, MouseEvent e){
		int x1 = e.getX();
        int y1 = e.getY();
        
		Iterator iter = data.getDrawData().iterator();
		while (iter.hasNext()) {
			DrawingData drawdata = (DrawingData) iter.next();
			drawdata.color = getForeground();
		    drawdata.size = parent.getBrush_size();
		
			Iterator it = drawdata.points.iterator();
			while( it. hasNext() ){
				Point p = (Point) it.next();
				p.translate(x1, y1);
			} // end iterator
		}// end while					
	}

	@SuppressWarnings("unchecked")
	private void drawCustomData(UndoData data, MouseEvent e){
		addColorSizePositionInfo(data, e);		
		parent.setCurdata(data);
		parent.getData().push(parent.getCurdata());
		parent.getUndo_data().clear();
		//TODO new
		//repaint();
		paintIntoBuffer();
	}
	
	private void signDrawing(MouseEvent e) {
		UndoData data = (UndoData) parent.getSignData().clone();
		drawCustomData(data, e);
		parent.getControls().getButton("sign").setSelected(false);
	}
	
	public void mousePressed(MouseEvent e) {
		
		try {
			//System.out.println("pressed");
			e.consume();		
			
			if(isDrawSign){
				signDrawing(e);				
			} else if (isDrawExMark){
				exmarkDrawing(e);
			} else if (isDrawQuMark){
				qumarkDrawing(e);							
			} else if (isDrawCheckMark){
				checkmarkDrawing(e);							
			} else if (isDrawCustom) {
				customDrawing(e);				
			} else {
				pointsDrawing(e);
			}

		
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkmarkDrawing(MouseEvent e) {
		if(parent.getCheckMarkData() != null){
			UndoData data = (UndoData) parent.getCheckMarkData().clone();			
			drawCustomData(data, e);
			parent.getControls().getButton("exmark").setSelected(false);
			return;
		}
		
		DrawingData drawdata = new DrawingData();
		UndoData data = new UndoData();
		data.add(drawdata);
		
		parent.setCurdata(data);
		parent.getData().push(parent.getCurdata());
		parent.getUndo_data().clear();
		setCursor(parent.pencil);
		drawdata.color = getForeground();
		drawdata.size = parent.getBrush_size();
		
		
		int x1 = e.getX();
        int y1 = e.getY();
		
        drawdata.points.add(new Point(x1, y1+10));
        drawdata.points.add(new Point(x1+10, y1+20));
        drawdata.points.add(new Point(x1+20, y1));
       		
		//repaint();
        paintIntoBuffer();
		parent.getControls().getButton("exmark").setSelected(false);
		
	}

	private void customDrawing(MouseEvent e) {
		UndoData data = (UndoData) parent.getSelectedCustomData().clone();
		drawCustomData(data, e);
		parent.getControls().getButton("custom").setSelected(false);
	}

	@SuppressWarnings("unchecked")
	private void exmarkDrawing(MouseEvent e) {
		if(parent.getExMarkData() != null){
			UndoData data = (UndoData) parent.getExMarkData().clone();			
			drawCustomData(data, e);
			parent.getControls().getButton("exmark").setSelected(false);
			return;
		}
		
		DrawingData drawdata = new DrawingData();
		UndoData data = new UndoData();
		data.add(drawdata);
		
		parent.setCurdata(data);
		parent.getData().push(parent.getCurdata());
		parent.getUndo_data().clear();
		setCursor(parent.pencil);
		drawdata.color = getForeground();
		drawdata.size = parent.getBrush_size();
		
		
		int x1 = e.getX();
        int y1 = e.getY();
		
        drawdata.points.add(new Point(x1, y1));
        drawdata.points.add(new Point(x1, y1+15));
        drawdata.points.add(new Point(x1+1, y1));
        drawdata.points.add(new Point(x1+1, y1+15));
        		
        drawdata = new DrawingData();
        data.add(drawdata);
        drawdata.color = getForeground();
        drawdata.size = parent.getBrush_size();
        if(parent.getBrush_size()==1){
            drawdata.points.add(new Point(x1, y1+19));
            drawdata.points.add(new Point(x1, y1+20));
            drawdata.points.add(new Point(x1+1, y1+19));
            drawdata.points.add(new Point(x1+1, y1+20));        	
        } else if (parent.getBrush_size()==2){
            drawdata.points.add(new Point(x1, y1+20));
            drawdata.points.add(new Point(x1, y1+21));
            drawdata.points.add(new Point(x1+1, y1+20));
            drawdata.points.add(new Point(x1+1, y1+21));        	        	
        } else if (parent.getBrush_size()==4){
            drawdata.points.add(new Point(x1, y1+22));
            drawdata.points.add(new Point(x1, y1+23));
            drawdata.points.add(new Point(x1+1, y1+22));
            drawdata.points.add(new Point(x1+1, y1+23));        	        	        	
        }
		//repaint();
        paintIntoBuffer();
		parent.getControls().getButton("exmark").setSelected(false);
	}
	@SuppressWarnings("unchecked")
	private void qumarkDrawing(MouseEvent e) {
		if(parent.getQuMarkData() != null){
			UndoData data = (UndoData) parent.getQuMarkData().clone();			
			drawCustomData(data, e);
			parent.getControls().getButton("exmark").setSelected(false);
			return;
		}
		
		DrawingData drawdata = new DrawingData();
		UndoData data = new UndoData();
		data.add(drawdata);
		
		parent.setCurdata(data);
		parent.getData().push(parent.getCurdata());
		parent.getUndo_data().clear();
		setCursor(parent.pencil);
		drawdata.color = getForeground();
		drawdata.size = parent.getBrush_size();
			
		int x1 = e.getX();
        int y1 = e.getY();
		
        drawdata.points.add(new Point(x1+5, y1+5));
        drawdata.points.add(new Point(x1+8, y1+3));
        drawdata.points.add(new Point(x1+10, y1+1));
        drawdata.points.add(new Point(x1+12, y1+3));
        drawdata.points.add(new Point(x1+15, y1+5));
        drawdata.points.add(new Point(x1+15, y1+8));
        drawdata.points.add(new Point(x1+10, y1+12));
        drawdata.points.add(new Point(x1+10, y1+17));
        		
        drawdata = new DrawingData();
        data.add(drawdata);
        drawdata.color = getForeground();
        drawdata.size = parent.getBrush_size();
        if(parent.getBrush_size()==1){
            drawdata.points.add(new Point(x1+10, y1+20));
            drawdata.points.add(new Point(x1+10, y1+21));
            drawdata.points.add(new Point(x1+11, y1+20));
            drawdata.points.add(new Point(x1+11, y1+21));        	
        } else if (parent.getBrush_size()==2){
            drawdata.points.add(new Point(x1+10, y1+22));
            drawdata.points.add(new Point(x1+10, y1+23));
            drawdata.points.add(new Point(x1+11, y1+22));
            drawdata.points.add(new Point(x1+11, y1+23));        	        	
        } else if (parent.getBrush_size()==4){
            drawdata.points.add(new Point(x1+10, y1+24));
            drawdata.points.add(new Point(x1+10, y1+25));
            drawdata.points.add(new Point(x1+11, y1+24));
            drawdata.points.add(new Point(x1+11, y1+25));        	        	        	
        }
        
		//repaint();
        paintIntoBuffer();
		parent.getControls().getButton("exmark").setSelected(false);
	}

	public void mouseReleased(MouseEvent e) {
		try {
			isDrawSign = false;
			isDrawExMark = false;
			isDrawQuMark = false;
			isDrawCheckMark = false;
			isDrawCustom = false;
			
			//System.out.println("released: "+curdata.points.size());
			e.consume();
			setCursor(parent.pencil);
			parent.getControls().getButton("undo").setEnabled(true);
			parent.getControls().getButton("redo").setEnabled(false);
			repaint();	
			
			//TODO new
			prev_point = null;
			
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void mouseDragged(MouseEvent e) {
		e.consume();
		if (isDrawSign || isDrawExMark || isDrawQuMark || isDrawCheckMark || isDrawCustom ) return;
		//System.out.println("drag");
		try {				
			int x1 = e.getX();
			int y1 = e.getY();
			//DrawingData drawdata = (DrawingData) parent.getCurdata().getDrawData().get(0);
			Point newp = new Point(x1, y1); 
			cur_drawdata.points.add(newp);
			
			//TODO new
			if (prev_point != null) 
				{ g2.drawLine(prev_point.x, prev_point.y, x1, y1); } 
			else 
				{ g2.drawLine(x1, y1, x1, y1); }
			prev_point = (Point) newp.clone();
			
			repaint();
			//repaint(x1-50, y1-50, x1+50, y1+50);
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}
	}

	public void mouseMoved(MouseEvent e) {		
	}
	
	protected void paintComponent(Graphics g) {
		g.drawImage( buffer, 0, 0, this );

	}
	
	protected void paintIntoBuffer() {
		int i=0;
		try {
			g2.drawImage(parent.img, 0, 0, null);
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);

			Iterator undoIterator = parent.getData().iterator();
			while (undoIterator.hasNext()) {
				UndoData undo = (UndoData) undoIterator.next();
				
				Iterator iter = undo.getDrawData().iterator();
				while (iter.hasNext()) {
					DrawingData element = (DrawingData) iter.next();
					
					Point prev_point = null;
					int bsize = element.size;
					g2.setStroke(new BasicStroke(bsize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2.setColor(element.color);
					
//					Iterator it = element.points.iterator();
//					while( it. hasNext() ){
//						i=i+1;
//						Point p = (Point) it.next();
//						if (prev_point != null) { g2.drawLine(prev_point.x, prev_point.y, p.x, p.y); } 
//							else { g2.drawLine(p.x, p.y, p.x, p.y); }
//						prev_point = (Point) p.clone();
//					} // end iterator
					for (int j = 0; j < element.points.size(); j++) {
						i=i+1;
						Point p = (Point) element.points.get(j);
						if (prev_point != null) { g2.drawLine(prev_point.x, prev_point.y, p.x, p.y); } 
							else { g2.drawLine(p.x, p.y, p.x, p.y); }
						prev_point = (Point) p.clone();						
					}
					

				}// end while					
				
			}
			
			//System.out.println("i: "+i);			
			
			//g2.dispose();
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}			
		//super.paintComponent(g);
		//System.out.println("i: "+i);
		repaint();
	}
	
	
	protected void paintComponent2(Graphics g) {
		long time1 = System.currentTimeMillis();
		int i=0;
		try {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			//Graphics2D g2 = parent.img.createGraphics();	
			g2.drawImage(parent.img, 0, 0, null);
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);

			Iterator undoIterator = parent.getData().iterator();
			while (undoIterator.hasNext()) {
				UndoData undo = (UndoData) undoIterator.next();
				
				Iterator iter = undo.getDrawData().iterator();
				while (iter.hasNext()) {
					DrawingData element = (DrawingData) iter.next();
					
					Point prev_point = null;
					int bsize = element.size;
					g2.setStroke(new BasicStroke(bsize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2.setColor(element.color);
					
//					Iterator it = element.points.iterator();
//					while( it. hasNext() ){
//						i=i+1;
//						Point p = (Point) it.next();
//						if (prev_point != null) { g2.drawLine(prev_point.x, prev_point.y, p.x, p.y); } 
//							else { g2.drawLine(p.x, p.y, p.x, p.y); }
//						prev_point = (Point) p.clone();
//					} // end iterator
					for (int j = 0; j < element.points.size(); j++) {
						i=i+1;
						Point p = (Point) element.points.get(j);
						if (prev_point != null) { g2.drawLine(prev_point.x, prev_point.y, p.x, p.y); } 
							else { g2.drawLine(p.x, p.y, p.x, p.y); }
						prev_point = (Point) p.clone();						
					}
					

				}// end while					
				
			}
			
			//System.out.println("i: "+i);			
			
			g2.dispose();
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}			
		//super.paintComponent(g);
		long time2 = System.currentTimeMillis();
		System.out.println("i: "+i);
		System.out.println("time to paint: "+(time2-time1));
	}
	
	
	protected void paintIntoImage() {
		try {
			Graphics gImg = parent.img.getGraphics();
			Graphics2D g2 = (Graphics2D) gImg;
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			
			Iterator undoIterator = parent.getData().iterator();
			while (undoIterator.hasNext()) {
				UndoData undo = (UndoData) undoIterator.next();
				Iterator iter = undo.getDrawData().iterator();
				while (iter.hasNext()) {
					DrawingData element = (DrawingData) iter.next();
					
					Point prev_point =null;
					int bsize = element.size;
					g2.setStroke(new BasicStroke(bsize));
					g2.setColor(element.color);
		
					Iterator it = element.points.iterator();
					while( it. hasNext() ){
						Point p = (Point) it.next();
						if (prev_point != null) { g2.drawLine(prev_point.x, prev_point.y, p.x, p.y); } 
							else { g2.drawLine(p.x, p.y, p.x, p.y); }
						prev_point = (Point) p.clone();				
					} // end iterator
				}// end while
				
			}
				

			
			g2.dispose();
			gImg.dispose();
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}
		
	}

	public void update(Graphics g) {
		//System.out.println("update");
		paint(g);
	}

	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredSize();

	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - maxUnitIncrement;
		} else {
			return visibleRect.height - maxUnitIncrement;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		try {
			//Get the current position.
			int currentPosition = 0;
			if (orientation == SwingConstants.HORIZONTAL) {
				currentPosition = visibleRect.x;
			} else {
				currentPosition = visibleRect.y;
			}
	
			//Return the number of pixels between currentPosition
			//and the nearest tick mark in the indicated direction.
			if (direction < 0) {
				int newPosition = currentPosition
						- (currentPosition / maxUnitIncrement) * maxUnitIncrement;
				return (newPosition == 0) ? maxUnitIncrement : newPosition;
			} else {
				return ((currentPosition / maxUnitIncrement) + 1)
						* maxUnitIncrement - currentPosition;
			}
		} catch (Exception ex) {
			ExceptionLoggerFile.Log(ex);
		}
		return 1;
	}

	public void deactivatePanel() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		setCursor(Cursor.getDefaultCursor());
	}
	
	public void activatePanel() {
		System.out.println("width: "+parent.img.getWidth());
		System.out.println("height: "+parent.img.getHeight());
		setPreferredSize(new Dimension(parent.img.getWidth(), parent.img.getHeight()));
		
		//TODO new
		buffer = new BufferedImage(parent.img.getWidth(), parent.img.getHeight(),BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D) buffer.getGraphics();
		g2.drawImage(parent.img, 0, 0, null);		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		
		setCursor(parent.pencil);
		addMouseMotionListener(this);			
		addMouseListener(this);
		requestFocusInWindow();
		repaint();
	}

	public void setDrawSign(boolean b) {
		isDrawSign = b;
	}

	public void setExMarkSign(boolean b) {
		isDrawExMark = b;		
	}
	public void setCheckMarkSign(boolean b) {
		isDrawCheckMark = b;		
	}
	public void setQuMarkSign(boolean b) {
		isDrawQuMark = b;		
	}

	public void setCustomSign(boolean b) {
		isDrawCustom = b;
		
	}
	
	
	
}
