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
package com.l2jfree.gameserver.skills.conditions;

import com.l2jfree.gameserver.skills.Env;

/**
 * @author mkizub
 */
public abstract class Condition implements ConditionListener
{

	//static final Log _log = LogFactory.getLog(Condition.class.getName());

	private ConditionListener	_listener;
	private String				_msg;
	private int					_msgId;
	private boolean				_result;

	public final void setMessage(String msg)
	{
		_msg = msg;
	}

	public final String getMessage()
	{
		return _msg;
	}

	public final void setMessageId(int msgId)
	{
		_msgId = msgId;
	}

	public final int getMessageId()
	{
		return _msgId;
	}

	void setListener(ConditionListener listener)
	{
		_listener = listener;
		notifyChanged();
	}

	final ConditionListener getListener()
	{
		return _listener;
	}

	public final boolean test(Env env)
	{
		boolean res = testImpl(env);
		if (_listener != null && res != _result)
		{
			_result = res;
			notifyChanged();
		}
		return res;
	}

	abstract boolean testImpl(Env env);

	public void notifyChanged()
	{
		if (_listener != null)
			_listener.notifyChanged();
	}
}
