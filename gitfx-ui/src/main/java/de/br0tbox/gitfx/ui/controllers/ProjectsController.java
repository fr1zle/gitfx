package de.br0tbox.gitfx.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.events.ListenerHandle;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.cathive.fx.guice.FXMLController;

import de.br0tbox.gitfx.core.model.GitFxProject;
import de.br0tbox.gitfx.core.services.IPropertyService;
import de.br0tbox.gitfx.ui.uimodel.ProjectModel;

/**
 * The controller for the projects view.
 * @author fr1zle
 *
 */
@FXMLController(controllerId = "/ProjectView.fxml")
public class ProjectsController extends AbstractController {

	private static final String LASTOPEN_PROPERTY = "projectcontroller.lastopened";

	private static final Logger LOGGER = LogManager.getLogger(ProjectsController.class);

	@Inject
	private IPropertyService propertyService;

	@FXML
	private Button openButton;

	private File lastOpened = null;

	@FXML
	private ListView projectList;

	@FXML
	Button cloneButton;

	@Override
	public void onInit() {
		System.out.println("init");
		final String lastOpenProperty = propertyService.getStringProperty(LASTOPEN_PROPERTY);
		if (lastOpenProperty != null) {
			lastOpened = new File(lastOpenProperty);
		}

		projectList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				final ProjectModel model = (ProjectModel) projectList.getItems().get(0);
				model.setChanges(10);
			}
		});
		projectList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {
				if (MouseButton.PRIMARY.equals(mouseEvent.getButton())) {
					if (mouseEvent.getClickCount() == 2) {
						// TODO: Open the Project in Question
					}
				}
			}
		});
		openButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				final DirectoryChooser chooser = new DirectoryChooser();
				if (lastOpened != null) {
					chooser.setInitialDirectory(lastOpened);
				}
				File file = chooser.showDialog(getStage());
				if (file != null) {
					lastOpened = file;
					propertyService.saveProperty(LASTOPEN_PROPERTY, file.getAbsolutePath());
					System.out.println(file.getAbsolutePath());
					if (!file.getAbsolutePath().endsWith(".git")) {
						file = new File(file, ".git");
					}
					final FileRepositoryBuilder builder = new FileRepositoryBuilder();
					Repository repository;
					try {
						repository = builder.setGitDir(file).readEnvironment().findGitDir().build();
						final Git git = new Git(repository);
						final GitFxProject gitFxProject = new GitFxProject(git);
						final Set<String> allUncommitedChanges = gitFxProject.getAllUncommitedChanges();
						for (final String uncommitedChange : allUncommitedChanges) {
							System.out.println(uncommitedChange);
						}
						final ListenerHandle addIndexChangedListener = repository.getListenerList().addIndexChangedListener(new IndexChangedListener() {

							@Override
							public void onIndexChanged(IndexChangedEvent event) {
								System.out.println("Bla");
								System.out.println(event);
							}
						});
						startTimer(repository);
						final ProjectModel projectModel = new ProjectModel();
						projectModel.setProjectName(new File(file.getParent()).getName());
						projectModel.setCurrentBranch(gitFxProject.getGit().getRepository().getBranch());
						projectModel.setChanges(gitFxProject.getUncommitedChangesNumber());
						projectList.getItems().add(projectModel);
					} catch (final IOException e) {
						LOGGER.error(e);
					}

				}
			}

		});
	}

	private void startTimer(final Repository repository) {
		final TimerTask task = new TimerTask() {

			@Override
			public void run() {
//				try {
//					final DirCache cache = DirCache.read(repository);
//					final DirCacheBuilder builder = cache.builder();
//					for (int i = 0; i < cache.getEntryCount(); i++) {
//						final DirCacheEntry entry = cache.getEntry(i);
//						System.out.println(entry.getPathString() + " " + entry.getStage());
//					}
//					cache.unlock();
//				} catch (final IOException e) {
//					e.printStackTrace();
//				}
			}
		};
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, 4000);
	}
}
