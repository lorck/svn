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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.QuestState;

/**
 * 7B 74 00 75 00 74 00 6F 00 72 00 69 00 61 00 6C 
 * 00 5F 00 63 00 6C 00 6F 00 73 00 65 00 5F 00 32 
 * 00 00 00 
 * 
 * Format: (c) S
 * 
 * @author  DaDummy
 */
public class RequestTutorialLinkHtml extends L2GameClientPacket
{
    private static final String _C__7B_REQUESTTUTORIALLINKHTML = "[C] 7B equestTutorialLinkHtml";
    @SuppressWarnings("unused")
    private String _link;

    @Override
    protected void readImpl()
    {
        _link = readS(); // link
    }
    
    /**
     * @see net.sf.l2j.gameserver.network.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if(player == null)
            return;

        QuestState qs = player.getQuestState("255_Tutorial");
        if(qs != null)
            qs.getQuest().notifyEvent(_link, null, player);
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__7B_REQUESTTUTORIALLINKHTML;
    }
}
