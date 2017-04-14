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
package com.aplana.dbmi.card.download.actionhandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class GetDocLinksAction extends FileActionHandler
{
	public final static String PARAM_CARD_ID = "cardId";
	public final static ObjectId ATTR_DOCLINKS = new ObjectId( CardLinkAttribute.class, "DOCLINKS" );

	@Override
	public void process( HttpServletRequest request, HttpServletResponse response ) throws DataException
	{
		String cardId = request.getParameter( PARAM_CARD_ID );
		if( cardId == null )
		{
            logger.error( "Parameter " + PARAM_CARD_ID + " is null" );
            throw new DataException( "Parameter " + PARAM_CARD_ID + " is null" );
        }

		try
		{
			StringBuffer resp = new StringBuffer(); // servlet response
			ObjectId objId = new ObjectId(Card.class, Long.parseLong(cardId) );

			DataServiceBean ds = getServiceBean();
			Card card = (Card)ds.getById( objId );

			CardLinkAttribute links_attr = (CardLinkAttribute)card.getAttributeById( ATTR_DOCLINKS );
			if( links_attr != null )
			{
				ObjectId[] links = links_attr.getIdsArray();
				if( links != null )
				{
					// we have all attachemnts card Ids; now get their file names
					Card[] linked_cards = new Card[links.length];
					for( int i=0; i<links.length; i++ )
					{
						linked_cards[i] = (Card) ds.getById( new ObjectId(Card.class, links[i].getId()) );
						// getById() may return null? throw exception?.. just skip?...
					}
					for( int i=0; i<links.length; i++ )
					{
						if( i>0 ) resp.append( ";" );
						resp.append( links[i].getId().toString() );
						resp.append( "=" );
						if( linked_cards[i] != null )
							if( linked_cards[i].getFileName() != null )
								resp.append( linked_cards[i].getFileName() );
							else
								resp.append( "(null)" ); // WTF
						else
							resp.append( "(null)" ); // WTF
					}
				}
			}
			response.setContentType( "text/plain" );
			PrintWriter out = response.getWriter();
			out.write( resp.toString() );
		}
		catch( ServiceException se )
		{
			logger.error( "GetDocLinksAction got ServiceException: ", se );
			throw new DataException( se );
		}
		catch( IOException ioe )
		{
			logger.error( "GetDocLinksAction got IOException: ", ioe );
			throw new DataException( ioe );
		}
	}
}

