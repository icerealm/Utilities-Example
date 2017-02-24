package com.example.util;

public class JAXBObjectParserException extends Exception
{
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 13L;

	public JAXBObjectParserException(Exception e)
	{
		super(e);
	}
	
	public JAXBObjectParserException(String msg)
	{
		super(msg);
	}
	
	public JAXBObjectParserException(Throwable t)
	{
		super(t);
	}

}
