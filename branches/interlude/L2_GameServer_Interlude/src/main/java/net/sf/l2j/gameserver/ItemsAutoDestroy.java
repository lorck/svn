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
package net.sf.l2j.gameserver;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ItemsAutoDestroy
{
	protected static Log _log = LogFactory.getLog("ItemsAutoDestroy");
	private static ItemsAutoDestroy _instance;
	protected FastList<L2ItemInstance> _items = null;
	protected static long _sleep;
    
    private ItemsAutoDestroy()
    {
        _items = new FastList<L2ItemInstance>();
        _sleep  = Config.AUTODESTROY_ITEM_AFTER * 1000;
        if(_sleep == 0) // it should not happend as it is not called when AUTODESTROY_ITEM_AFTER = 0 but we never know..
            _sleep = 3600000;
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(),5000,5000);
    }
    
    public static ItemsAutoDestroy getInstance()
    {
        if (_instance == null)
        {
            System.out.println("Initializing ItemsAutoDestroy.");
            _instance = new ItemsAutoDestroy();
        }
        return _instance;
    }
    
    public void addItem (L2ItemInstance item)
    {
        item.setDropTime(System.currentTimeMillis());
        _items.add(item);
    }
    
    protected class CheckItemsForDestroy extends Thread
    {
        public void run()
        {
            if (_log.isDebugEnabled())
                _log.info("[ItemsAutoDestroy] : "+_items.size()+" items to check.");
            
            if (_items.isEmpty()) return;
            
            long curtime = System.currentTimeMillis();
            for (L2ItemInstance item : _items)
            {
                if (item == null || item.getDropTime()==0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
                    _items.remove(item);
                else
                {
                    if(item.getItemType() == L2EtcItemType.HERB )
                    {
                        if((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
                        {
                            L2World.getInstance().removeVisibleObject(item,item.getWorldRegion());
                            _items.remove(item);
                            if (Config.SAVE_DROPPED_ITEM)
                                ItemsOnGroundManager.getInstance().removeObject(item);
                        }
                    }
                    else if ( (curtime - item.getDropTime()) > _sleep)
                    {
                        L2World.getInstance().removeVisibleObject(item,item.getWorldRegion());
                        _items.remove(item);
                        if (Config.SAVE_DROPPED_ITEM)
                            ItemsOnGroundManager.getInstance().removeObject(item);
                    }
                }
            }

            if (_log.isDebugEnabled())
                _log.info("[ItemsAutoDestroy] : "+_items.size()+" items remaining.");
        }    
    }
}