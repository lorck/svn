package net.sf.l2j.loginserver.beans;

import java.net.InetAddress;

/**
 * 
 * A class that represent the information for a ban
 *
 */
public class BanInfo
{
    /**
     * The ip adress banned
     */
    private InetAddress _ipAddress;

    /**
     * expiration of the ban : represent the difference, measured in milliseconds, between the expiration time of the ban and midnight, January 1, 1970 UTC.
     */
    private long        _expiration;

    /**
     * Constructor
     * @param ipAddress
     * @param expiration
     */
    public BanInfo(InetAddress ipAddress, long expiration)
    {
        _ipAddress = ipAddress;
        _expiration = expiration;
    }

    public InetAddress getAddress()
    {
        return _ipAddress;
    }
    
    /**
     * check if the ban is eternal (equal to 0)
     * @return true or false
     */
    public boolean isBanEternal ()
    {
        return _expiration == 0;
    }
    
    /**
     * Check if ban expired : current time > _expiration
     * @return true if ban is expired
     */
    public boolean hasExpired()
    {
        return System.currentTimeMillis() > _expiration;
    }
}