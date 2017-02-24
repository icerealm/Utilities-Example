/**
 * Copyright (c) DST Output, LLC, 2005-2013
 * All rights reserved.
 * 
 * Unpublished rights reserved under the copyright laws of the United States. The
 * Software contained on this media is proprietary to and embodies the confidential
 * technology of DST Output, LLC.  Possession, use, duplication or dissemination of 
 * the software and media is authorized only pursuant to a valid written license 
 * from DST Output, LLC.
 *
 * RESTRICTED RIGHTS LEGEND
 * Use, duplication, or disclosure by the U.S. Government is subject to restrictions
 * as set forth in Subparagraph (c) (1) (ii) of DFARS 252.227-7013 or in FAR
 * 52.227-19, as applicable.
 * 
 * $Header: //DICE/batch/GenericMsgListener/Main/GenericMsgListener/src/com/dsto/dice/gml/util/JAXBObjectParserManagers.java#2 $
 * $Author: dt85715 $
 * $Change: 212010 $
 * $DateTime: 
 */
package com.example.util;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

public class JAXBObjectParserManager 
{
	private static JAXBObjectParserManager _manager;
	private DateFormat df;
	private TimeZone timezone;
	private static final int __max_tree_level = 7;
	
	private JAXBObjectParserManager()
	{
	}
	
	public void setDateFormatAndTimeZone(DateFormat df, TimeZone timeZone)
	{
		this.df = df;
		this.timezone = timeZone;
		this.df.setTimeZone(timeZone);
	}
	
	private Object getMethodValue(final Object input, final String field) throws JAXBObjectParserException
	{
		Method m = null;
		try
		{
			Object obj = input.getClass();

    		if(input instanceof ArrayList)
			{
    			Object embededObjValue = ((ArrayList) input).get(0);	
				m = embededObjValue.getClass().getMethod("get" + field);	
	    		if(m != null)
				{
					return m.invoke(embededObjValue);
				}
	    		m = embededObjValue.getClass().getMethod("get" + field);				
			}
			else
			{
				m = input.getClass().getMethod("get" + field);
				if(m != null)
				{
					return m.invoke(input);
				}
			}
    		
			return m;
			
		}
		catch (Exception e)
		{
			throw new JAXBObjectParserException(e);
		}
	}
	
	private Object castToString(Object value)
	{
		if(value != null)
		{
			if ( value instanceof String )
			{
				return value;
			}
			else if ( value instanceof XMLGregorianCalendar )
			{				
				if( this.df == null || this.timezone == null)
				{
					this.df = new SimpleDateFormat("MMddyyyy");
					XMLGregorianCalendar date  = (XMLGregorianCalendar) value;		
					this.timezone = date.toGregorianCalendar().getTimeZone();
					this.df.setTimeZone(this.timezone);
				}
				return df.format(((XMLGregorianCalendar) value).toGregorianCalendar().getTime());
			}
			else
			{
				return value.toString();
			}
		}
		return value;
	}
	
	public static JAXBObjectParserManager getInstance()
	{
		if( _manager == null)
		{
			_manager = new JAXBObjectParserManager();
		}
		return _manager;
	}
	
	public Object parseObjectValue(Object jaxbObject, String field) throws JAXBObjectParserException
	{
		String[]dataFields = field.split("\\.");
		int size = dataFields.length;
	
		
		if ( size > __max_tree_level )
		{
			throw new JAXBObjectParserException("Can not parse object value that has hierarchy level more than " + __max_tree_level);
		}
		
		if( size <= 1 )
		{
			Object retObject = getMethodValue(jaxbObject, dataFields[0]);
			if(retObject == null)
			{
				throw new JAXBObjectParserException("Can not parse object value of field '" + field + "' that doesn't contain in the message");
			}
			else
			{
				return castToString( retObject );
			}			
		}
		else
		{
			Object retObject = getMethodValue(jaxbObject, dataFields[0]);
			if(retObject == null)
			{
				throw new JAXBObjectParserException("Can not parse object value of field '" + field + "' that doesn't contain in the message");
			}
			
			for( int i=1; i<size; i++)
			{
				retObject = getMethodValue(retObject, dataFields[i]);
				if(retObject == null)
				{
					throw new JAXBObjectParserException("Can not parse object value of field '" + field + "' that doesn't contain in the message");
				}
			}
			return castToString( retObject );
		}
	}
}