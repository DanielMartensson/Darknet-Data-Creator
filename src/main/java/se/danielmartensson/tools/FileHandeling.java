package se.danielmartensson.tools;

import java.io.File;

import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FileHandeling {

	public static String operativeSystem;

	public FileHandeling() {
		String osName = System.getProperty("os.name");
		if (osName.contains("Windows")) {
			operativeSystem = "Windows";
		} else {
			operativeSystem = "Linux";
		}
	}

	public File selectFolder(Text selectFolderLabel) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Where do you want to save your data?");
		if (operativeSystem.equals("Windows"))
			directoryChooser.setInitialDirectory(new File("C:\\"));
		else
			directoryChooser.setInitialDirectory(new File("/"));
		File selectedFolder = directoryChooser.showDialog(new Stage());
		if (selectedFolder != null)
			selectFolderLabel.setText("Path: " + selectedFolder.getAbsolutePath());
		else
			selectFolderLabel.setText("Path: No path selected");
		return selectedFolder;
	}

	public File selectFile(Text selectFileLabel, String fileName, String[] extensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("What file do you want to select?");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(fileName, extensions));
		if (operativeSystem.equals("Windows"))
			fileChooser.setInitialDirectory(new File("C:\\"));
		else
			fileChooser.setInitialDirectory(new File("/"));
		File selectedFile = fileChooser.showOpenDialog(new Stage());
		if (selectedFile != null)
			selectFileLabel.setText("Path: " + selectedFile.getAbsolutePath());
		else
			selectFileLabel.setText("Path: No path selected");
		return selectedFile;
	}

}
