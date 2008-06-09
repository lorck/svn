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
 * @author durgus
 *
 */
public class DragonBomberWeak extends L2DefaultTransformation
{
	public DragonBomberWeak()
	{
		// id, colRadius, colHeight
		super(218, 8.0, 22.0);
	}

	public void transformedSkills(L2PcInstance player)
	{
		// Death Blow
		addSkill(player, 580, 2);
		// Sand Cloud
		addSkill(player, 581, 2);
		// Scope Bleed
		addSkill(player, 582, 2);
		// Assimilation
		addSkill(player, 583, 2);
	}

	public void removeSkills(L2PcInstance player)
	{
		// Death Blow
		removeSkill(player, 580);
		// Sand Cloud
		removeSkill(player, 581);
		// Scope Bleed
		removeSkill(player, 582);
		// Assimilation
		removeSkill(player, 583);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DragonBomberWeak());
	}
}
