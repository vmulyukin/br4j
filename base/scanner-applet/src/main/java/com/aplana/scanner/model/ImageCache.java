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
package com.aplana.scanner.model;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;


/**
 * Cache for scaled images.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ImageCache {
	private static final ImageCache INSTANCE = new ImageCache();
	
	private final Map<CacheEntry, SoftReference<Image>> cache;
	
	private static final class CacheEntry {
		private final Object source;
		private final int width;
		private final int height;
		
		public CacheEntry(Object source, int width, int height) {
			if (source == null)
				throw new IllegalArgumentException("Source must not be null");
			
			this.source = source;
			this.width = width;
			this.height = height;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + source.hashCode();
			result = prime * result + width;
			result = prime * result + height;
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof CacheEntry))
				return false;
			
			CacheEntry other = (CacheEntry)obj;
			return (other.source.equals(source) && other.height == height && other.width == width);
		}
	}
	
	/**
	 * Default constructor.
	 */
	protected ImageCache() {
		cache = new HashMap<CacheEntry, SoftReference<Image>>();
	}

	/**
   * Gets a global instance of the <code>ImageCache</code>.
   *
   * @return shared instance of the cache
   */
	public static ImageCache getInstance() {
		return INSTANCE;
	}
	
	/**
   * Gets a scaled image of the specified size for <code>Image</code>.
   *
   * @param  component  the {@link Component} the image is displayed in
   * @param  image      the {@link Image} to scale
   * @param  width      the scaled image width
   * @param  height     the scaled image height
   * @return an image of the specified size
   */
  public Image getImage(Component component, Image image, int width, int height) {
		CacheEntry entry = new CacheEntry(image, width, height);
		SoftReference<Image> ref = cache.get(entry);
		Image cachedImage;
		if (ref == null || (cachedImage = ref.get()) == null) {
			cachedImage = createImage(component, image, width, height);
			cache.put(entry, new SoftReference<Image>(cachedImage));
		}
		return cachedImage;
  }
  
  /**
   * Gets a scaled image of the specified size from a {@link Page}.
   *
   * @param  component  the {@link Component} the image is displayed in
   * @param  page       the page
   * @param  width      the scaled image width
   * @param  height     the scaled image height
   * @return an image of the specified size
   * @throws IOException if an error occurs during reading the page image
   */
  public Image getImage(Component component, Page page, int width, int height)
  				throws IOException {
  	CacheEntry entry = new CacheEntry(page, width, height);
  	SoftReference<Image> ref = cache.get(entry);
  	Image cachedImage;
  	if (ref == null || (cachedImage = ref.get()) == null) {
  		BufferedImage image = page.getImageData().getImage();
  		cachedImage = createImage(component, image, width, height);
  		cache.put(entry, new SoftReference<Image>(cachedImage));
  	}
  	return cachedImage;
  }
  
  private static Image createImage(Component component, Image image, int width, int height) {
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);
		if (iw > 0 && ih > 0) {
			float aspectRatio = (float)iw / (float)ih;
			int targetWidth;
			int targetHeight;
			if (iw > ih) {
				targetWidth = width;
				targetHeight = (int)(targetWidth / aspectRatio);
				if (targetHeight > height) {
					targetHeight = height;
					targetWidth = (int)(aspectRatio * targetHeight);
				}
			} else {
		    targetHeight = height;
		    targetWidth = (int)(aspectRatio * targetHeight);
		    if (targetWidth > width) {
		    	targetWidth = width;
					targetHeight = (int)(targetWidth / aspectRatio);
		    }
			}
			if (targetWidth != iw || targetHeight != iw) {
				Image cachedImage;
				GraphicsConfiguration gc;
				if (component != null && (gc = component.getGraphicsConfiguration()) != null) {
					cachedImage = gc.createCompatibleImage(targetWidth,
									targetHeight, Transparency.TRANSLUCENT);
				} else
					cachedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics imageG = cachedImage.getGraphics();
				if (imageG instanceof Graphics2D) {
					((Graphics2D)imageG).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
									RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				}
				imageG.drawImage(image, 0, 0, targetWidth, targetHeight, 0, 0, iw, ih, null);
				imageG.dispose();
				return cachedImage;
			}
			return image;
		}
		return null;
  }
}
