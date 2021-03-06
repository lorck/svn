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
package com.l2jfree.gameserver.network.client.packets.sendable;

import com.l2jfree.gameserver.network.client.packets.L2ServerPacket;

/**
 * This packet needs re-generation after completing the definition.
 * 
 * @author savormix (generated)
 */
public abstract class ExUISettingPacket extends L2ServerPacket
{
	/**
	 * A nicer name for {@link ExUISettingPacket}.
	 * 
	 * @author savormix (generated)
	 * @see ExUISettingPacket
	 */
	public static final class UserInterfaceSetup extends ExUISettingPacket
	{
		/**
		 * Constructs this packet.
		 * 
		 * @see ExUISettingPacket#ExUISettingPacket()
		 */
		public UserInterfaceSetup()
		{
		}
	}
	
	private static final int[] EXT_OPCODES = { 0x70, 0x00 };
	
	/** Constructs this packet. */
	public ExUISettingPacket()
	{
	}
	
	@Override
	protected int getOpcode()
	{
		return 0xfe;
	}
	
	@Override
	protected int[] getAdditionalOpcodes()
	{
		return EXT_OPCODES;
	}
}
