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
package com.l2jfree.gameserver.model.zone;

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class L2SiegeZone extends EntityZone
{
	@Override
	protected void register()
	{
		if (_castleId > 0)
		{
			_entity = CastleManager.getInstance().getCastleById(_castleId);
			if (_entity != null)
			{
				// Init siege task
				((Castle)_entity).getSiege();
				_entity.registerSiegeZone(this);
			}
			else
				_log.warn("Invalid castleId: "+_castleId);
		}
		else if (_fortId > 0)
		{
			_entity = FortManager.getInstance().getFortById(_fortId);
			if (_entity != null)
			{
				// Init siege task
				((Fort)_entity).getSiege();
				_entity.registerSiegeZone(this);
			}
			else
				_log.warn("Invalid fortId: "+_castleId);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if ((_entity instanceof Castle && ((Castle)_entity).getSiege().getIsInProgress())
			|| (_entity instanceof Fort && ((Fort)_entity).getSiege().getIsInProgress()))
		{
			character.setInsideZone(FLAG_PVP, true);
			character.setInsideZone(FLAG_SIEGE, true);
			character.setInsideZone(FLAG_NOSUMMON, true);

			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}

		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if ((_entity instanceof Castle && ((Castle)_entity).getSiege().getIsInProgress())
			|| (_entity instanceof Fort && ((Fort)_entity).getSiege().getIsInProgress()))
		{
			character.setInsideZone(FLAG_PVP, false);
			character.setInsideZone(FLAG_SIEGE, false);
			character.setInsideZone(FLAG_NOSUMMON, false);

			if (character instanceof L2PcInstance)
			{
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));

				// Set pvp flag
				if (((L2PcInstance)character).getPvpFlag() == 0)
					character.startPvPFlag();
			}
		}
		if (character instanceof L2SiegeSummonInstance)
		{
			((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
		}
		if (character instanceof L2PcInstance)
		{
			L2PcInstance activeChar = (L2PcInstance) character;
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(9819);
			if (item != null)
			{
				Fort fort = FortManager.getInstance().getFort(activeChar);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(activeChar);
				}
				else
				{
					int slot = item.getItem().getBodyPart();
					activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
					activeChar.destroyItem("CombatFlag", item, null, true);
				}
			}
		}

		super.onExit(character);
	}

	public void updateSiegeStatus()
	{
		if ((_entity instanceof Castle && ((Castle)_entity).getSiege().getIsInProgress())
			|| (_entity instanceof Fort && ((Fort)_entity).getSiege().getIsInProgress()))
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch(Exception e)
				{
				}
			}
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					character.setInsideZone(FLAG_PVP, false);
					character.setInsideZone(FLAG_SIEGE, false);
					character.setInsideZone(FLAG_NOSUMMON, false);

					if (character instanceof L2PcInstance)
						character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
					if (character instanceof L2SiegeSummonInstance)
					{
						((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
					}
				}
				catch(Exception e)
				{
				}
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		if (_entity instanceof Fort)
		{
			Fort fort = (Fort) _entity;
			if (fort.getSiege().getIsInProgress())
			{
				// debuff participants only if they die inside siege zone
				if (character instanceof L2PcInstance && ((L2PcInstance) character).getClan() != null)
				{
					int lvl = 1;
					for (L2Effect effect: character.getAllEffects())
					{
						if (effect != null && effect.getSkill().getId() == 5660)
						{
							lvl = lvl + effect.getLevel();
							if (lvl > 5)
								lvl = 5;
							break;
						}
					}
					L2Clan clan;
					L2Skill skill;
					if (fort.getOwnerClan() == ((L2PcInstance)character).getClan())
					{
						skill = SkillTable.getInstance().getInfo(5660, lvl);
						if (skill != null)
							skill.getEffects(character, character);
					}
					else
					{
						for (L2SiegeClan siegeclan : fort.getSiege().getAttackerClans())
						{
							if (siegeclan == null)
								continue;
							clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
							if (((L2PcInstance) character).getClan() == clan)
							{
								skill = SkillTable.getInstance().getInfo(5660, lvl);
								if (skill != null)
									skill.getEffects(character, character);
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
