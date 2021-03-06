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
package com.l2jfree.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.itemhandlers.AdvQuestItems;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSoulShot;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSpice;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.BlessedSpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.Book;
import com.l2jfree.gameserver.handler.itemhandlers.CharChangePotions;
import com.l2jfree.gameserver.handler.itemhandlers.ChestKey;
import com.l2jfree.gameserver.handler.itemhandlers.CrystalCarol;
import com.l2jfree.gameserver.handler.itemhandlers.DoorKey;
import com.l2jfree.gameserver.handler.itemhandlers.EnchantAttr;
import com.l2jfree.gameserver.handler.itemhandlers.EnchantScrolls;
import com.l2jfree.gameserver.handler.itemhandlers.EnergyStone;
import com.l2jfree.gameserver.handler.itemhandlers.ExtractableItems;
import com.l2jfree.gameserver.handler.itemhandlers.Firework;
import com.l2jfree.gameserver.handler.itemhandlers.FishShots;
import com.l2jfree.gameserver.handler.itemhandlers.Harvester;
import com.l2jfree.gameserver.handler.itemhandlers.Maps;
import com.l2jfree.gameserver.handler.itemhandlers.MercTicket;
import com.l2jfree.gameserver.handler.itemhandlers.MysteryPotion;
import com.l2jfree.gameserver.handler.itemhandlers.Potions;
import com.l2jfree.gameserver.handler.itemhandlers.Recipes;
import com.l2jfree.gameserver.handler.itemhandlers.Remedy;
import com.l2jfree.gameserver.handler.itemhandlers.RollingDice;
import com.l2jfree.gameserver.handler.itemhandlers.ScrollOfEscape;
import com.l2jfree.gameserver.handler.itemhandlers.ScrollOfResurrection;
import com.l2jfree.gameserver.handler.itemhandlers.Scrolls;
import com.l2jfree.gameserver.handler.itemhandlers.Seed;
import com.l2jfree.gameserver.handler.itemhandlers.SevenSignsRecord;
import com.l2jfree.gameserver.handler.itemhandlers.SoulCrystals;
import com.l2jfree.gameserver.handler.itemhandlers.SoulShots;
import com.l2jfree.gameserver.handler.itemhandlers.SpecialXMas;
import com.l2jfree.gameserver.handler.itemhandlers.SpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.SummonItems;
import com.l2jfree.gameserver.handler.itemhandlers.TransformationItems;
import com.l2jfree.gameserver.handler.itemhandlers.WorldMap;

/**
 * This class manages handlers of items
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class ItemHandler
{
	private final static Log			_log	= LogFactory.getLog(ItemHandler.class.getName());
	private static ItemHandler			_instance;

	private Map<Integer, IItemHandler>	_datatable;

	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 * 
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		if (_instance == null)
			_instance = new ItemHandler();
		return _instance;
	}

	/**
	 * Returns the number of elements contained in datatable
	 * 
	 * @return int : Size of the datatable
	 */
	public int size()
	{
		return _datatable.size();
	}

	/**
	 * Constructor of ItemHandler
	 */
	private ItemHandler()
	{
		_datatable = new TreeMap<Integer, IItemHandler>();
		registerItemHandler(new AdvQuestItems());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new Book());
		registerItemHandler(new CharChangePotions());
		registerItemHandler(new ChestKey());
		registerItemHandler(new CrystalCarol());
		registerItemHandler(new DoorKey());
		registerItemHandler(new EnchantAttr());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new EnergyStone());
		registerItemHandler(new ExtractableItems());
		registerItemHandler(new Firework());
		registerItemHandler(new FishShots());
		registerItemHandler(new Harvester());
		registerItemHandler(new Maps());
		registerItemHandler(new MercTicket());
		registerItemHandler(new MysteryPotion());
		registerItemHandler(new Recipes());
		registerItemHandler(new Remedy());
		registerItemHandler(new RollingDice());
		registerItemHandler(new Potions());
		registerItemHandler(new ScrollOfEscape());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new Scrolls());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new Seed());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new SummonItems());
		registerItemHandler(new TransformationItems());
		registerItemHandler(new WorldMap());
		_log.info("ItemHandler: Loaded " + _datatable.size() + " handlers.");
	}

	/**
	 * Adds handler of item type in <I>datatable</I>.<BR>
	 * <BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item type (existing in classes of package
	 * itemhandlers) sets as key of the Map.
	 * 
	 * @param handler
	 *            (IItemHandler)
	 */
	public void registerItemHandler(IItemHandler handler)
	{
		// Get all ID corresponding to the item type of the handler
		int[] ids = handler.getItemIds();
		for (int element : ids)
		{
			_datatable.put(Integer.valueOf(element), handler);
		}
	}

	/**
	 * Returns the handler of the item
	 * 
	 * @param itemId :
	 *            int designating the itemID
	 * @return IItemHandler
	 */
	public IItemHandler getItemHandler(int itemId)
	{
		return _datatable.get(Integer.valueOf(itemId));
	}
}
