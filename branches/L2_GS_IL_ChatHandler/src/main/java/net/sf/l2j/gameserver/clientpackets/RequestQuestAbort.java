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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestQuestAbort extends L2GameClientPacket
{
    private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
    private final static Log _log = LogFactory.getLog(RequestQuestAbort.class.getName());

    
    private int _questId;
    /**
     * packet type id 0x64<p>
     */
    @Override
    protected void readImpl()
    {
        _questId = readD();
    }
    
    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        
        Quest qe = QuestManager.getInstance().getQuest(_questId);
        if (qe != null)
        {
            QuestState qs = activeChar.getQuestState(qe.getName());
            if(qs != null)
            {
                qs.exitQuest(true);
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                sm.addString("Quest aborted.");
                activeChar.sendPacket(sm);
                sm = null;
                QuestList ql = new QuestList();
                activeChar.sendPacket(ql);
            } else
            {
                if (_log.isDebugEnabled()) _log.info("Player '"+activeChar.getName()+"' try to abort quest "+qe.getName()+" but he didn't have it started.");
            }
        } else
        {
            if (_log.isDebugEnabled()) _log.warn("Quest (id='"+_questId+"') not found.");
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__64_REQUESTQUESTABORT;
    }
}
