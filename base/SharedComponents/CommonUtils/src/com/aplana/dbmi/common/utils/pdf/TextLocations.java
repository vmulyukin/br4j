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
package com.aplana.dbmi.common.utils.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itextpdf.text.pdf.DocumentFont;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

/**
 * ����� ��� ����������� �������������� ������ ������ � PDF ���������
 * �� ������ iText LocationTextExtractionStrategy
 */

public class TextLocations extends LocationTextExtractionStrategy {
	private String seek;
    private final List<TextChunk> locationalResult = new ArrayList<TextChunk>();
    private final List<TextChunk> tLineChunks = new ArrayList<TextChunk>();
    private TextChunk lastPosition = null;
    
    public TextLocations(String seek) {
    	this.seek = seek;
    }

    public String getResultantText(){
        Collections.sort(locationalResult);
        final StringBuffer tLine = new StringBuffer();

        TextChunk lastChunk = null;
        for (TextChunk chunk : locationalResult) {

            if (lastChunk == null){
            	appendLine(tLine, chunk);
            } else {
                if (chunk.sameLine(lastChunk)){
                	appendLine(tLine, chunk);
                } else {
                	endLine(tLine, tLineChunks);
                	appendLine(tLine, chunk);
                }
            }
            lastChunk = chunk;
        }
        endLine(tLine, tLineChunks);

        return tLine.toString();

    }
    private void appendLine(final StringBuffer tLine, final TextChunk chunk)
    {
    	tLineChunks.add(chunk);
    	tLine.append(chunk.text);
    }

    private void endLine(final StringBuffer tLine, final List<TextChunk> tLineChunks)
    {
    	final String newLine = tLine.toString();
    	if (!newLine.isEmpty() && newLine.contains(seek)) {
            lastPosition = tLineChunks.get(tLineChunks.size() - 1);
        }
    	tLineChunks.clear();
    	tLine.delete(0, tLine.length());
    }

    public void renderText(TextRenderInfo renderInfo) {
        LineSegment segment = renderInfo.getAscentLine();
        LineSegment descLine = renderInfo.getDescentLine();
        float fontSize = segment.getEndPoint().get((Vector.I2)) - descLine.getEndPoint().get((Vector.I2));;
        TextChunk location = new TextChunk(renderInfo.getText(), segment.getStartPoint(), segment.getEndPoint(), 
        		renderInfo.getSingleSpaceWidth(), renderInfo.getFont(), fontSize);
        locationalResult.add(location);
    }

    public TextChunk getLastPosition() {
    	return lastPosition;
    }

    /**
     * Represents a chunk of text, it's orientation, and location relative to the orientation vector
     */
    public static class TextChunk implements Comparable<TextChunk>{
        /** the text of the chunk */
        final String text;
        /** the starting location of the chunk */
        final Vector startLocation;
        /** the ending location of the chunk */
        final Vector endLocation;
        /** unit vector in the orientation of the chunk */
        final Vector orientationVector;
        /** the orientation as a scalar for quick sorting */
        final int orientationMagnitude;
        /** perpendicular distance to the orientation unit vector (i.e. the Y position in an unrotated coordinate system)
         * we round to the nearest integer to handle the fuzziness of comparing floats */
        final int distPerpendicular;
        /** distance of the start of the chunk parallel to the orientation unit vector (i.e. the X position in an unrotated coordinate system) */
        final float distParallelStart;
        /** distance of the end of the chunk parallel to the orientation unit vector (i.e. the X position in an unrotated coordinate system) */
        final float distParallelEnd;
        /** the width of a single space character in the font of the chunk */
        final float charSpaceWidth;
        DocumentFont font;
        float fontSize;
        
        public TextChunk(String string, Vector startLocation, Vector endLocation, float charSpaceWidth, DocumentFont font, float fontSize) {
            this.text = string;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.charSpaceWidth = charSpaceWidth;
            this.font = font;
            this.fontSize = fontSize;
            
            orientationVector = endLocation.subtract(startLocation).normalize();
            orientationMagnitude = (int)(Math.atan2(orientationVector.get(Vector.I2), orientationVector.get(Vector.I1))*1000);

            // see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
            // the two vectors we are crossing are in the same plane, so the result will be purely
            // in the z-axis (out of plane) direction, so we just take the I3 component of the result
            Vector origin = new Vector(0,0,1);
            distPerpendicular = (int)(startLocation.subtract(origin)).cross(orientationVector).get(Vector.I3);

            distParallelStart = orientationVector.dot(startLocation);
            distParallelEnd = orientationVector.dot(endLocation);
        }

		public float getFontSize() {
			return fontSize;
		}

		public void setFontSize(float fontSize) {
			this.fontSize = fontSize;
		}

		public DocumentFont getFont() {
			return font;
		}

		public void setFont(DocumentFont font) {
			this.font = font;
		}

		private void printDiagnostics(){
            System.out.println("Text (@" + startLocation + " -> " + endLocation + "): " + text);
            System.out.println("orientationMagnitude: " + orientationMagnitude);
            System.out.println("distPerpendicular: " + distPerpendicular);
            System.out.println("distParallel: " + distParallelStart);
        }
        
        /**
         * @param as the location to compare to
         * @return true is this location is on the the same line as the other
         */
        public boolean sameLine(TextChunk as){
            if (orientationMagnitude != as.orientationMagnitude) return false;
            if (distPerpendicular != as.distPerpendicular) return false;
            return true;
        }

        /**
         * Computes the distance between the end of 'other' and the beginning of this chunk
         * in the direction of this chunk's orientation vector.  Note that it's a bad idea
         * to call this for chunks that aren't on the same line and orientation, but we don't
         * explicitly check for that condition for performance reasons.
         * @param other
         * @return the number of spaces between the end of 'other' and the beginning of this chunk
         */
        public float distanceFromEndOf(TextChunk other){
            float distance = distParallelStart - other.distParallelEnd;
            return distance;
        }
        
        /**
         * Compares based on orientation, perpendicular distance, then parallel distance
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TextChunk rhs) {
            if (this == rhs) return 0; // not really needed, but just in case
            
            int rslt;
            rslt = compareInts(orientationMagnitude, rhs.orientationMagnitude);
            if (rslt != 0) return rslt;

            rslt = compareInts(distPerpendicular, rhs.distPerpendicular);
            if (rslt != 0) return rslt; 
            
            return 0;
        }
        
        public Vector getStartLocation() {
        	return startLocation;
        }
        
        public Vector getEndLocation() {
        	return endLocation;
        }

        /**
         *
         * @param int1
         * @param int2
         * @return comparison of the two integers
         */
        private static int compareInts(int int1, int int2){
            return int1 == int2 ? 0 : int1 < int2 ? -1 : 1;
        } 
    }
}