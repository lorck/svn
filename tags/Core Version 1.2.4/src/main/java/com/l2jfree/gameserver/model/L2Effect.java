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
package com.l2jfree.gameserver.model;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AbnormalStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.l2jfree.gameserver.network.serverpackets.PartySpelled;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.effects.EffectTemplate;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.gameserver.skills.funcs.Lambda;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 */
public abstract class L2Effect
{
	static final Log	_log	= LogFactory.getLog(L2Effect.class.getName());
	
	public static enum EffectState
	{
		CREATED, ACTING, FINISHING
	}
	
	public static enum EffectType
	{
		BUFF, DEBUFF, DMG_OVER_TIME, HEAL_OVER_TIME, COMBAT_POINT_HEAL_OVER_TIME, MANA_DMG_OVER_TIME, MP_CONSUME_PER_LEVEL,
		MANA_HEAL_OVER_TIME, RELAXING, STUN, ROOT, SLEEP, IMMOBILEUNTILATTACKED, HATE, FAKE_DEATH, CONFUSION, CONFUSE_MOB_ONLY,
		MUTE, FEAR, SILENT_MOVE, SEED, PARALYZE, STUN_SELF, BLUFF, BETRAY, NOBLESSE_BLESSING, PHOENIX_BLESSING, PETRIFY,
		CANCEL_TARGET, SILENCE_MAGIC_PHYSICAL, ERASE, LUCKNOBLESSE, PHYSICAL_MUTE, PHYSICAL_ATTACK_MUTE, TARGET_ME, REMOVE_TARGET,
		CHARM_OF_LUCK, INVINCIBLE, BAND_OF_DARKNESS, DARK_SEED, TRANSFORM, DISARM, CHARMOFCOURAGE,
		PREVENT_BUFF, CONDITION_HIT, TRANSFORMATION, SIGNET_EFFECT, SIGNET_GROUND, WARP, SPOIL, PROTECTION_BLESSING
	}
	
	private static final Func[]		_emptyFunctionSet	= new Func[0];
	
	// member _effector is the instance of L2Character that cast/used the spell/skill that is
	// causing this effect. Do not confuse with the instance of L2Character that
	// is being affected by this effect.
	private final L2Character		_effector;
	
	// member _effected is the instance of L2Character that was affected
	// by this effect. Do not confuse with the instance of L2Character that
	// casted/used this effect.
	private final L2Character		_effected;
	
	// the skill that was used.
	private final L2Skill			_skill;
	
	// or the items that was used.
	// private final L2Item _item;
	
	// the value of an update
	private final Lambda			_lambda;
	
	// the current state
	private EffectState				_state;
	
	// period, seconds
	private int						_period;
	private int						_periodStartTicks;
	private int						_periodfirsttime;

	// Effect template
	private EffectTemplate			_template;
	
	// function templates
	private final FuncTemplate[]	_funcTemplates;
	
	// initial count
	private int						_totalCount;
	
	// counter
	private int						_count;
	
	// abnormal effect mask
	private int						_abnormalEffect;
	
	// show icon
	private boolean					_icon;
	
	public boolean					preventExitUpdate;
	
	public final class EffectTask implements Runnable
	{
		protected final int	_delay;
		protected final int	_rate;
		
		EffectTask(int delay, int rate)
		{
			_delay = delay;
			_rate = rate;
		}
		
		public void run()
		{
			try
			{
				if (_periodfirsttime == 0)
					_periodStartTicks = GameTimeController.getGameTicks();
				else
					_periodfirsttime = 0;
				L2Effect.this.scheduleEffect();
			}
			catch (Throwable e)
			{
				_log.fatal("", e);
			}
		}
	}
	
	private ScheduledFuture<?> _currentFuture;
	private EffectTask         _currentTask;
	
	/** The Identifier of the stack group */
	private final String	_stackType;
	
	/** The position of the effect in the stack group */
	private final float		_stackOrder;
	
	private boolean			_inUse	= false;
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env.skill;
		// _item = env._item == null ? null : env._item.getItem();
		_template = template;
		_effected = env.target;
		_effector = env.player;
		_lambda = template.lambda;
		_funcTemplates = template.funcTemplates;
		_count = template.counter;
		_totalCount = _count;

		// TODO DrHouse: This should be reworked, we need to be able to change effect time out of Effect Constructor
		// maybe using a child class
		// Support for retail herbs duration when _effected has a Summon 
		int temp = template.period, id = _skill.getId();
		if ((id > 2277 && id < 2286) || (id >= 2512 && id <= 2514))
		{
			if (_effected instanceof L2SummonInstance 
				|| (_effected instanceof L2PcInstance && ((L2PcInstance)_effected).getPet() instanceof L2SummonInstance))
			{
				temp /= 2;
			}
		}
		_period = temp; 

		_abnormalEffect = template.abnormalEffect;
		_stackType = template.stackType;
		_stackOrder = template.stackOrder;
		_periodStartTicks = GameTimeController.getGameTicks();
		_periodfirsttime = 0;
		_icon = template.icon;
		scheduleEffect();
	}
	
	/**
	 * Special constructor to "steal" buffs. Must be implemented on
	 * every child class that can be stolen.
	 *
	 * @param env
	 * @param effect
	 */
	protected L2Effect(Env env, L2Effect effect)
	{
		_template = effect._template;
		_state = EffectState.CREATED;
		_skill = env.skill;
		_effected = env.target;
		_effector = env.player;
		_lambda = _template.lambda;
		_funcTemplates = _template.funcTemplates;
		_count = effect.getCount();
		_totalCount = _template.counter;
		_period = _template.period - effect.getTime();
		_abnormalEffect = _template.abnormalEffect;
		_stackType = _template.stackType;
		_stackOrder = _template.stackOrder;
		_periodStartTicks = effect.getPeriodStartTicks();
		_periodfirsttime = effect.getPeriodfirsttime();
		_icon = _template.icon;
		scheduleEffect();
	}

	public int getCount()
	{
		return _count;
	}
	
	public int getTotalCount()
	{
		return _totalCount;
	}
	
	public void setCount(int newcount)
	{
		_count = newcount;
	}
	
	public void setFirstTime(int newfirsttime)
	{
		if (_currentFuture != null)
		{
			_periodStartTicks = GameTimeController.getGameTicks() - newfirsttime * GameTimeController.TICKS_PER_SECOND;
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			_periodfirsttime = newfirsttime;
			int duration = _period - _periodfirsttime;
			// _log.warn("Period: "+_period+"-"+_periodfirsttime+"="+duration);
			_currentTask = new EffectTask(duration * 1000, -1);
			_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask, duration * 1000);
		}
	}

	public boolean getShowIcon()
	{
		return _icon;
	}

	public int getPeriod()
	{
		return _period;
	}
	
	public int getTime()
	{
		return (GameTimeController.getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Returns the elapsed time of the task.
	 * 
	 * @return Time in seconds.
	 */
	public int getElapsedTaskTime()
	{
		return (_totalCount - _count) * _period + getTime() + 1;
	}
	
	public int getTotalTaskTime()
	{
		return _totalCount * _period;
	}
	
	public int getRemainingTaskTime()
	{
		return getTotalTaskTime() - getElapsedTaskTime();
	}
	
	public int getPeriodfirsttime()
	{
		return _periodfirsttime;
	}
	
	public void setPeriodfirsttime(int periodfirsttime)
	{
		_periodfirsttime = periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}

	public void setPeriodStartTicks(int periodStartTicks)
	{
		_periodStartTicks = periodStartTicks;
	}
	
	public boolean getInUse()
	{
		return _inUse;
	}
	
	public void setInUse(boolean inUse)
	{
		_inUse = inUse;
		if (_inUse)
			onStart();
		else
			onExit();
	}
	
	public String getStackType()
	{
		return _stackType;
	}
	
	public float getStackOrder()
	{
		return _stackOrder;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}

	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _skill._effectTemplatesSelf != null;
	}
	
	public boolean isHerbEffect()
	{
		if (getSkill().getName().contains("Herb"))
			return true;
		
		return false;
	}
	
	public final double calc()
	{
		Env env = new Env();
		env.player = _effector;
		env.target = _effected;
		env.skill = _skill;
		
		return _lambda.calc(env);
	}
	
	private synchronized void startEffectTask(int duration)
	{
		stopEffectTask();
		_currentTask = new EffectTask(duration, -1);
		_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask, duration);
		
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
	}

	private synchronized void startEffectTaskAtFixedRate(int delay, int rate)
	{
		stopEffectTask();
		_currentTask = new EffectTask(delay, rate);
		_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(_currentTask, delay, rate);
		
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
	}
	
	public final void exit()
	{
		exit(false);
	}
	
	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character </li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icons </li>
	 * <BR>
	 * <BR>
	 */
	public final void exit(boolean preventUpdate)
	{
		preventExitUpdate = preventUpdate;
		_state = EffectState.FINISHING;
		scheduleEffect();
	}
	
	/**
	 * Stop the task of the L2Effect, remove it and update client magic icons.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the task </li>
	 * <li>Stop and remove L2Effect from L2Character and update client magic icons </li>
	 * <BR>
	 * <BR>
	 */
	public void stopEffectTask()
	{
		if (_currentFuture != null)
		{
			// Cancel the task
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			
			_effected.removeEffect(this);
		}
	}
	
	/** returns effect type */
	public abstract EffectType getEffectType();
	
	/** Notify started */
	public void onStart()
	{
		if (_abnormalEffect != 0)
			getEffected().startAbnormalEffect(_abnormalEffect);
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR>
	 * <BR>
	 */
	public void onExit()
	{
		if (_abnormalEffect != 0)
			getEffected().stopAbnormalEffect(_abnormalEffect);
	}
	
	/** Return true for continueation of this effect */
	public abstract boolean onActionTime();
	
	public final void rescheduleEffect()
	{
		if (_state != EffectState.ACTING)
		{
			scheduleEffect();
		}
		else
		{
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
				return;
			}
		}
	}
	
	public final void scheduleEffect()
	{
		if (_state == EffectState.CREATED)
		{
			_state = EffectState.ACTING;
			
			if (_skill.isPvpSkill())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				smsg.addSkillName(_skill);
				getEffected().sendPacket(smsg);
			}
			
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
				return;
			}
			// effects not having count or period should start
			setInUse(true);
		}
		
		if (_state == EffectState.ACTING)
		{
			if (_count-- > 0)
			{
				if (getInUse()) // effect has to be in use
				{
					if (onActionTime())
						return; // false causes effect to finish right away
				}
				else if (_count > 0) // do not finish it yet, in case reactivated
				{ return; }
			}
			_state = EffectState.FINISHING;
		}
		
		if (_state == EffectState.FINISHING)
		{
			// Cancel the effect in the the abnormal effect map of the L2Character
			if ((getInUse() || !(_count > 1 || _period > 0)) && _skill.getTransformId() < 1)
				setInUse(false);
			
			// If the time left is equal to zero, send the message
			if (_count == 0)
			{
				SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
				smsg3.addSkillName(_skill);
				getEffected().sendPacket(smsg3);
			}
			// Stop the task of the L2Effect, remove it and update client magic icons
			stopEffectTask();
			
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (_funcTemplates == null)
			return _emptyFunctionSet;
		
		List<Func> funcs = new FastList<Func>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = getEffector();
			env.target = getEffected();
			env.skill = getSkill();
			Func f = t.getFunc(env, this); // effect is owner
			if (f != null)
				funcs.add(f);
		}
		
		if (funcs.size() == 0)
			return _emptyFunctionSet;
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public final void addIcon(AbnormalStatusUpdate mi)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		
		if (task == null || future == null)
			return;
		
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED)
			return;
		
		L2Skill sk = getSkill();
		int time = -1;
		if (task._rate > 0)
			time = getRemainingTaskTime() * 1000;
		// Why only potions? HOT skills should have this too.. maybe not retail, but more informative...
		// if (sk.isPotion()) time = getRemainingTaskTime() * 1000;
		else
			time = (int) future.getDelay(TimeUnit.MILLISECONDS);
		
		mi.addEffect(sk.getId(), sk.getLevel(), time);
	}
	
	public final void addPartySpelledIcon(PartySpelled ps)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		
		if (task == null || future == null)
			return;
		
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED)
			return;
		
		L2Skill sk = getSkill();
		ps.addPartySpelledEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
	}
	
	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os)
	{
		EffectTask task = _currentTask;
		ScheduledFuture<?> future = _currentFuture;
		
		if (task == null || future == null)
			return;
		
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED)
			return;
		
		L2Skill sk = getSkill();
		os.addEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
	}
	
	public int getLevel()
	{
		return getSkill().getLevel();
	}
	
	public EffectTemplate getEffectTemplate()
	{
		return _template;
	}
	
	public void destroy()
	{
		_effected.removeEffect(this, false);
		
		_state = null;
		_currentTask = null;
		
		if (_currentFuture != null)
		{
			_currentFuture.cancel(true);
			_currentFuture = null;
		}
	}
}
