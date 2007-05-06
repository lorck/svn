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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class ExDuelUpdateUserInfo extends L2GameServerPacket 
{
	private static final String _S__67_PARTYSMALLWINDOWUPDATE = "[S] 52 ExDuelUpdateUserInfo";
	private L2PcInstance _member;
	
	public ExDuelUpdateUserInfo(L2PcInstance member)
	{
		_member = member;
	}
	
	protected void writeImpl()
	{
		writeC(0xfe);
        writeH(75);
		writeS(_member.getName());
        writeD(_member.getObjectId());
        writeD(_member.getClassId().getId());
        writeD(_member.getLevel());
		
		writeD((int) _member.getStatus().getCurrentHp());
		writeD(_member.getStat().getMaxHp());
		writeD((int) _member.getStatus().getCurrentMp());
		writeD(_member.getStat().getMaxMp());
        
        writeD((int) _member.getStatus().getCurrentCp());
        writeD(_member.getStat().getMaxCp());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__67_PARTYSMALLWINDOWUPDATE;
	}
}
