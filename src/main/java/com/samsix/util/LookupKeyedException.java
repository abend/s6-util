package com.samsix.util;

public abstract class LookupKeyedException
    extends
        SamSixException
    implements
        LookupKeyed
{
    private static final long serialVersionUID = -739994467504207306L;

    private final String _lookupKey;


    public LookupKeyedException( final String lookupKey )
    {
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final boolean isUserError )
    {
        setUserError( isUserError );
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final String msg,
                                 final boolean isUserError )
    {
        super( msg, isUserError );
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final String msg,
                                 final Throwable cause,
                                 final boolean isUserError )
    {
        super( msg, cause, isUserError );
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final String msg,
                                 final Throwable cause )
    {
        super( msg, cause );
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final String msg )
    {
        super( msg );
        _lookupKey = lookupKey;
    }


    public LookupKeyedException( final String lookupKey,
                                 final Throwable cause )
    {
        super( cause );
        _lookupKey = lookupKey;
    }


    @Override
    public String getLookupKey()
    {
        return _lookupKey;
    }


    @Override
    public String getLookupKeyType()
    {
        return getClass().getSimpleName();
    }
}
