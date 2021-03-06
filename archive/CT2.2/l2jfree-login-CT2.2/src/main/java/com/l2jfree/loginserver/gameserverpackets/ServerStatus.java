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
package com.l2jfree.loginserver.gameserverpackets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.loginserver.beans.GameServerInfo;
import com.l2jfree.loginserver.clientpackets.ClientBasePacket;
import com.l2jfree.loginserver.manager.GameServerManager;

/**
 * @author -Wooden-
 * @reworked by savormix
 */
public class ServerStatus extends ClientBasePacket
{
	private static final Log		_log			= LogFactory.getLog(ServerStatus.class.getName());

	public static final int SERVER_LIST_PVP			= 0x01;
    public static final int SERVER_LIST_MAX_PLAYERS = 0x02;
    public static final int SERVER_LIST_STATUS		= 0x03;
    public static final int SERVER_LIST_UNK			= 0x04;
    public static final int SERVER_LIST_CLOCK		= 0x05;
    public static final int SERVER_LIST_HIDE_NAME	= 0x06;
    public static final int TEST_SERVER				= 0x07;
    public static final int SERVER_LIST_BRACKETS	= 0x08;
    public static final int SERVER_AGE_LIMITATION	= 0x09;

    public static final int STATUS_AUTO		= 0x00;
    public static final int STATUS_GM_ONLY	= 0x01;
    public static final int STATUS_DOWN		= 0x02;

	private static final int OFF = 0x00;

	/**
	 * @param decrypt
	 */
	public ServerStatus(byte[] decrypt, int serverID)
	{
		super(decrypt);

		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(serverID);

		if (gsi != null)
		{
			int size = readD();
			for (int i = 0; i < size; i++)
			{
				int type = readD();
				int value = readD();
				if (_log.isDebugEnabled()) _log.debug("ServerStatus: "+type+" = "+value);
				switch(type)
				{
				case SERVER_LIST_PVP:
					gsi.setPvp(value > OFF); break;
				case SERVER_LIST_MAX_PLAYERS:
					gsi.setMaxPlayers(value); break;
				case SERVER_LIST_STATUS:
					gsi.setStatus(value); break;
				case SERVER_LIST_CLOCK:
					gsi.setShowingClock(value > OFF); break;
				case SERVER_LIST_HIDE_NAME:
					gsi.setHideName(value > OFF); break;
				case SERVER_LIST_UNK:
					gsi.setUnk1(value > OFF); break;
				case TEST_SERVER:
					gsi.setTestServer(value > OFF); break;
				case SERVER_LIST_BRACKETS:
					gsi.setShowingBrackets(value > OFF); break;
				case SERVER_AGE_LIMITATION:
					gsi.setAgeLimitation(value);
				}
			}
		}
	}

}
