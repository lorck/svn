package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save. 
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 * 
 * @author Ahmed
 *
 */
public class DivineHealer extends L2DefaultTransformation
{
	public DivineHealer()
	{
		// id, colRadius, colHeight
		super(255, 8.0, 27.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Divine Healer Clans
		addSkill(player, 702, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Divine Healer Clans
		removeSkill(player, 702);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineHealer());
	}
}