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
package com.l2jfree.gameserver.gameobjects;

import com.l2jfree.gameserver.templates.L2NpcTemplate;

/**
 * @author NB4L1
 */
public abstract class L2Npc extends L2Character
{
	protected L2Npc(L2NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)super.getTemplate();
	}
}
