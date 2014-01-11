/**
 * Copyright 2012-2014 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.visualvm.remotevm;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import com.sun.tools.visualvm.core.properties.PropertiesCustomizer;
import com.sun.tools.visualvm.core.properties.PropertiesSupport;
import com.sun.tools.visualvm.core.ui.components.ScrollableContainer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SshHostCustomizer extends JPanel {

	private static final long serialVersionUID = 20140112L;
	
	private static Dimension MIN_PROPERTIES_SIZE = new Dimension(400, 200);
	private static Dimension MAX_PROPERTIES_SIZE = new Dimension(700, 400);

	private boolean internalChange = false;

	public static SshHostProperties defineHost() {
		SshHostCustomizer hc = getInstance();
		hc.setup();

		ScrollableContainer sc = new ScrollableContainer(hc,
				ScrollableContainer.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollableContainer.HORIZONTAL_SCROLLBAR_NEVER);
		sc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sc.setViewportBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final DialogDescriptor dd = new DialogDescriptor(sc,
				NbBundle.getMessage(SshHostCustomizer.class,
						"Title_Add_Remote_Host"), true,
				new Object[] { // NOI18N
				hc.okButton, DialogDescriptor.CANCEL_OPTION }, hc.okButton, 0,
				null, null);
		dd.setAdditionalOptions(new Object[] { hc.settingsButton });
		final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
		d.pack();
		d.setVisible(true);

		if (dd.getValue() == hc.okButton) {
			SshHostProperties hp = new SshHostProperties(hc.getHostName(),
					hc.getDisplayName(), hc.getPropertiesCustomizer());
			hc.accepted();
			return hp;
		} else {
			hc.cancelled();
			return null;
		}
	}

	private static SshHostCustomizer instance;

	private SshHostCustomizer() {
		initComponents();
		update();
	}

	private static SshHostCustomizer getInstance() {
		if (instance == null)
			instance = new SshHostCustomizer();
		return instance;
	}

	private String getHostName() {
		return hostnameField.getText().trim();
	}

	private String getDisplayName() {
		return displaynameField.getText().trim();
	}

	private PropertiesCustomizer getPropertiesCustomizer() {
		return settingsPanel;
	}

	private void setup() {
		hostnameField.setEnabled(true);
		displaynameCheckbox.setSelected(false);
		displaynameCheckbox.setEnabled(true);
		hostnameField.setText(""); // NOI18N
		displaynameField.setText(""); // NOI18N

		PropertiesSupport support = PropertiesSupport.sharedInstance();
		settingsPanel = !support.hasProperties(SshHost.class) ? null : support
				.getCustomizer(SshHost.class);

		if (settingsPanel != null) {
			settingsPanel.addChangeListener(listener);
		}
		settingsButton.setVisible(settingsPanel != null);
		if (!settingsButton.isVisible()) {
			settingsButton.setSelected(false);
		} else {
			settingsButton.setSelected(!settingsPanel.settingsValid());
		}

		updateSettings();
	}

	private void accepted() {
		cleanup();
	}

	private void cancelled() {
		if (settingsPanel != null)
			settingsPanel.propertiesCancelled();
		cleanup();
	}

	private void cleanup() {
		if (settingsPanel != null)
			settingsPanel.removeChangeListener(listener);
		settingsContainer.removeAll();
		settingsPanel = null;
	}

	private void update() {
		if (internalChange) {
			return;
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String hostname = getHostName();

					if (!displaynameCheckbox.isSelected()) {
						internalChange = true;
						displaynameField.setText(hostname);
						internalChange = false;
					}

					String displayname = getDisplayName();
					displaynameField.setEnabled(displaynameCheckbox
							.isSelected());

					boolean hostValid = hostname.length() > 0
							&& displayname.length() > 0;
					boolean settingsValid = settingsPanel == null ? true
							: settingsPanel.settingsValid();

					okButton.setEnabled(hostValid && settingsValid);
				}
			});
		}
	}

	private void updateSettings() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (settingsButton.isSelected()) {
					settingsContainer.add(settingsPanel, BorderLayout.CENTER);
					settingsContainer.setBorder(BorderFactory
							.createEmptyBorder(10, 0, 0, 0));

					Dimension prefSize = settingsPanel.getPreferredSize();
					prefSize.height = Math.max(prefSize.height,
							MIN_PROPERTIES_SIZE.height);
					prefSize.height = Math.min(prefSize.height,
							MAX_PROPERTIES_SIZE.height);
					settingsPanel.setPreferredSize(prefSize);

				} else {
					settingsContainer.removeAll();
					settingsContainer.setBorder(BorderFactory
							.createEmptyBorder());
				}
				Window w = SwingUtilities
						.getWindowAncestor(SshHostCustomizer.this);
				if (w != null)
					w.pack();
				update();
			}
		});
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		GridBagConstraints constraints;

		// hostnameLabel
		hostnameLabel = new JLabel();
		Mnemonics.setLocalizedText(hostnameLabel,
				NbBundle.getMessage(SshHostCustomizer.class, "LBL_Host_name")); // NOI18N
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 0, 0, 0);
		add(hostnameLabel, constraints);

		// hostnameField
		hostnameField = new JTextField();
		hostnameLabel.setLabelFor(hostnameField);
		hostnameField.setPreferredSize(new Dimension(250, hostnameField
				.getPreferredSize().height));
		hostnameField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void changedUpdate(DocumentEvent e) {
				update();
			}
		});
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 5, 0, 0);
		add(hostnameField, constraints);

		// displaynameCheckbox
		displaynameCheckbox = new JCheckBox();
		Mnemonics.setLocalizedText(displaynameCheckbox, NbBundle.getMessage(
				SshHostCustomizer.class, "LBL_Display_name")); // NOI18N
		displaynameCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			};
		});
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(8, 0, 5, 0);
		add(displaynameCheckbox, constraints);

		// displaynameField
		displaynameField = new JTextField();
		displaynameField.setPreferredSize(new Dimension(250, displaynameField
				.getPreferredSize().height));
		displaynameField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void insertUpdate(DocumentEvent e) {
						update();
					}

					public void removeUpdate(DocumentEvent e) {
						update();
					}

					public void changedUpdate(DocumentEvent e) {
						update();
					}
				});
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(8, 5, 5, 0);
		add(displaynameField, constraints);

		// spacer
		settingsContainer = new JPanel(new BorderLayout(0, 0));
		settingsContainer.setOpaque(false);
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(0, 0, 0, 0);
		add(settingsContainer, constraints);

		// okButton
		okButton = new JButton();
		Mnemonics.setLocalizedText(okButton,
				NbBundle.getMessage(SshHostCustomizer.class, "LBL_OK")); // NOI18N

		settingsButton = new JToggleButton() {
			protected void fireActionPerformed(ActionEvent e) {
				updateSettings();
			}
		};
		Mnemonics.setLocalizedText(settingsButton, NbBundle.getMessage(
				SshHostCustomizer.class, "BTN_AdavancedSettings")); // NOI18N

		// UI tweaks
		displaynameCheckbox.setBorder(hostnameLabel.getBorder());
	}

	private JLabel hostnameLabel;
	private JTextField hostnameField;
	private JCheckBox displaynameCheckbox;
	private JTextField displaynameField;
	private JPanel settingsContainer;

	@SuppressWarnings("rawtypes")
	private PropertiesCustomizer settingsPanel;

	private JButton okButton;
	private JToggleButton settingsButton;

	private final ChangeListener listener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			update();
		}
	};
}
