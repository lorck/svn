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

import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.L2BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public class Say2 extends ClientBasePacket
{
    private static final String _C__38_SAY2 = "[C] 38 Say2";
    private final static Log _log = LogFactory.getLog(Say2.class.getName());
    private static Log _logChat = LogFactory.getLog("chat");

    public final static int ALL = 0;
    public final static int SHOUT = 1; //!
    public final static int TELL = 2;
    public final static int PARTY = 3; //#
    public final static int CLAN = 4;  //@
    public final static int GM = 5;    
    public final static int PETITION_PLAYER = 6; // used for petition
    public final static int PETITION_GM = 7; //* used for petition
    public final static int TRADE = 8; //+
    public final static int ALLIANCE = 9; //$
    public final static int ANNOUNCEMENT = 10;
    public final static int PARTYROOM_ALL = 15; //(yellow)
    public final static int PARTYROOM_COMMANDER = 16; //(blue)
    public final static int HERO_VOICE = 17;
    
    public final static String[] chatNames = {
        "ALL  ",
        "SHOUT",
        "TELL ",
        "PARTY",
        "CLAN ",
        "GM   ",
        "PETITION_PLAYER",
        "PETITION_GM",
        "TRADE",
        "ALLIANCE",
        "ANNOUNCEMENT", //10
        "WILLCRASHCLIENT:)",
        "FAKEALL?",
        "FAKEALL?",
        "FAKEALL?",
        "PARTYROOM_ALL",
        "PARTYROOM_COMMANDER",
        "HERO_VOICE"
    };
    
    private String _text;
    private int _type;
    private String _target;
    /**
     * packet type id 0x38
     * format:      cSd (S)
     * @param decrypt
     */
    public Say2(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _text = readS();
        _type = readD();
        _target = (_type == TELL) ? readS() : null;  
    }

    void runImpl()
    {
        if (_log.isDebugEnabled()) 
            _log.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");
        
        if(_type >= chatNames.length)
        {
            _log.warn("Say2: Invalid type: "+_type);
            return;
        }
        
        L2PcInstance activeChar = getClient().getActiveChar();
                
        if (activeChar == null)
        {
            _log.warn("[Say2.java] Active Character is null.");
            return;
        }

        if (activeChar.isChatBanned())
        {
            {
				// [L2J_JP EDIT]
				activeChar.sendPacket(new SystemMessage(SystemMessage.CHATTING_IS_CURRENTLY_PROHIBITED));
                return;
            }
        }
        
        if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
        {
            if (_type == TELL || _type == SHOUT || _type == TRADE)
            {
                activeChar.sendMessage("You can not chat with the outside of the jail.");
                return;
            }
        }
        
        if (_type == PETITION_PLAYER && activeChar.isGM()) 
            _type = PETITION_GM;
                    

        if (Config.LOG_CHAT) 
        {
            if (_type == TELL)
                _logChat.info( chatNames[_type] + "[" + activeChar.getName() + " to "+_target+"] " + _text);
            else
                _logChat.info( chatNames[_type] + "[" + activeChar.getName() + "] " + _text);
        }

        if(Config.USE_SAY_FILTER) 
        {
            for(String pattern : Config.FILTER_LIST)
            {
                    _text = _text.replaceAll(pattern,"^_^");
            }
        }       
        
        CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);
    
                
        switch (_type)
        {
            case TELL:
                L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
                
                if (receiver != null && 
                        !L2BlockList.isBlocked(receiver, activeChar))
                {   
                    if (!receiver.getMessageRefusal())
                    {
                        receiver.sendPacket(cs);
                        activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(),  _type, "->" + receiver.getName(), _text));
                    }
                    else
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
                    }
        }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_NOT_ONLINE);
                    sm.addString(_target);        
                    activeChar.sendPacket(sm);
                    sm = null;
                }
            break;
        case SHOUT:
                if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") ||
                        (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
                {
                    int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY())) 
                            player.sendPacket(cs);
                    }
                }
                else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
                {
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        player.sendPacket(cs);                        
                    }
                }                
                break;
        case TRADE: 
                if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") ||
                        (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
            {
                for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                {
                        player.sendPacket(cs);
                }
                } else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
                {
                    int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        if (region == MapRegionTable.getInstance().getMapRegion(player.getX(),player.getY())) 
                            player.sendPacket(cs);
                    }
                }
             break;
        case ALL:
            if (_text.startsWith(".")) 
            {
                StringTokenizer st = new StringTokenizer(_text);

               if (st.countTokens()>=1)
               {
                   String command = st.nextToken().substring(1);
                   String params = "";
                   if (st.countTokens()==0){ 
                       if (activeChar.getTarget()!=null) params=activeChar.getTarget().getName();
                   } else params=st.nextToken().trim();
                                   
                   IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
                                     
                    if (vch != null) 
                        vch.useVoicedCommand(command, activeChar, params);
                    else
                        _log.warn("No handler registered for voice command '"+command+"'");
                }                
            }
            else
            {
                for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
                {
                    if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
                        player.sendPacket(cs);
                }
                activeChar.sendPacket(cs);
            }   
            break;
        case CLAN:
            if (activeChar.getClan() != null)
                activeChar.getClan().broadcastToOnlineMembers(cs);
            break;  
        case ALLIANCE:
            if (activeChar.getClan() != null)
                activeChar.getClan().broadcastToOnlineAllyMembers(cs);
            break;
        case PARTY:
            if (activeChar.isInParty())
                activeChar.getParty().broadcastToPartyMembers(cs);
            break;
        case PETITION_PLAYER:
        case PETITION_GM:
            if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
            {
                activeChar.sendPacket(new SystemMessage(745));
                break;
            }
            
            PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
            break;
        case PARTYROOM_ALL:
        case PARTYROOM_COMMANDER:
            //PartyCommandManager.getInstance().sendChannelMessage(activeChar, _text);
            break;
        case HERO_VOICE:
            if (activeChar.isHero())
            {
                for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    if (!L2BlockList.isBlocked(player, activeChar))
                        player.sendPacket(cs);
            }
            break;
        }           
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__38_SAY2;
    }

    public void changeString(String newString) { _text = newString; }
   
    public String getSay() { return _text; }
}
