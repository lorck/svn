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
package net.sf.l2j.gameserver.serverpackets;



/**
 *
 * sample
 * <p>
 * 4b 
 * c1 b2 e0 4a 
 * 00 00 00 00
 * <p>
 * 
 * format
 * cdd
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ExDuelAskStart extends ServerBasePacket
{
	private static final String _S__4B_ExDuelAskStart_0X4B = "[S] 39 ExDuelAskStart 0x4b";
	//private final static Log _log = LogFactory.getLog(ExDuelAskStart.class.getName());

	private String _requestorName;
	private int _duelMode;

	/**
	 * @param int objectId of the target
	 * @param int 
	 */
	public ExDuelAskStart(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_duelMode = itemDistribution;
	}

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0xfe);
        writeH(75);
		writeS(_requestorName);
		writeD(_duelMode);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__4B_ExDuelAskStart_0X4B;
	}

}
