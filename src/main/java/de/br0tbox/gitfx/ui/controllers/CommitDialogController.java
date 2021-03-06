/*
 * Copyright 2013 Timm Hirsens
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
package de.br0tbox.gitfx.ui.controllers;

import static de.br0tbox.gitfx.ui.util.Preconditions.checkNotNull;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.events.IndexChangedEvent;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

@FXMLController(controllerId = "/CommitDialogView.fxml")
public class CommitDialogController extends AbstractController {

	private ProjectModel projectModel;
	@FXML
	private ListView<String> unstagedFiles;
	@FXML
	private ListView<String> stagedFiles;
	@FXML
	private TextArea commitMessage;

	@FXML
	public void stageSelected() {
		try {
			if (unstagedFiles.getSelectionModel().getSelectedItems().size() < 1) {
				return;
			}
			final AddCommand add = projectModel.getFxProject().getGit().add();
			add.setUpdate(true);
			final List<String> selectedItems = unstagedFiles.getSelectionModel().getSelectedItems();
			for (final String unstagedFile : selectedItems) {
				add.addFilepattern(unstagedFile);
			}
			add.call();
		} catch (final GitAPIException e) {
			Dialogs.create().owner(getStage()).message("Error while Staging").masthead("An error occured").title("Error").showException(e);
		}
	}

	@FXML
	public void stageAll() {
		try {
			final AddCommand add = projectModel.getFxProject().getGit().add();
			add.addFilepattern(".");
			add.call();
			final AddCommand addUpdate = projectModel.getFxProject().getGit().add();
			addUpdate.addFilepattern(".");
			addUpdate.call();
		} catch (final GitAPIException e) {
			Dialogs.create().owner(getStage()).message("Error while Staging").masthead("An error occured").title("Error").showException(e);
		}
	}

	@FXML
	public void commit() {
		try {
			final CommitCommand commit = projectModel.getFxProject().getGit().commit();
			final ObservableList<String> unstagedChanges = projectModel.getStagedChangesProperty().get();
			if (unstagedChanges.size() < 1) {
				final Action commitAll = Dialogs.create().owner(getStage()).message("There are no changes staged, do you want to commit every changed File?").showConfirm();
				if (Dialog.Actions.YES.equals(commitAll)) {
					//FIXME: Threading issue, this is a workaround that seems to work for now.
					stageAll();
					projectModel.getFxProject().getGit().getRepository().getListenerList().dispatch(new IndexChangedEvent());
					Platform.runLater(this::commit);
				}
			} else {
				commit.setMessage(commitMessage.getText());
				commit.call();
				this.hide();
			}
		} catch (final GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onInit() {
		checkNotNull(projectModel, "projectModel");
		unstagedFiles.itemsProperty().bind(projectModel.getUnstagedChangesProperty());
		stagedFiles.itemsProperty().bind(projectModel.getStagedChangesProperty());
	}

	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public void setProjectModel(ProjectModel projectModel) {
		this.projectModel = projectModel;
	}

	public void hide() {
		getStage().close();
	}

}
