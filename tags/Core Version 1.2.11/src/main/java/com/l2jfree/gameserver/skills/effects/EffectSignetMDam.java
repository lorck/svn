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

/**
 * @author Forsaiken
 */

package com.l2jfree.gameserver.skills.effects;

import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.model.L2Attackable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfree.gameserver.network.serverpackets.NpcInfo;
import com.l2jfree.gameserver.network.serverpackets.PetInfo;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.tools.geometry.Point3D;

import javolution.util.FastList;

public final class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance	_actor;

	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}

	@Override
	public boolean onStart()
	{
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		else
			return false;

		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		L2World.getInstance().storeObject(effectPoint);

		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();

		if (getEffector() instanceof L2PcInstance && getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance) getEffector()).getCurrentSkillWorldPosition();

			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);

		_actor = effectPoint;
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		if (getCount() >= getTotalCount() - 2)
			return true; // do nothing first 2 times
		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		boolean ss = false;
		boolean bss = false;

		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			switch (weaponInst.getChargedSpiritshot())
			{
			case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				bss = true;
				break;
			case L2ItemInstance.CHARGED_SPIRITSHOT:
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				ss = true;
				break;
			}
		}

		// if (!bss && !ss)
		//    caster.rechargeAutoSoulShot(false, true, false);

		FastList<L2Character> targets = new FastList<L2Character>();

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster)
				continue;

			if (cha instanceof L2Attackable || cha instanceof L2PlayableInstance)
			{
				if (cha.isAlikeDead())
					continue;

				if (mpConsume > caster.getStatus().getCurrentMp())
				{
					caster.sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}

				caster.reduceCurrentMp(mpConsume);

				if (cha instanceof L2PcInstance || cha instanceof L2Summon)
					caster.updatePvPStatus(cha);

				targets.add(cha);
			}
		}

		if (targets.size() > 0)
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.getInstance().calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Formulas.getInstance().calcShldUse(caster, target);
				int mdam = (int) Formulas.getInstance().calcMagicDam(caster, target, getSkill(), shld, ss, bss, mcrit);

				if (target instanceof L2Summon)
				{
					if (caster.equals(((L2Summon) target).getOwner()))
						caster.sendPacket(new PetInfo((L2Summon) target));
					else
						caster.sendPacket(new NpcInfo((L2Summon) target, caster));
				}

				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
