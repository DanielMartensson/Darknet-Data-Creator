package se.danielmartensson.tools;

import java.io.File;

import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FileHandeling {
	
	private String operativeSystem;

	public FileHandeling() {
		String osName = System.getProperty("os.name");
		if(osName.contains("Windows")) {
			operativeSystem = "Windows";
		}else {
			operativeSystem = "Linux";
		}
	}

	public File selectSaveToFolder(Text saveToFolderLabel) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Where do you want to save your data?");
		if(operativeSystem.equals("Windows"))
			directoryChooser.setInitialDirectory(new File("C:/"));
		else
			directoryChooser.setInitialDirectory(new File("/"));
		File selectedSaveToFolderDirectory = directoryChooser.showDialog(new Stage());
		if(selectedSaveToFolderDirectory != null)
			saveToFolderLabel.setText("Path: " +selectedSaveToFolderDirectory.getAbsolutePath());
		else
			saveToFolderLabel.setText("Path: No path selected");
		return selectedSaveToFolderDirectory;
	}

}
