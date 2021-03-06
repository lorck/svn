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
package com.l2jfree.status.commands;

import com.l2jfree.status.StatusCommand;

/**
 * @author NB4L1
 */
public final class GC extends StatusCommand
{
	public GC()
	{
		super("garbage collection and finalization", "gc");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		printMemoryStatistics();
		
		println();
		println("##################################");
		print("Collecting garbage...        ");
		long begin = System.currentTimeMillis();
		System.gc();
		println("[OK]");
		println("\tDuration: " + (System.currentTimeMillis() - begin) + "msec");
		println("##################################");
		println();
		
		printMemoryStatistics();
		
		println();
		println("##################################");
		print("Finalizing...                ");
		long begin2 = System.currentTimeMillis();
		System.runFinalization();
		println("[OK]");
		println("\tDuration: " + (System.currentTimeMillis() - begin2) + "msec");
		println("##################################");
		println();
		
		printMemoryStatistics();
	}
}
