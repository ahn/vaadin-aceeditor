package org.vaadin.aceeditor.client;

import com.vaadin.shared.communication.ClientRpc;

public interface AceEditorClientRpc extends ClientRpc {
	public void diff(TransportDiff diff);

	public void changedOnServer();

	public void scrollToRow(int row);
}
