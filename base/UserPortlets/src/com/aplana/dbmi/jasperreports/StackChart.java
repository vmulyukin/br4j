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
package com.aplana.dbmi.jasperreports;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class StackChart implements Chart {
	
	public BufferedImage getChart(Document doc, Map parameters, Connection conn) {
		// ��������� ����������������� ������������� dataset
		DataModel dataModel;
		Dataset datasetInner;
		try {
			dataModel = readConfigChar(doc);
			datasetInner = getDataset(dataModel, conn);
		} catch (Exception e) {
			System.out.println("������ ��� ������������� dataset");
			e.printStackTrace();
			return null;
		}
		
		DefaultCategoryDataset result = new DefaultCategoryDataset();
		Iterator iterEl = datasetInner.elements.iterator();
		while (iterEl.hasNext()) {
			String element = (String)iterEl.next();
			
			Iterator iterKind = datasetInner.kinds.iterator();
			while (iterKind.hasNext()) {
				String kind = (String)iterKind.next();
				
				Iterator iterGr = datasetInner.groups.iterator();
				while (iterGr.hasNext()) {
					String group = (String)iterGr.next();
					
					Long value = datasetInner.getValue(element, kind, group);
					if (value != null) {
						result.addValue(value, element + " (" + kind + ")", group);
					}  else {
						result.addValue(0, element + " (" + kind + ")", group);
					}
				}
			}
		}
		
		JFreeChart chartTrue = ChartFactory.createStackedBarChart(
				datasetInner.title, 
				null, 
				null, 
				result, 
				PlotOrientation.VERTICAL, 
				true, 
				true, 
				false
		);
		
		GroupedStackedBarRenderer rendererTrue = new GroupedStackedBarRenderer();
		KeyToGroupMap mapTrue;
	    if (datasetInner.elements.size() > 0) {
	    	String firstEl = (String)datasetInner.elements.iterator().next();
			mapTrue = new KeyToGroupMap(firstEl);
	    } else {
	    	mapTrue = new KeyToGroupMap();
	    }
	    
	    Map positionsOfRecords = new HashMap(); // ���������� �����, ��� ��������� ������ �����
	    int cRecords = 0;
	    iterEl = datasetInner.elements.iterator();
	    while (iterEl.hasNext()) {
	    	String element = (String)iterEl.next();
	    	
	    	Iterator iterKind = datasetInner.kinds.iterator();
	    	while (iterKind.hasNext()) {
	    		String kind = (String)iterKind.next();
	    		
	    		if (positionsOfRecords.get(kind) == null)
					positionsOfRecords.put(kind, new LinkedList());
				List positions = (List)positionsOfRecords.get(kind);			
	    		
	    		mapTrue.mapKeyToGroup(element+" ("+kind+")", element);
	    		positions.add(new Long(cRecords));
	    		cRecords++;	
	    	}
	    }
	    
	    rendererTrue.setSeriesToGroupMap(mapTrue);
	    
	    rendererTrue.setItemMargin(0.05);
	    Iterator iterKind = datasetInner.kinds.iterator();
	    while (iterKind.hasNext()) {
	    	String kind = (String)iterKind.next();
	    	
	    	Paint p = (Color)datasetInner.kindsColor.get(kind);
	    	Iterator positions = ((List)positionsOfRecords.get(kind)).iterator();
	    	while (positions.hasNext()) {
	    		int posit = ((Long)positions.next()).intValue();
	    		rendererTrue.setSeriesPaint(posit, p);
	    	}
	    }
		
	    SubCategoryAxis domainAxisTrue = new SubCategoryAxis("");
	    domainAxisTrue.setCategoryMargin(0.05);
	    iterEl = datasetInner.elements.iterator();
	    while (iterEl.hasNext()) {
	    	String element = (String)iterEl.next();
	    	domainAxisTrue.addSubCategory(element);
	    }
	     
	    CategoryPlot plotTrue = (CategoryPlot) chartTrue.getPlot();
	    plotTrue.setDomainAxis(domainAxisTrue);
	    //plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
	    plotTrue.setRenderer(rendererTrue);
	    plotTrue.setFixedLegendItems(createLegendItems(datasetInner));
	    
	    // �������������� �����������
	    /*
	    BufferedImage im = chartTrue.createBufferedImage(1164, 832);
		BufferedImage rotateIm = new BufferedImage(832, 1164, im.getType());
	    Graphics2D gr = rotateIm.createGraphics();
	    gr.rotate(Math.toRadians(-90), rotateIm.getHeight()/2, rotateIm.getHeight()/2);
	    gr.drawImage(im, null, 0, 0);
	    gr.dispose();
	    return rotateIm;
	    */
	    
	    BufferedImage im = chartTrue.createBufferedImage(1204, 792);
	    return im;
	}
	
	private ResultSet getDataFromDB(Connection conn, String query) throws SQLException {
		return conn.createStatement().executeQuery(query);
	}
	
	private LegendItemCollection createLegendItems(Dataset dataset) {
		LegendItemCollection result = new LegendItemCollection();
		Iterator iterKind = dataset.kinds.iterator();
		while (iterKind.hasNext()) {
			String kind = (String)iterKind.next();
			LegendItem item = new LegendItem(kind, (Color)dataset.kindsColor.get(kind));
			result.add(item);
		}
	    return result;
	}
	
	private DataModel readConfigChar(Document doc) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression queryExpression = xpath.compile("/chart/query");
		XPathExpression groupExpression = xpath.compile("/chart/columnGroup");
		XPathExpression elementsExpression = xpath.compile("/chart/elements/element");
		XPathExpression kindsExpression = xpath.compile("/chart/kinds/kind");
		XPathExpression mapsExpression = xpath.compile("/chart/maps/map");
		XPathExpression titleExpression = xpath.compile("/chart/title");
		
		Element queryEl = (Element)queryExpression.evaluate(doc, XPathConstants.NODE);
		String query = queryEl.getTextContent();
		
		Element titleEl = (Element)titleExpression.evaluate(doc, XPathConstants.NODE);
		String title = titleEl.getTextContent();
		
		Element groupEl = (Element)groupExpression.evaluate(doc, XPathConstants.NODE);
		String group = groupEl.getAttribute("column");
		
		List elements = new LinkedList();
		NodeList elementNodes = (NodeList)elementsExpression.evaluate(doc, XPathConstants.NODESET);
		for (int i=0; i < elementNodes.getLength(); i++) {
			Element elementEl = (Element)elementNodes.item(i);
			String element = elementEl.getAttribute("name");
			elements.add(element);
		}
		
		List kinds = new LinkedList();
		Map kindsColor = new HashMap();
		NodeList kindNodes = (NodeList)kindsExpression.evaluate(doc, XPathConstants.NODESET);
		for (int i=0; i < kindNodes.getLength(); i++) {
			Element kindEl = (Element)kindNodes.item(i);
			String kind = kindEl.getAttribute("name");
			kinds.add(kind);
			
			String colorHex = kindEl.getAttribute("color");
			if (colorHex != null) {
				Color color = new Color(Integer.parseInt(colorHex, 16));
				kindsColor.put(kind, color);
			}
		}
		
		DataModel dataModel = new DataModel(elements, kinds, group);
		dataModel.query = query;
		dataModel.title = title;
		dataModel.setKindsColor(kindsColor);
		
		NodeList mapNodes = (NodeList)mapsExpression.evaluate(doc, XPathConstants.NODESET);
		for (int i=0; i < mapNodes.getLength(); i++) {
			Element mapEl = (Element)mapNodes.item(i);
			String element = mapEl.getAttribute("element");
			String kind = mapEl.getAttribute("kind");
			String column = mapEl.getAttribute("column");
			dataModel.setColumn(element, kind, column);
		}	
		
		return dataModel;
	}
	
	private Dataset getDataset(DataModel dataModel, Connection conn) throws SQLException, ClassNotFoundException {
		Dataset dataset = new Dataset(dataModel.elements, dataModel.kinds);
		dataset.title = dataModel.title;
		dataset.kindsColor = dataModel.kindsColor;
		
		ResultSet dataDB = getDataFromDB(conn, dataModel.query);
		while (dataDB.next()) {
			String group = dataDB.getString(dataModel.columnGroup);
			Iterator iterEl = dataset.elements.iterator();
			while (iterEl.hasNext()) {
				String element = (String) iterEl.next();
				
				Iterator iterKids = dataset.kinds.iterator();
				while (iterKids.hasNext()) {
					String kind = (String) iterKids.next();
					
					String column = dataModel.getColumn(element, kind);
					if (column != null) {
						Long value = dataDB.getLong(column);
						dataset.setValue(element, kind, group, value);
					}
				}
			}
		}
		return dataset;
	}
	
	private class Dataset {
		String title;
		List elements;
		List kinds;
		Map kindsColor;
		List groups;
		Map values;
		
		Dataset(List elements, List kinds) {
			this.elements = elements;
			this.kinds = kinds;
			groups = new LinkedList();
			
			values = new HashMap();
			Iterator iterEl = elements.iterator();
			while (iterEl.hasNext()) {
				
				Map mapKinds = new HashMap();
				Iterator iterKind = kinds.iterator();
				while (iterKind.hasNext()) {
					
					Map mapGroups = new HashMap();
					mapKinds.put(iterKind.next(), mapGroups);
				}
				values.put(iterEl.next(), mapKinds);
			}
		}
		
		Long getValue(String element, String kind, String group) {
			Map mapKind = (Map)values.get(element);
			Map mapGroup = (Map)mapKind.get(kind);
			return (Long)mapGroup.get(group);
		}
		
		void setValue(String element, String kind, String group, Long value) {
			Map mapKind = (Map)values.get(element);
			Map mapGroup = (Map)mapKind.get(kind);
			mapGroup.put(group, value);
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}
	}
	
	private class DataModel {
		String title;
		String query;
		
		List elements;
		List kinds;
		Map kindsColor;
		String columnGroup;
		Map columns;
		
		DataModel(List elements, List kinds, String columnGroup) {
			this.elements = elements;
			this.kinds = kinds;
			this.columnGroup = columnGroup;
			
			columns = new HashMap();
			Iterator iterEl = elements.iterator();
			while (iterEl.hasNext()) {
				Map mapKinds = new HashMap();
				columns.put(iterEl.next(), mapKinds);
			}
			kindsColor = new HashMap();
		}
		
		void setColumn(String element, String kind, String column) {
			Map mapKinds = (Map)columns.get(element);
			mapKinds.put(kind, column);
		}
		
		String getColumn(String element, String kind) {
			Map mapKinds = (Map)columns.get(element);
			return (String)mapKinds.get(kind);
		}

		public void setKindsColor(Map kindsColor) {
			this.kindsColor = kindsColor;
		}		
	}
}
