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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.lang.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class FriendsBBSManager extends BaseBBSManager
{
    private static Logger _logChat = Logger.getLogger("chat"); 
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public void parsecmd(String command, L2PcInstance activeChar)
    {
        if (command.equals("_bbsgetfav"))
        {
            showFriendsList(activeChar);    
        }
        else if (command.startsWith("_bbsgetfav;playerdelete;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            deleteFriend(activeChar, name);
            showFriendsList(activeChar);
        }
        else if (command.startsWith("_bbsgetfav;playeradd;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            addFriend(activeChar, name);
            showFriendsList(activeChar);
        }
        else if (command.startsWith("_bbsgetfav;playerinfo;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            showFriendsPI(activeChar, name);
        }
        else
        {
            if(Config.COMMUNITY_TYPE.toLowerCase().equals("old"))
            {
                showFriendsList(activeChar);    
            }
            else
            {
                ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
                activeChar.sendPacket(sb);
                activeChar.sendPacket(new ShowBoard(null,"102"));
                activeChar.sendPacket(new ShowBoard(null,"103"));
            }
        }
    }

    /**
     * @param activeChar
     * @param name
     */
    private void showFriendsPI(L2PcInstance activeChar, String name)
    {
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");        
        L2PcInstance player = L2World.getInstance().getPlayer(name);
        
        if (player != null)
        {
            String sex = "Male";
            if (player.getSex() == 1)
            {
                sex = "Female";
            }
            String levelApprox = "low";
            if (player.getLevel() >= 60)
                levelApprox = "very high";
            else if (player.getLevel() >= 40)
                levelApprox = "high";
            else if (player.getLevel() >= 20)
                levelApprox = "medium";
            htmlCode.append("<table border=0><tr><td>"+player.getName()+" ("+sex+" "+player.getTemplate().className+"):</td></tr>");
            htmlCode.append("<tr><td>Level: "+levelApprox+"</td></tr>");
            htmlCode.append("<tr><td><br></td></tr>");
            
            if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId()
                    || Config.SHOW_LEVEL_COMMUNITYBOARD))
            {
                long nextLevelExp = 0;
                long nextLevelExpNeeded = 0;
                if (player.getLevel() < 75)
                {
                    nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
                    nextLevelExpNeeded = nextLevelExp-player.getExp();
                }
                
                htmlCode.append("<tr><td>Level: "+player.getLevel()+"</td></tr>");
                htmlCode.append("<tr><td>Experience: "+player.getExp()+"/"+nextLevelExp+"</td></tr>");
                htmlCode.append("<tr><td>Experience needed for level up: "+nextLevelExpNeeded+"</td></tr>");
                htmlCode.append("<tr><td><br></td></tr>");
            }
            
            int uptime = (int)player.getUptime()/1000;
            int h = uptime/3600;
            int m = (uptime-(h*3600))/60;
            int s = ((uptime-(h*3600))-(m*60));
            
            htmlCode.append("<tr><td>Uptime: "+h+"h "+m+"m "+s+"s</td></tr>");
            htmlCode.append("<tr><td><br></td></tr>");
            
            if (player.getClan() != null)
            {
                htmlCode.append("<tr><td>Clan: "+player.getClan().getName()+"</td></tr>");
                htmlCode.append("<tr><td><br></td></tr>");
            }
            
            htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM "+player.getName()+" pm pm pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _bbsgetfav\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
            htmlCode.append("</td></tr></table>");          
            htmlCode.append("</body></html>");
            separateAndSend(htmlCode.toString(),activeChar);
        }
        else
        {
            ShowBoard sb = new ShowBoard("<html><body><br><br><center>No player with name "+name+"</center><br><br></body></html>","101");
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null,"102"));
            activeChar.sendPacket(new ShowBoard(null,"103"));  
        }
    }

    /**
     * @param activeChar
     */
    private void showFriendsList(L2PcInstance activeChar)
    {       
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();

        Connection con = null;
        
        try
        {
            String sqlQuery = "SELECT friend_id, friend_name FROM character_friends WHERE " +
                    "char_id=" + activeChar.getObjectId() + " ORDER BY friend_name ASC";
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            ResultSet rset = statement.executeQuery(sqlQuery);

            //add new friend
            htmlCode.append("<table border=0>");
            //htmlCode.append("<tr><td>Add new friend:</td><td><edit var= \"friendname\" width=50></td><td><button value=\"Add Friend\" action=\"bypass -h _bbsgetfav;playeradd;$friendname\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            htmlCode.append("<tr><td>To add someone to your friendlist type '/addfriend friendname'</td></tr>");
            htmlCode.append("<tr><td>You can remove someone from your friendlist only when your friend is online</td></tr>");
            htmlCode.append("</table><br>");
            //show friendlist
            htmlCode.append("<table border=0>");
            htmlCode.append("<tr><td align=left FIXWIDTH=150>Your Friends List</td><td></td></tr>");
            while (rset.next()){
                int friendId = rset.getInt("friend_id");
                String friendName = rset.getString("friend_name");
                
                L2PcInstance friend = (L2PcInstance)L2World.getInstance().findObject(friendId);
                if (friend == null)
                    htmlCode.append("<tr><td align=left valign=top FIXWIDTH=150>"+friendName+"</td><td>Offline</td><td></td></tr>");
                else
                    htmlCode.append("<tr><td align=left valign=top FIXWIDTH=150><a action=\"bypass _bbsgetfav;playerinfo;" + friendName + "\">"+friendName+"</a></td><td>Online</td><td><a action=\"bypass _bbsgetfav;playerdelete;" + friendName + "\">delete</a></td></tr>");
            }
            htmlCode.append("</table>");
            htmlCode.append("</body></html>");
            statement.close();
        }
        catch (Exception e) {
            //ignore
        }

        separateAndSend(htmlCode.toString(),activeChar);
      
    }

    /**
     * @param activeChar
     */
    private void deleteFriend(L2PcInstance activeChar, String name)
    {   
        L2PcInstance player = L2World.getInstance().getPlayer(name);
        Connection con = null;
        try
        {
            String sqlQuery = "DELETE FROM character_friends WHERE (char_id="+activeChar.getObjectId()+" AND friend_id="+player.getObjectId()+") OR (char_id="+player.getObjectId()+" AND friend_id="+activeChar.getObjectId()+")";
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            ResultSet rset = statement.executeQuery(sqlQuery);
            statement.close();
        }
        catch (Exception e) {
            //ignore
        }
    }
    
    /**
     * @param activeChar
     */
    private void addFriend(L2PcInstance activeChar, String name)
    {   
        L2PcInstance player = L2World.getInstance().getPlayer(name);
        Connection con = null;
        try
        {
            String sqlQuery = "SELECT * FROM character_friends WHERE char_id="+activeChar.getObjectId()+" AND friend_id="+player.getObjectId();
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            ResultSet rset = statement.executeQuery(sqlQuery);
            if (rset.getRow() == 0){
                statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name) VALUES (?, ?, ?), (?, ?, ?)");
                statement.setInt(1, activeChar.getObjectId());
                statement.setInt(2, player.getObjectId());
                statement.setString(3, player.getName());
                statement.setInt(4, player.getObjectId());
                statement.setInt(5, activeChar.getObjectId());
                statement.setString(6, activeChar.getName());
                statement.execute();
                statement.close();
                SystemMessage msg = new SystemMessage(SystemMessage.INVITED_A_FRIEND);
                activeChar.sendPacket(msg);
                
                //Player added to your friendlist
                msg = new SystemMessage(SystemMessage.S1_ADDED_TO_FRIENDS);
                msg.addString(player.getName());
                activeChar.sendPacket(msg);
                
                //has joined as friend.
                msg = new SystemMessage(SystemMessage.S1_JOINED_AS_FRIEND);
                msg.addString(activeChar.getName());
                player.sendPacket(msg);
            }
        }
        catch (Exception e) {
            //ignore
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
    {
        if (activeChar == null)
            return;
        
        if (ar1.equals("PM"))
        {           
            TextBuilder htmlCode = new TextBuilder("<html><body><br>");
                htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

                try
                {
                    
                    L2PcInstance reciever = L2World.getInstance().getPlayer(ar2);
                    if (reciever == null)
                    {
                        htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _bbsgetfav;playerinfo;"+ar2+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        htmlCode.append("</td></tr></table></body></html>");
                        separateAndSend(htmlCode.toString(),activeChar);
                        return;
                    }
                    
                    if (Config.LOG_CHAT)  
                    { 
                        LogRecord record = new LogRecord(Level.INFO, ar3); 
                        record.setLoggerName("chat"); 
                        record.setParameters(new Object[]{"TELL", "[" + activeChar.getName() + " to "+reciever.getName()+"]"}); 
                        _logChat.log(record); 
                    } 
                    CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), ar3);
                    if (!reciever.getMessageRefusal())
                    {
                        reciever.sendPacket(cs);
                        activeChar.sendPacket(cs);
                        htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsgetfav;playerinfo;"+reciever.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        htmlCode.append("</td></tr></table></body></html>");
                        separateAndSend(htmlCode.toString(),activeChar)  ;
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);        
                        activeChar.sendPacket(sm);
                        parsecmd("_bbsgetfav;playerinfo;"+reciever.getName(), activeChar);
                    }
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    // ignore
                }
                    
        }
        else
        {
            ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+ar1+" is not implemented yet</center><br><br></body></html>","101");
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null,"102"));
            activeChar.sendPacket(new ShowBoard(null,"103"));  
        }
        
    }
    private static FriendsBBSManager _Instance = null;
    /**
     * @return
     */
    public static FriendsBBSManager getInstance()
    {
        if(_Instance == null)
        {
            _Instance = new FriendsBBSManager();
        }
        return _Instance;
    }

}