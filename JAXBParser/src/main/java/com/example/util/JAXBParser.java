package com.example.util;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

public class JAXBParser 
{
	private static JAXBParser _manager;
	private DateFormat df;
	private TimeZone timezone;
	private static final int __max_tree_level = 7;
	
	private JAXBParser()
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
	
	public static JAXBParser getInstance()
	{
		if( _manager == null)
		{
			_manager = new JAXBParser();
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