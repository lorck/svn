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
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz
 * Format: cddd
 */
public class DlgAnswer extends L2GameClientPacket
{
    private static final String _C__C5_DLGANSWER = "[C] C5 DlgAnswer";
    private final static Log _log = LogFactory.getLog(DlgAnswer.class.getName());
    
    private int _messageId;
    private int _answer;
    private int _requesterId;
    
    @Override
    protected void readImpl()
    {
        _messageId = readD();
        _answer = readD();
        _requesterId = readD();
    }

    @Override
    public void runImpl()
    {
        L2PcInstance cha = getClient().getActiveChar();
        if (cha == null)
            return;

        if(_log.isDebugEnabled())
            _log.debug(getType()+": Answer acepted. Message ID "+_messageId+", answer "+_answer+", Requester ID "+_requesterId);
        if (_messageId == SystemMessageId.RESSURECTION_REQUEST.getId())
            cha.reviveAnswer(_answer);
        else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
            cha.teleportAnswer(_answer, _requesterId);
        else if (_messageId == SystemMessageId.S1.getId() && Config.ALLOW_WEDDING && cha.isEngageRequest())
            cha.engageAnswer(_answer);
        else if (_messageId == 1140)
            cha.gatesAnswer(_answer, 1);
        else if (_messageId == 1141)
            cha.gatesAnswer(_answer, 0);
    }

    @Override
    public String getType()
    {
        return _C__C5_DLGANSWER;
    }
}
