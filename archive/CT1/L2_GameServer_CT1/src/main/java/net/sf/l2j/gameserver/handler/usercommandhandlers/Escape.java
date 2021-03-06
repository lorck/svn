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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.util.Broadcast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 */
public class Escape implements IUserCommandHandler
{
    static Log _log = LogFactory.getLog(Escape.class);
    private static final int[] COMMAND_IDS = { 52 }; 

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {   
        if (activeChar.isCastingNow() || activeChar.isMovementDisabled()
            || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode())
            return false;

        // [L2J_JP ADD]
        if(activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
        {
            activeChar.sendMessage("You can not escape from here.");
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }
 
        int unstuckTimer = (activeChar.getAccessLevel() >= Config.GM_ESCAPE ? 5000 : Config.UNSTUCK_INTERVAL * 1000 );
        
        // Check to see if the player is in a festival.
        if (activeChar.isFestivalParticipant()) 
        {
            activeChar.sendMessage("You may not use an escape command in a festival.");
            return false;
        }
        
        // Check to see if player is in jail
        if (activeChar.isInJail())
        {
            activeChar.sendMessage("You can not escape from jail.");
            return false;
        }

        if(activeChar.getAccessLevel() >= Config.GM_ESCAPE)
        {
            activeChar.sendMessage("You use Fast Escape: 5 seconds.");
        }
        else if(Config.UNSTUCK_INTERVAL > 100)
        {
            activeChar.sendMessage("You use Escape: " + unstuckTimer/60000 + " minutes.");
        }
        else
            activeChar.sendMessage("You use Escape: " + unstuckTimer/1000 + " seconds.");

        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
        SetupGauge sg = new SetupGauge(0, unstuckTimer);
        activeChar.sendPacket(sg);
        //End SoE Animation section

        EscapeFinalizer ef = new EscapeFinalizer(activeChar);
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+unstuckTimer/GameTimeController.MILLIS_IN_TICK);
        
        return true;
    }

    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;
        
        EscapeFinalizer(L2PcInstance activeChar)
        {
            _activeChar = activeChar;
        }
        
        public void run()
        {
            if (_activeChar.isDead()) 
                return; 
            
            _activeChar.setIsIn7sDungeon(false);
            
            _activeChar.enableAllSkills();
            
            try 
            {
                _activeChar.teleToLocation(TeleportWhereType.Town);
            } catch (Throwable e) { _log.error(e.getMessage(),e); }
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
