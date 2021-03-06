package elayne.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author polbat02
 */
public class ChangeCustomValueDialog extends Dialog
{
	private Text _accountId;
	private boolean _changeConfirmed;
	private Button _confirmChange;
	private String _newValue;
	private String _value;

	public ChangeCustomValueDialog(Shell parentShell, String value)
	{
		super(parentShell);
		_value = value;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText("Change " + _value);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "&Change Name", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label confirmText = new Label(composite, SWT.NONE);
		confirmText.setText("Which " + _value + " would you like to give to this Character?");
		confirmText.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&New " + _value + ":");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));

		_accountId = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);

		gridData.widthHint = convertHeightInCharsToPixels(20);
		_accountId.setLayoutData(gridData);

		_confirmChange = new Button(composite, SWT.CHECK);
		_confirmChange.setText("Are you sure you want to change this player's " + _value + "?");
		_confirmChange.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));

		return composite;
	}

	public boolean getChangeConfirmmed()
	{
		return _changeConfirmed;
	}

	public String getNewValue()
	{
		return _newValue;
	}

	@Override
	protected void okPressed()
	{
		setNewValue();
		setChangeConfirmed();
		super.okPressed();
	}

	private void setChangeConfirmed()
	{
		_changeConfirmed = _confirmChange.getSelection();
	}

	private void setNewValue()
	{
		_newValue = _accountId.getText();
	}
}
