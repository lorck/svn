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
package com.l2jfree.status.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2JailZone;
import com.l2jfree.status.GameStatusCommand;

public final class Jail extends GameStatusCommand
{
	public Jail()
	{
		super("jails given player", "jail");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player [time]";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		StringTokenizer st = new StringTokenizer(params);
		
		String name = st.nextToken();
		L2PcInstance playerObj = L2World.getInstance().getPlayer(name);
		int delay = 0;
		if (st.hasMoreTokens())
			delay = Integer.parseInt(st.nextToken());
		
		if (playerObj != null)
		{
			playerObj.setInJail(true, delay);
			println("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
		}
		else
			jailOfflinePlayer(name, delay);
	}
	
	private void jailOfflinePlayer(String name, int delay)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, L2JailZone.JAIL_LOCATION.getX());
			statement.setInt(2, L2JailZone.JAIL_LOCATION.getY());
			statement.setInt(3, L2JailZone.JAIL_LOCATION.getZ());
			statement.setInt(4, 1);
			statement.setLong(5, delay * 60000L);
			statement.setString(6, name);
			
			int count = statement.executeUpdate();
			statement.close();
			
			if (count == 0)
				println("Character not found!");
			else
				println("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
