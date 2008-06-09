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
public class Benom extends L2DefaultTransformation
{
	public Benom()
	{
		// id, colRadius, colHeight
		super(307, 10.0, 57.5);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Venom Power Smash
		addSkill(player, 725, 2);
		// Venom Sonic Storm
		addSkill(player, 726, 2);
		// Venom Disillusion
		addSkill(player, 727, 1);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Venom Power Smash
		removeSkill(player, 725);
		// Venom Sonic Storm
		removeSkill(player, 726);
		// Venom Disillusion
		removeSkill(player, 727);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Benom());
	}
}