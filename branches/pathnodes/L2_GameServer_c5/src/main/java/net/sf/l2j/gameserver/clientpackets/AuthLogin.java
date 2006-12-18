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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class AuthLogin extends ClientBasePacket
{
	private static final String _C__08_AUTHLOGIN = "[C] 08 AuthLogin";
	private final static Log _log = LogFactory.getLog(AuthLogin.class.getName());
	
	// loginName + keys must match what the loginserver used.  
	private final String _loginName;
	/*private final long _key1;
	private final long _key2;
	private final long _key3;
	private final long _key4;*/
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	
	/**
	 * @param decrypt
	 */
	public AuthLogin(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);

		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		while (buf.hasRemaining()) buf.get();
	}

	/** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
	
	void runImpl()
	{
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		if (_log.isDebugEnabled()) {
			_log.info("user:" + _loginName);
			_log.info("key:" + key);
		}
		
		ClientThread client = getClient();
		client.setLoginName(_loginName);
		LoginServerThread.getInstance().addGameServerLogin(_loginName,getConnection());
		
		LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName,client,key);
	}

    
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__08_AUTHLOGIN;
	}
}
