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
package com.l2jfree.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.items.model.Item;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.item.L2Armor;
import com.l2jfree.gameserver.templates.item.L2EtcItem;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.templates.item.L2Weapon;

/**
 * @author mkizub
 */
public class SkillsEngine
{

	protected static Log				_log			= LogFactory.getLog(SkillsEngine.class.getName());
	private static final SkillsEngine	_instance		= new SkillsEngine();

	private FastList<File>				_armorFiles		= new FastList<File>();
	private FastList<File>				_weaponFiles	= new FastList<File>();
	private FastList<File>				_etcitemFiles	= new FastList<File>();
	private FastList<File>				_skillFiles		= new FastList<File>();

	public static SkillsEngine getInstance()
	{
		return _instance;
	}

	private SkillsEngine()
	{
		hashFiles("data/stats/etcitem", _etcitemFiles);
		hashFiles("data/stats/armor", _armorFiles);
		hashFiles("data/stats/weapon", _weaponFiles);
		hashFiles("data/stats/skills", _skillFiles);
	}

	private void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
				if (!f.getName().startsWith("custom"))
					hash.add(f);
		}
		File customfile = new File(Config.DATAPACK_ROOT, dirname + "/custom.xml");
		if (customfile.exists())
			hash.add(customfile);
	}

	public FastList<L2Skill> loadSkills(File file)
	{
		if (file == null)
		{
			_log.info("Skill file not found (NULL passed)");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}

	public void loadAllSkills(Map<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for (File file : _skillFiles)
		{
			List<L2Skill> s = loadSkills(file);
			if (s == null)
				continue;
			for (L2Skill skill : s)
			{
				allSkills.put(SkillTable.getSkillHashCode(skill), skill);
				count++;
			}
		}
		_log.info("SkillsEngine: Loaded " + count + " Skill templates from XML files.");
	}

	public FastList<L2Armor> loadArmors(FastMap<Integer, Item> armorData)
	{
		FastList<L2Armor> list = new FastList<L2Armor>();
		for (L2Item item : loadData(armorData, _armorFiles))
		{
			list.add((L2Armor) item);
		}
		return list;
	}

	public List<L2Weapon> loadWeapons(FastMap<Integer, Item> weaponData)
	{
		FastList<L2Weapon> list = new FastList<L2Weapon>();
		for (L2Item item : loadData(weaponData, _weaponFiles))
		{
			list.add((L2Weapon) item);
		}
		return list;
	}

	public FastList<L2EtcItem> loadItems(FastMap<Integer, Item> itemData)
	{
		FastList<L2EtcItem> list = new FastList<L2EtcItem>();
		List<Integer> xmlItem = new FastList<Integer>();

		for (L2Item item : loadData(itemData, _etcitemFiles))
		{
			list.add((L2EtcItem)item);
			xmlItem.add(item.getItemId());
		}
		for (Item item : itemData.values())
		{
			if (!xmlItem.contains(item.id))
				list.add(new L2EtcItem((L2EtcItemType) item.type, item.set));

		}
		return list;
	}

	public FastList<L2Item> loadData(FastMap<Integer, Item> itemData, FastList<File> files)
	{
		FastList<L2Item> list = new FastList<L2Item>();
		for (File f : files)
		{
			DocumentItem document = new DocumentItem(itemData, f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
}
