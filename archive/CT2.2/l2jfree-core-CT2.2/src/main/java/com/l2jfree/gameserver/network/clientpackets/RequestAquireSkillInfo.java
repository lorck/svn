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
import com.l2jfree.gameserver.datatables.SkillSpellbookTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2PledgeSkillLearn;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2SkillLearn;
import com.l2jfree.gameserver.model.L2TransformSkillLearn;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2TransformManagerInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AcquireSkillInfo;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class represents a packet that is sent by the client when a player selects a
 * skill to be learnt.
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestAquireSkillInfo extends L2GameClientPacket
{
    private static final String _C__6B_REQUESTAQUIRESKILLINFO = "[C] 6B RequestAquireSkillInfo";
    private final static Log _log = LogFactory.getLog(RequestAquireSkillInfo.class.getName());

    private int _id;
    private int _level;
	private int _skillType;

    /**
     * packet type id 0x6b packet
	 * format rev650 cddd
     */
    @Override
    protected void readImpl()
    {
        _id = readD();
        _level = readD();
        _skillType = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;
        L2NpcInstance trainer = activeChar.getLastFolkNPC();
        if ((trainer == null || !activeChar.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false)) && !activeChar.isGM())
        {
        	requestFailed(SystemMessageId.TOO_FAR_FROM_NPC);
            return;
        }

        L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

        boolean canteach = false;

        if (skill == null)
        {
        	if(_log.isDebugEnabled())
        		_log.info("skill id " + _id + " level " + _level + " is undefined. aquireSkillInfo failed.");
        	requestFailed(new SystemMessage(SystemMessageId.RACE_SETUP_FILE7_ERROR_S1).addNumber(_id));
            return;
        }

        if (_skillType == 0)
        {
            if (trainer instanceof L2TransformManagerInstance)
            {
                int itemId = 0;
                L2TransformSkillLearn[] skillst = SkillTreeTable.getInstance().getAvailableTransformSkills(activeChar);

                for (L2TransformSkillLearn s : skillst)
                {
                    if (s.getId() == _id && s.getLevel() == _level)
                    {
                        canteach = true;
                        itemId = s.getItemId();
                        break;
                    }
                }

                if (!canteach)
                {
                	sendPacket(ActionFailed.STATIC_PACKET);
                    return; // cheater
                }

                int requiredSp = 0;
                AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
                asi.addRequirement(1, itemId, 1, 0);
                sendPacket(asi);
                return;
            }
            if (trainer != null && !trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
            {
            	sendPacket(ActionFailed.STATIC_PACKET);
                return; // cheater
            }

            L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(activeChar, activeChar.getSkillLearningClassId());

            for (L2SkillLearn s : skills)
            {
                if (s.getId() == _id && s.getLevel() == _level)
                {
                    canteach = true;
                    break;
                }
            }

            if (!canteach)
            {
            	sendPacket(ActionFailed.STATIC_PACKET);
                return; // cheater :)
            }

            int requiredSp = SkillTreeTable.getInstance().getSkillCost(activeChar, skill);

            AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
            if (Config.ALT_SP_BOOK_NEEDED)
            {
                int spbId = -1;
                if (skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
                    spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, _level);
                else
                    spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);

                if (skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION || skill.getLevel() == 1 && spbId > -1)
                    asi.addRequirement(99, spbId, 1, 50);
            }
            sendPacket(asi);
            asi = null;
        }
        else if (_skillType == 2)
        {
            int requiredRep = 0;
            int itemId = 0;
            L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(activeChar);

            for (L2PledgeSkillLearn s : skills)
            {
                if (s.getId() == _id && s.getLevel() == _level)
                {
                    canteach = true;
                    requiredRep = s.getRepCost();
                    itemId = s.getItemId();
                    break;
                }
            }

            if (!canteach)
            {
            	sendPacket(ActionFailed.STATIC_PACKET);
                return; // cheater :)
            }

            AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
            if (Config.ALT_LIFE_CRYSTAL_NEEDED)
                asi.addRequirement(1, itemId, 1, 0);
            sendPacket(asi);
            asi = null;
        }
        else // Common Skills
        {
            int costid = 0;
            int costcount = 0;
            int spcost = 0;

            L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableFishingSkills(activeChar);

            for (L2SkillLearn s : skillsc)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if (sk == null || sk != skill)
                    continue;

                canteach = true;
                costid = s.getIdCost();
                costcount = s.getCostCount();
                spcost = s.getSpCost();
            }

			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
            asi.addRequirement(4, costid, costcount, 0);
            sendPacket(asi);
            asi = null;
        }
        sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public String getType()
    {
        return _C__6B_REQUESTAQUIRESKILLINFO;
    }
}
