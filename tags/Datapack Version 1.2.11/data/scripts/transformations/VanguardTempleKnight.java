package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class VanguardTempleKnight extends L2Transformation
{
	public VanguardTempleKnight()
	{
		// id, colRadius, colHeight
		super(314, 7.0, 24.0);
	}

	public void onTransform(L2PcInstance player)
	{
		// Update transformation ID into database and player instance variables.
		player.transformInsertInfo();
		if (player.transformId() > 0 && !player.isCursedWeaponEquipped())
		{
			// Disable all character skills.
			for (L2Skill sk : player.getAllSkills())
			{
				if (sk != null && !sk.isPassive())
				{
					switch (sk.getId())
					{
						case 28:  // Aggression
						case 18:  // Aura of Hate
						case 10:  // Summon Storm Cubic
						case 67:  // Summon Life Cubic
						case 449: // Summon Attractive Cubic
						case 400: // Tribunal
						case 197: // Holy Armor
						{
							// Those Skills wont be removed.
							break;
						}
						default:
						{
							player.removeSkill(sk, false);
							break;
						}
					}
				}
			}
			// give transformation skills
			transformedSkills(player);
			return;
		}
	}

	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 814, level); // Full Swing
			addSkill(player, 816, level); // Power Divide
			addSkill(player, 838, 1); // Switch Stance
			// Send a Server->Client packet StatusUpdate to the L2PcInstance.
			player.sendSkillList();
		}
	}

	public void onUntransform(L2PcInstance player)
	{
		// remove transformation skills
		removeSkills(player);
	}

	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 814); // Full Swing
		removeSkill(player, 816); // Power Divide
		removeSkill(player, 838); // Switch Stance
		// Send a Server->Client packet StatusUpdate to the L2PcInstance.
		player.sendSkillList();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardTempleKnight());
	}
}