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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.TradeDone;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class AnswerTradeRequest extends L2GameClientPacket
{
    private static final String _C__40_ANSWERTRADEREQUEST = "[C] 40 AnswerTradeRequest";

    private int _response;

    @Override
    protected void readImpl()
    {
        _response = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

        if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null 
            && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
            player.sendMessage("Transactions are not allowed during restart/shutdown.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Unsufficient privileges.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        
        L2PcInstance partner = player.getActiveRequester();
        if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
        {
            // Trade partner not found, cancel trade
            player.sendPacket(new TradeDone(0));
            SystemMessage msg = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.sendPacket(msg);
            player.setActiveRequester(null);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

		if (_response == 1)
		{
			player.startTrade(partner);
		}
		else
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST);
			msg.addString(player.getName());
			partner.sendPacket(msg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}

		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__40_ANSWERTRADEREQUEST;
	}
}
