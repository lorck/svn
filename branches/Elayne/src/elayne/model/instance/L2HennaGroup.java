package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;

public class L2HennaGroup extends L2GroupEntry
{
	public L2HennaGroup(L2PcInstance parent, String name)
	{
		super(parent, name);
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.HENNA_CON);
	}

	@Override
	public L2PcInstance getParent()
	{
		return (L2PcInstance) _parent;
	}
}
