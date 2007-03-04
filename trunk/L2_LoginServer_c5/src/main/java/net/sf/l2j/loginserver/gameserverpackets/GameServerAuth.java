/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.loginserver.gameserverpackets;

import java.util.logging.Logger;

import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

/**
 * Format: cccddb
 * c desired ID
 * c accept alternative ID
 * c reserve Host
 * s ExternalHostName
 * s InetranlHostName
 * d max players
 * d hexid size
 * b hexid
 * @author -Wooden-
 *
 */
public class GameServerAuth extends ClientBasePacket
{
	protected static Logger _log = Logger.getLogger(GameServerAuth.class.getName());
	private byte[] _hexID;
	private int _desiredID;
	private boolean _hostReserved;
	private boolean _acceptAlternativeID;
	private int _max_palyers;
	private int _port;
	private String _reserved;
	private String _netConfig;
	
	/**
	 * @param decrypt
	 */
	public GameServerAuth(byte[] decrypt)
	{
		super(decrypt);
		_desiredID = readC();
		_acceptAlternativeID = (readC() == 0 ? false : true); 
		_hostReserved = (readC() == 0 ? false : true);
		_netConfig = readS();
		_reserved = readS();
		_port = readH();
		_max_palyers = readD();
		int size = readD();
		_hexID = readB(size);
	}

	/**
	 * @return
	 */
	public byte[] getHexID()
	{
		return _hexID;
	}
	
	public boolean getHostReserved()
	{
		return _hostReserved;
	}
	
	public int getDesiredID()
	{
		return _desiredID;
	}
	
	public boolean acceptAlternateID()
	{
		return _acceptAlternativeID;
	}

	/**
	 * @return Returns the max_palyers.
	 */
	public int getMax_palyers()
	{
		return _max_palyers;
	}

	/**
	 * @return Returns the gameserver networks config.
	 */
	public String getNetConfig()
	{
		return _netConfig;
	}
	
	/**
	 * @return Returns the port.
	 */
	public int getPort()
	{
		return _port;
	}
}
