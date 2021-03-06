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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class FortSiegeGuardManager {

    private static final Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());

    // =========================================================
    // Data Field
    private Fort _fort;
    private List<L2Spawn> _siegeGuardSpawn  = new FastList<L2Spawn>();

    // =========================================================
    // Constructor
    public FortSiegeGuardManager(Fort fort)
    {
        _fort = fort;
    }

    // =========================================================
    // Method - Public
    /**
     * Add guard.<BR><BR>
     */
    public void addSiegeGuard(L2PcInstance activeChar, int npcId)
    {
        if (activeChar == null) return;
        addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
    }

    /**
     * Add guard.<BR><BR>
     */
    public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
    {
        saveSiegeGuard(x, y, z, heading, npcId, 0);
    }

    /**
     * Hire merc.<BR><BR>
     */
    public void hireMerc(L2PcInstance activeChar, int npcId)
    {
        if (activeChar == null) return;
        hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
    }

    /**
     * Hire merc.<BR><BR>
     */
    public void hireMerc(int x, int y, int z, int heading, int npcId)
    {
        saveSiegeGuard(x, y, z, heading, npcId, 1);
    }

    /**
     * Remove a single mercenary, identified by the npcId and location.
     * Presumably, this is used when a fort lord picks up a previously dropped ticket
     */
    public void removeMerc(int npcId, int x, int y, int z)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
            statement.setInt(1, npcId);
            statement.setInt(2, x);
            statement.setInt(3, y);
            statement.setInt(4, z);
            statement.execute();
            statement.close();
        }
        catch (Exception e1)
        {
            _log.warning("Error deleting hired siege guard at " + x +','+y+','+z + ":" + e1);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    /**
     * Remove mercs.<BR><BR>
     */
    public void removeMercs()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where fortId = ? And isHired = 1");
            statement.setInt(1, getFort().getFortId());
            statement.execute();
            statement.close();
        }
        catch (Exception e1)
        {
            _log.warning("Error deleting hired siege guard for fort " + getFort().getName() + ":" + e1);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    /**
     * Spawn guards.<BR><BR>
     */
    public void spawnSiegeGuard()
    {
        try
        {
            int     hiredCount  = 0;
            //hiredMax    = MercTicketManager.getInstance().getMaxAllowedMerc(_fort.getFortId());  
            boolean isHired     = (getFort().getOwnerId() > 0) ? true : false;
            loadSiegeGuard(); 
            for (L2Spawn spawn: getSiegeGuardSpawn()) 
            {
                if (spawn != null)
                {
                    spawn.init();
                    if (isHired)
                    {
                        hiredCount++; 
                        //if (hiredCount > hiredMax)
                        //    return;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            _log.warning("Error spawning siege guards for fort " + getFort().getName() + ":" + t.toString()); 
        }
    }

    /**
     * Unspawn guards.<BR><BR>
     */
    public void unspawnSiegeGuard()
    {
        for (L2Spawn spawn: getSiegeGuardSpawn())
        {
            if (spawn == null)
                continue;

            spawn.stopRespawn();
            spawn.getLastSpawn().doDie(spawn.getLastSpawn());
        }

        getSiegeGuardSpawn().clear();
    }

    // =========================================================
    // Method - Private
    /**
     * Load guards.<BR><BR>
     */
    private void loadSiegeGuard()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_siege_guards Where fortId = ? ");
            statement.setInt(1, getFort().getFortId());
            ResultSet rs = statement.executeQuery();

            L2Spawn spawn1;
            L2NpcTemplate template1;

            while (rs.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
                if (template1 != null)
                {
                    spawn1 = new L2Spawn(template1);
                    spawn1.setId(rs.getInt("id"));
                    spawn1.setAmount(1);
                    spawn1.setLocx(rs.getInt("x"));
                    spawn1.setLocy(rs.getInt("y"));
                    spawn1.setLocz(rs.getInt("z"));
                    spawn1.setHeading(rs.getInt("heading"));
                    spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
                    spawn1.setLocation(0);

                    _siegeGuardSpawn.add(spawn1);
                }
                else
                {
                    _log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
                }
            }
            rs.close();
            statement.close();
        }
        catch (Exception e1)
        {
            _log.warning("Error loading siege guard for fort " + getFort().getName() + ":" + e1);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    /**
     * Save guards.<BR><BR>
     */
    private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Insert Into fort_siege_guards (fortId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, getFort().getFortId());
            statement.setInt(2, npcId);
            statement.setInt(3, x);
            statement.setInt(4, y);
            statement.setInt(5, z);
            statement.setInt(6, heading);
            if (isHire == 1)
                statement.setInt(7, 0);
            else
                statement.setInt(7, 600);
            statement.setInt(8, isHire);
            statement.execute();
            statement.close();
        }
        catch (Exception e1)
        {
            _log.warning("Error adding siege guard for fort " + getFort().getName() + ":" + e1);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    // =========================================================
    // Proeprty

    public final Fort getFort()
    {
        return _fort;
    }

    public final List<L2Spawn> getSiegeGuardSpawn()
    {
        return _siegeGuardSpawn;
    }
}
