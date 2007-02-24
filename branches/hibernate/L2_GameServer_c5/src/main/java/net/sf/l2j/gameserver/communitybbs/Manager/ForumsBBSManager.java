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
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2Registry;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ForumsBBSManager extends BaseBBSManager
{
	private final static Log _log = LogFactory.getLog(ForumsBBSManager.class.getName());
	private FastMap<Integer, Forum> _root;
	private FastList<Forum> _table;
	private static ForumsBBSManager _Instance;
	private int lastid = 1;

	/**
	 * @return
	 */
	public static ForumsBBSManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new ForumsBBSManager();
			_Instance.load();
		}
		return _Instance;
	}

	public ForumsBBSManager()
	{
		_root = new FastMap<Integer, Forum>();
		_table = new FastList<Forum>();		
	}

	public void addForum(Forum ff)
	{
		_table.add(ff);
	
		if (ff.getID() > lastid)
		{
			lastid = ff.getID();
		}		
	}

	/**
	 * 
	 */
	private void load()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2Registry.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery();			
			while (result.next())
			{
				
				Forum f = new Forum(Integer.parseInt(result.getString("forum_id")), null);
				_root.put(Integer.parseInt(result.getString("forum_id")), f);								
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("data error on Forum (root): " + e,e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @param string
	 * @return
	 */
	public Forum getForumByName(String Name)
	{
		for (Forum f : _table)
		{
			if (f.getName().equals(Name))
			{
				return f;
			}
		}
		
		return null;
	}

	/**
	 * @param name
	 * @param forumByName
	 * @return
	 */
	public Forum CreateNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		Forum forum;
		forum = new Forum(name, parent, type, perm, oid);		
		forum.insertindb();
		return forum;
	}

	/**
	 * @return
	 */
	public int GetANewID()
	{
		lastid++;
		return lastid;
	}

	/**
	 * @param idf
	 * @return
	 */
	public Forum getForumByID(int idf)
	{		
		for (Forum f : _table)
		{
			if (f.getID() == idf)
			{
				return f;
			}
		}		
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub
		
	}
}
