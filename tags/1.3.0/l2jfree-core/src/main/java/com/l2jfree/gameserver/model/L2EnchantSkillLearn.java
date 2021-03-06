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

import javolution.util.FastTable;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;


/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2EnchantSkillLearn
{
	private final int _id;
	private final int _baseLvl;

	@SuppressWarnings("unchecked")
	private List<EnchantSkillDetail>[] _enchantDetails = new FastTable[0];

	public L2EnchantSkillLearn(int id, int baseLvl)
	{
		_id = id;
		_baseLvl = baseLvl;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return _baseLvl;
	}

	@SuppressWarnings("unchecked")
	public void addEnchantDetail(EnchantSkillDetail esd)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantType(esd.getLevel());
		
		if (enchantType < 0)
		{
			throw new IllegalArgumentException("Skill enchantments should have level higher then 100");
		}

		if (enchantType >= _enchantDetails.length)
		{
			List<EnchantSkillDetail>[] newArray = new FastTable[enchantType+1];
			System.arraycopy(_enchantDetails, 0, newArray, 0, _enchantDetails.length);
			_enchantDetails = newArray;
			_enchantDetails[enchantType] = new FastTable<EnchantSkillDetail>();
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(esd.getLevel());
		_enchantDetails[enchantType].add(index, esd);
	}

	public List<EnchantSkillDetail>[] getEnchantRoutes()
	{
		return _enchantDetails;
	}

	public EnchantSkillDetail getEnchantSkillDetail(int level)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantType(level);
		if (enchantType < 0 || enchantType >= _enchantDetails.length)
		{
			return null;
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(level);
		if (index < 0 || index >= _enchantDetails[enchantType].size())
		{
			return null;
		}
		return _enchantDetails[enchantType].get(index);
	}

	public static int getEnchantIndex(int level)
	{
		return (level % 100) - 1;
	}

	public static int getEnchantType(int level)
	{
		return ((level - 1) / 100) - 1;
	}

	public static class EnchantSkillDetail
	{
		private final int _level;
		private final int _spCost;
		private final int _minSkillLevel;
		private final int _exp;
		private final byte _rate76;
		private final byte _rate77;
		private final byte _rate78;
		private final byte _rate79;
		private final byte _rate80;
		private final byte _rate81;
		private final byte _rate82;
		private final byte _rate83;
		private final byte _rate84;
		private final byte _rate85;
		
		public EnchantSkillDetail(int lvl, int minSkillLvl, int cost, int exp,
				byte rate76, byte rate77, byte rate78,
				byte rate79, byte rate80, byte rate81,
				byte rate82, byte rate83, byte rate84,
				byte rate85)
		{
			_level = lvl;
			_minSkillLevel = minSkillLvl;
			_spCost = cost;
			_exp = exp;
			_rate76 = rate76;
			_rate77 = rate77;
			_rate78 = rate78;
			_rate79 = rate79;
			_rate80 = rate80;
			_rate81 = rate81;
			_rate82 = rate82;
			_rate83 = rate83;
			_rate84 = rate84;
			_rate85 = rate85;
		}
		
		/**
		 * @return Returns the level.
		 */
		public int getLevel()
		{
			return _level;
		}
		
		/**
		 * @return Returns the minSkillLevel.
		 */
		public int getMinSkillLevel()
		{
			return _minSkillLevel;
		}

		/**
		 * @return Returns the spCost.
		 */
		public int getSpCost()
		{
			return _spCost;
		}
		
		public int getExp()
		{
			return _exp;
		}

		public byte getRate(L2PcInstance ply)
		{
			byte result;
			switch (ply.getLevel())
			{
				case 76:
					result = _rate76;
					break;
				case 77:
					result = _rate77;
					break;
				case 78:
					result = _rate78;
					break;
				case 79:
					result = _rate79;
					break;
				case 80:
					result = _rate80;
					break;
				case 81:
					result = _rate81;
					break;
				case 82:
					result = _rate82;
					break;
				case 83:
					result = _rate83;
					break;
				case 84:
					result = _rate84;
					break;
				case 85:
				default:
					result = _rate85;
					break;
			}
			return result;
		}
	}
}