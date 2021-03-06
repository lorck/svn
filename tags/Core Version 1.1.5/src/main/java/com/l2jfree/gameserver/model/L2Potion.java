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

import java.util.concurrent.Future;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;

/**
 * 
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class L2Potion extends L2Object
{
	@SuppressWarnings("unused")
	protected static final Log _log = LogFactory.getLog(L2Potion.class.getName());

	private Future<?> _potionHpRegTask;
	private Future<?> _potionMpRegTask;
	protected int _milliseconds;
	protected double _effect;
	protected int _duration;
	private int _potion;
	protected Object _mpLock = new Object();
	protected Object _hpLock = new Object();
	
	
	class PotionHpHealing implements Runnable
	{
		L2Character _instance;
				
		public PotionHpHealing(L2Character instance)
		{
			_instance = instance;
		}
		
		public void run()
		{
			try
			{
				synchronized(_hpLock)
				{
					double nowHp = _instance.getStatus().getCurrentHp();
					if(_duration == 0)
					{
						stopPotionHpRegeneration();
					}
					if (_duration != 0)
					{
						nowHp     += _effect;
						_instance.getStatus().setCurrentHp(nowHp);
						_duration = _duration - (_milliseconds / 1000);
						setCurrentHpPotion2();
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("Error in hp potion task:"+e);
			}
		}
	}
	
	
	public L2Potion(int objectId)
	{
		super(objectId);
	}

	public void stopPotionHpRegeneration()
	{
		if (_potionHpRegTask != null)
		{
			_potionHpRegTask.cancel(false);
		}
		_potionHpRegTask = null;
		if (_log.isDebugEnabled()) _log.info("Potion HP regen stop");
	}

	public void setCurrentHpPotion2()
	{
		if (_duration == 0)
		{
			stopPotionHpRegeneration();
		}
	}
	public void setCurrentHpPotion1(L2Character activeChar, int item)
	{
		_potion = item;

		switch (_potion)
		{
			case (1540): 
				double nowHp = activeChar.getStatus().getCurrentHp();
				nowHp+=435;
				if (nowHp>= activeChar.getMaxHp())
				{
					nowHp = activeChar.getMaxHp();
				}
				activeChar.getStatus().setCurrentHp(nowHp);
				break;
			case (728):	
				double nowMp = activeChar.getMaxMp();
				nowMp+=435;
				if (nowMp>= activeChar.getMaxMp())
				{
					nowMp = activeChar.getMaxMp();
				}
				activeChar.getStatus().setCurrentMp(nowMp);
				break;
			case (726):
				_milliseconds = 500;
				_duration = 15;
				_effect = 1.5;
				startPotionMpRegeneration(activeChar);
				break;	
		}
	}
	
	class PotionMpHealing implements Runnable
	{
		L2Character _instance;
		
		public PotionMpHealing(L2Character instance)
		{
			_instance = instance;
		}
		
		public void run()
		{
			try
			{
				synchronized(_mpLock)
				{
					double nowMp = _instance.getStatus().getCurrentMp();
					if(_duration == 0)
					{
						stopPotionMpRegeneration();
					}
					if (_duration != 0)
					{
						nowMp+=_effect;
						_instance.getStatus().setCurrentMp(nowMp);
						_duration=(_duration-(_milliseconds/1000));
						setCurrentMpPotion2();
						
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("error in mp potion task:"+e);
			}
		}
	}
	
	private void startPotionMpRegeneration(L2Character activeChar)
	{
		_potionMpRegTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(
				new PotionMpHealing(activeChar), 1000, _milliseconds);
		if (_log.isDebugEnabled()) _log.info("Potion MP regen Started");
	}

	public void stopPotionMpRegeneration()
	{
		if (_potionMpRegTask != null)
		{
			_potionMpRegTask.cancel(false);
		}

		_potionMpRegTask = null;
		if (_log.isDebugEnabled()) _log.info("Potion MP regen stop");
	}

	public void setCurrentMpPotion2()
	{
		if (_duration == 0)
		{
			stopPotionMpRegeneration();
		}
	}
	
	/**
	 * @param activeChar  
	 * @param item  
	 */
	public void setCurrentMpPotion1(L2Character activeChar, int item)
	{
		_potion = item;

		switch (_potion)
		{
			
		}
	}

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.model.L2Object#isAttackable()
     */
    @Override
    public boolean isAutoAttackable(@SuppressWarnings("unused") L2Character attacker)
    {
        return false;
    }
}
