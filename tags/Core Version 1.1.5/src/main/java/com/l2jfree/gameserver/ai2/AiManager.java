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
package com.l2jfree.gameserver.ai2;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai2.AiInstance.QueueEventRunner;

/**
 * This class will load all the AI's and retain all of them.
 * It is a singleton
 * @author -Wooden-
 *
 */
public class AiManager
{
	protected final static Log			_log	= LogFactory.getLog(AiManager.class);
	private static AiManager			_instance;
	private List<AiInstance>			_aiList;
	private Map<Integer, AiInstance>	_aiMap;
	private ThreadPoolManager			_tpm;
	private Map<String, String>			_paramcache;

	public static AiManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new AiManager();
		}
		return _instance;
	}

	private AiManager()
	{
		_aiList = new FastList<AiInstance>();
		_aiMap = new FastMap<Integer, AiInstance>();
		_tpm = ThreadPoolManager.getInstance();
		_paramcache = new FastMap<String, String>();
		load();
	}

	public void load()
	{
		URL url = Class.class.getResource("/net/sf/l2j/gameserver/ai/managers");

		if (url == null)
		{
			_log.error("Could not open the ai managers folder. No ai will be loaded!");
			return;
		}
		File directory = new File(url.getFile());
		for (String file : directory.list())
		{
			if (file.endsWith(".class"))
			{
				try
				{
					Class<?> managerClass = Class.forName("com.l2jfree.gameserver.ai.managers." + file.substring(0, file.length() - 6));
					Object managerObject = managerClass.newInstance();
					if (!(managerObject instanceof ISpecificAiManager))
					{
						_log.info("A class that was not a ISpecificAiManager was found in the ai managers folder.");
						continue;
					}
					ISpecificAiManager managerInstance = (ISpecificAiManager) managerObject;
					for (EventHandler handler : managerInstance.getEventHandlers())
					{
						AiPlugingParameters pparams = handler.getPlugingParameters();
						pparams.convertToIDs();
						boolean perfectMatch = false;
						// let's check if any previously created AiInstance is allready used for the NPC concerned by this handler
						List<Intersection> intersections = new FastList<Intersection>();
						for (AiInstance ai : _aiList)
						{
							if (ai.getPluginingParamaters().equals(pparams))
							{
								ai.addHandler(handler);
								perfectMatch = true;
								break;
							}
							Intersection intersection = new Intersection(ai);
							for (int id : pparams.getIDs())
							{
								if (ai.getPluginingParamaters().contains(id))
								{
									//intersection with this AI
									intersection.ids.add(id);
								}
							}
							if (!intersection.isEmpty())
								intersections.add(intersection);
						}
						if (perfectMatch)
							continue;
						for (Intersection i : intersections)
						{
							//remove secant ids on both AiInstances
							pparams.removeIDs(i.ids);
							i.ai.getPluginingParamaters().removeIDs(i.ids);
							//create a new instance with the secant ids that will inherit all the handlers from the secant Ai
							AiPlugingParameters newAiPparams = new AiPlugingParameters(null, null, null, i.ids, null);
							AiInstance newAi = new AiInstance(i.ai, newAiPparams);
							newAi.addHandler(handler);
							_aiList.add(newAi);
						}
						if (pparams.isEmpty())
							continue;
						// create a new instance with the remaining ids
						AiInstance newAi = new AiInstance(pparams);
						newAi.addHandler(handler);
						_aiList.add(newAi);
					}

				}
				catch (ClassCastException e)
				{
					_log.error(e.getMessage(), e);
				}
				catch (ClassNotFoundException e)
				{
					_log.error(e.getMessage(), e);
				}
				catch (IllegalArgumentException e)
				{
					_log.error(e.getMessage(), e);
				}
				catch (SecurityException e)
				{
					_log.error(e.getMessage(), e);
				}
				catch (InstantiationException e)
				{
					_log.error(e.getMessage(), e);
				}
				catch (IllegalAccessException e)
				{
					_log.error(e.getMessage(), e);
				}
			}
		}

		// build a mighty map
		for (AiInstance ai : _aiList)
		{
			for (Integer i : ai.getHandledNPCIds())
			{
				_aiMap.put(i, ai);
			}
		}
	}

	public void executeEventHandler(QueueEventRunner runner)
	{
		_tpm.executeAi(runner);
	}

	/**
	 * @param instance
	 */
	public void addAiInstance(AiInstance instance)
	{
		_aiList.add(instance);
	}

	/**
	 * @param npcId
	 * @return
	 */
	public AiInstance getAiForNPCId(int npcId)
	{
		return _aiMap.get(npcId);
	}

	public String getParameter(String who, String paramsType, String param1, String param2)
	{
		String key = who + ":" + paramsType + ":" + param1 + ":" + param2;
		String cacheResult = _paramcache.get(key);
		if (cacheResult != null)
			return cacheResult;
		String result = null;
		// get from SQL
		_paramcache.put(key, result);
		return null;
	}

	private class Intersection
	{
		public AiInstance	ai;
		public Set<Integer>	ids;

		public Intersection(AiInstance instance)
		{
			ai = instance;
			ids = new FastSet<Integer>();
		}

		public boolean isEmpty()
		{
			return ids.isEmpty();
		}
	}
}
