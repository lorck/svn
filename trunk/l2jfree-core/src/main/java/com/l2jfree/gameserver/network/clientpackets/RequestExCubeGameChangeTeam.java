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


/**
 * @author mrTJO
 * Format: chdd
 * d: team
 */
public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
	private static final String _C__D0_5A_00_REQUESTEXCUBEGAMECHANGETEAM = "[C] D0:5A:00 RequestExCubeGameChangeTeam";
	
	private int _team;
	
	@Override
	protected void readImpl()
	{
		_team = readD();
	}
	
	@Override
	protected void runImpl()
	{
		//L2PcInstance activeChar = getClient().getActiveChar();
		
		switch (_team)
		{
			case 0:
			case 1:
				// Change Player Team
				break;
			case -1:
				// Remove Player (me)
				break;
			default:
				_log.warn("Wrong Team ID: "+_team);
				break;
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_5A_00_REQUESTEXCUBEGAMECHANGETEAM;
	}
}
