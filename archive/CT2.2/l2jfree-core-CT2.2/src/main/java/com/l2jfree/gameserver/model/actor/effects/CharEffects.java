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
package com.l2jfree.gameserver.model.actor.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.l2jfree.gameserver.skills.effects.EffectCharmOfCourage;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.util.ObjectPool;

/**
 * @author NB4L1
 */
public class CharEffects
{
	private static final Log _log = LogFactory.getLog(CharEffects.class);
	
	private final L2Character _owner;
	
	private L2Effect[] _toArray;
	private List<L2Effect> _effects;
	private Map<String, StackQueue> _stackedEffects;
	
	public CharEffects(L2Character owner)
	{
		_owner = owner;
	}
	
	private boolean isEmpty()
	{
		return _effects == null || _effects.isEmpty();
	}
	
	private static boolean isActiveBuff(L2Effect e)
	{
		return e != null && e.isInUse() && e.getShowIcon() && !e.getSkill().isDance() && !e.getSkill().isSong()
			&& !e.getSkill().isDebuff() && !e.getSkill().bestowed() && e.isBuff();
	}
	
	private static boolean isActiveDance(L2Effect e, boolean isDance, boolean isSong)
	{
		return e != null && e.isInUse() && (isDance && e.getSkill().isDance() || isSong && e.getSkill().isSong());
	}
	
	public synchronized L2Effect[] getAllEffects()
	{
		if (isEmpty())
			return L2Effect.EMPTY_ARRAY;
		
		if (_toArray == null)
			_toArray = _effects.toArray(new L2Effect[_effects.size()]);
		
		return _toArray;
	}
	
	public synchronized void addEffect(L2Effect newEffect)
	{
		if (_effects == null)
			_effects = new ArrayList<L2Effect>(4);
		
		if (_stackedEffects == null)
			_stackedEffects = new FastMap<String, StackQueue>(4);
		
		final int newOrder = getOrder(newEffect);
		
		int index;
		for (index = 0; index < _effects.size(); index++)
		{
			final L2Effect e = _effects.get(index);
			
			if (getOrder(e) > newOrder)
				break;
		}
		
		_effects.add(index, newEffect);
		_toArray = null;
		
		StackQueue stackQueue = _stackedEffects.get(newEffect.getStackType());
		
		if (stackQueue == null)
			stackQueue = StackQueue.newInstance(this, newEffect.getStackType());
		
		stackQueue.add(newEffect);
		
		if (isActiveDance(newEffect, true, true))
		{
			if (getDanceCount(true, true) > Config.ALT_DANCES_SONGS_MAX_AMOUNT)
			{
				for (int i = 0; i < _effects.size(); i++)
				{
					final L2Effect e = _effects.get(i);
					
					if (isActiveDance(e, true, true))
					{
						_stackedEffects.get(e.getStackType()).stopAllEffects();
						return;
					}
				}
			}
		}
		else if (isActiveBuff(newEffect))
		{
			if (getBuffCount() > _owner.getMaxBuffCount())
			{
				for (int i = 0; i < _effects.size(); i++)
				{
					final L2Effect e = _effects.get(i);
					
					if (isActiveBuff(e))
					{
						_stackedEffects.get(e.getStackType()).stopAllEffects();
						return;
					}
				}
			}
		}
	}
	
	private static int getOrder(L2Effect e)
	{
		if (e.getSkill().isToggle())
			return 3;
		
		if (e.getSkill().isOffensive() || e.getSkill().isDebuff())
			return 2;
		
		return 1;
	}
	
	public synchronized boolean removeEffect(L2Effect effect)
	{
		final int index = _effects.indexOf(effect);
		if (index < 0)
			return false;
		
		_stackedEffects.get(effect.getStackType()).remove(effect);
		_effects.remove(index);
		_toArray = null;
		
		return true;
	}
	
	private static final class StackQueue
	{
		private static final ObjectPool<StackQueue> POOL = new ObjectPool<StackQueue>() {
			@Override
			protected StackQueue create()
			{
				return new StackQueue();
			}
		};
		
		private static StackQueue newInstance(CharEffects effects, String stackType)
		{
			StackQueue stackQueue = POOL.get();
			
			stackQueue._queue.clear();
			stackQueue._effects = effects;
			stackQueue._stackType = stackType;
			stackQueue._effects._stackedEffects.put(stackQueue._stackType, stackQueue);
			
			return stackQueue;
		}
		
		private static void recycle(StackQueue stackQueue)
		{
			stackQueue._effects._stackedEffects.remove(stackQueue._stackType);
			stackQueue._stackType = null;
			stackQueue._effects = null;
			stackQueue._queue.clear();
			
			POOL.store(stackQueue);
		}
		
		private final ArrayList<L2Effect> _queue = new ArrayList<L2Effect>(4);
		private CharEffects _effects;
		private String _stackType;
		
		private void add(final L2Effect effect)
		{
			int index;
			for (index = 0; index < _queue.size(); index++)
			{
				final L2Effect e = _queue.get(index);
				
				if (effect.getStackOrder() >= e.getStackOrder())
					break;
			}
			
			if (index == 0 && !_queue.isEmpty())
				_queue.get(0).setInUse(false);
			
			_queue.add(index, effect);
			
			if (index == 0)
				_queue.get(0).setInUse(true);
			
			// TODO: Config.EFFECT_CANCELING
		}
		
		private void remove(final L2Effect effect)
		{
			final int index = _queue.indexOf(effect);
			
			if (index == 0)
				_queue.get(0).setInUse(false);
			
			_queue.remove(index);
			
			if (index == 0 && !_queue.isEmpty())
				_queue.get(0).setInUse(true);
			
			if (_queue.isEmpty())
				StackQueue.recycle(this);
		}
		
		private void stopAllEffects()
		{
			for (int index = _queue.size() - 1; index >= 0; index--)
				_queue.get(index).exit();
		}
	}
	
	/**
	 * For debugging purpose...
	 */
	public void printStackTrace(String stackType, L2Effect effect)
	{
		TextBuilder tb = TextBuilder.newInstance();
		
		tb.append(_owner);
		
		if (stackType != null)
			tb.append(" -> ").append(stackType);
		
		if (effect != null)
			tb.append(" -> ").append(effect.getSkill().toString());
		
		_log.warn(tb, new IllegalStateException());
		
		TextBuilder.recycle(tb);
	}
	
	public synchronized L2Effect getFirstEffect(L2Skill skill)
	{
		return getFirstEffect(skill.getId());
	}
	
	public synchronized boolean hasEffect(L2Skill skill)
	{
		return hasEffect(skill.getId());
	}
	
	public synchronized void stopEffects(L2Skill skill)
	{
		stopEffects(skill.getId());
	}
	
	public synchronized L2Effect getFirstEffect(int id)
	{
		return getFirstEffect(id, true);
	}
	
	public synchronized boolean hasEffect(int id)
	{
		return getFirstEffect(id, false) != null;
	}
	
	public synchronized void stopEffects(int id)
	{
		for (L2Effect e; (e = getFirstEffect(id, false)) != null;)
			e.exit();
	}
	
	private L2Effect getFirstEffect(int id, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		L2Effect notUsedEffect = null;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (e.getSkill().getId() == id)
			{
				if (e.isInUse() || !searchForInUse)
					return e;
				else if (notUsedEffect == null)
					notUsedEffect = e;
			}
		}
		
		return notUsedEffect;
	}
	
	public synchronized L2Effect getFirstEffect(L2EffectType tp)
	{
		return getFirstEffect(tp, true);
	}
	
	public synchronized boolean hasEffect(L2EffectType tp)
	{
		return getFirstEffect(tp, false) != null;
	}
	
	public synchronized void stopEffects(L2EffectType tp)
	{
		for (L2Effect e; (e = getFirstEffect(tp, false)) != null;)
			e.exit();
	}
	
	private L2Effect getFirstEffect(L2EffectType tp, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		L2Effect notUsedEffect = null;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (e.getEffectType() == tp)
			{
				if (e.isInUse() || !searchForInUse)
					return e;
				else if (notUsedEffect == null)
					notUsedEffect = e;
			}
		}
		
		return notUsedEffect;
	}
	
	public synchronized int getBuffCount()
	{
		if (isEmpty())
			return 0;
		
		int buffCount = 0;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (isActiveBuff(e))
				buffCount++;
		}
		
		return buffCount;
	}
	
	public synchronized int getDanceCount(boolean dances, boolean songs)
	{
		if (isEmpty())
			return 0;
		
		int danceCount = 0;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (isActiveDance(e, dances, songs))
				danceCount++;
		}
		
		return danceCount;
	}
	
	public synchronized void addPacket(EffectInfoPacketList list)
	{
		if (isEmpty())
			return;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			e.addPacket(list);
		}
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public void stopAllEffects()
	{
		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().getId() != 5660)
			{
				e.exit();
			}
		}
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		for (L2Effect e : getAllEffects())
		{
			if (e != null)
			{
				if (e instanceof EffectCharmOfCourage)
					continue;
				e.exit();
			}
		}
	}
}
