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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.skills.Env;


class EffectManaHealOverTime extends L2Effect
{		
	public EffectManaHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return EffectType.MANA_HEAL_OVER_TIME;
	}

	public boolean onActionTime()
	{	
		if(getEffected().isDead())
			return false;
		
		double mp = getEffected().getStatus().getCurrentMp(); 
		double maxmp = getEffected().getStat().getMaxMp();
		mp += calc(); 
		if(mp > maxmp)
		{
			mp = maxmp;
		}
		getEffected().getStatus().setCurrentMp(mp); 
		StatusUpdate sump = new StatusUpdate(getEffected().getObjectId()); 
		sump.addAttribute(StatusUpdate.CUR_MP, (int)mp); 
		getEffected().sendPacket(sump);
		return true;
	}
}
