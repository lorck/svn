/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.cache.CrestCache;
import com.l2jfree.gameserver.network.serverpackets.AllyCrest;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAllyCrest extends L2GameClientPacket
{
	private static final String _C__88_REQUESTALLYCREST = "[C] 88 RequestAllyCrest";
	private final static Log _log = LogFactory.getLog(RequestAllyCrest.class.getName());

	private int _crestId;
	/**
	 * packet type id 0x88 format: cd
	 * 
	 * @param rawPacket
	 */
    @Override
    protected void readImpl()
    {
        _crestId = readD();
    }

    @Override
    protected void runImpl()
	{
		if (_log.isDebugEnabled()) _log.debug("allycrestid " + _crestId + " requested");
        
        byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);

		if (data != null)
		{
			AllyCrest ac = new AllyCrest(_crestId,data);
			sendPacket(ac);
		}
		else
		{
			if (_log.isDebugEnabled()) _log.debug("allycrest is missing:" + _crestId);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__88_REQUESTALLYCREST;
	}
}
