package se.danielmartensson.threads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import se.danielmartensson.Main;
import se.danielmartensson.controller.MainController;
import se.danielmartensson.tools.FileHandeling;

public class TrainingThread extends Thread {

	private AtomicBoolean runTraining;
	
	// From the GUIs
	private File selectedWeightFile;
	private File selectedDataFile;
	private File selectedDarknetFile;
	private File selectedConfigFile;
	private Button selectWeightFileButton;
	private Button selectDataFileButton;
	private Button selectDarknetFileButton;
	private Button selectConfigFileButton;
    private TextArea terminalTextArea;
    private Button startTrainingButton;
    private Button stopTrainingButton;
	private CheckBox meanAveragePrecisionsCheckBox;

	public TrainingThread(AtomicBoolean runTraining) {
		this.runTraining = runTraining;
	}

	@Override
	public void run() {
		while (Main.RUNTHREAD.get()) {
			if(runTraining.get()) {
				Platform.runLater(() -> {
					clearTerminal();
					disableComponents();
					callDarknetFromJava();
				});
			}
			threadSleep();
		}
	}

	private void callDarknetFromJava() {
		// Get file names
		String darknetFileName = selectedDarknetFile.getName();
		String dataFileName = selectedDataFile.getName();
		String configFileName = selectedConfigFile.getName();
		String weightFileName = selectedWeightFile.getName();
		
		// Get parent folder path
		String darknetFolderPath = selectedDarknetFile.getAbsoluteFile().getParent();
		
		// Get folder names
		String dataFolderName = selectedDataFile.getParentFile().getName();
		String configFolderName = selectedConfigFile.getParentFile().getName();
		String weightFolderName = selectedWeightFile.getParentFile().getName();
	
		// Process builder
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(new File(darknetFolderPath));
		if(FileHandeling.operativeSystem.equals("Windows")) {
			if(meanAveragePrecisionsCheckBox.isSelected()) {
				processBuilder.command(darknetFileName, "detector", "train", dataFolderName + "\\" + dataFileName, configFolderName + "\\" + configFileName, weightFolderName + "\\" + weightFileName, "-dont_show", "-mep"); 
			}else {
				processBuilder.command(darknetFileName, "detector", "train", dataFolderName + "\\" + dataFileName, configFolderName + "\\" + configFileName, weightFolderName + "\\" + weightFileName, "-dont_show"); 
			}
		}else {
			if(meanAveragePrecisionsCheckBox.isSelected()) {
				processBuilder.command("./" + darknetFileName, "detector", "train", dataFolderName + "/" + dataFileName, configFolderName + "/" + configFileName, weightFolderName + "/" + weightFileName, "-dont_show", "-mep"); 
			}else {
				processBuilder.command("./" + darknetFileName, "detector", "train", dataFolderName + "/" + dataFileName, configFolderName + "/" + configFileName, weightFolderName + "/" + weightFileName, "-dont_show"); 
			}
		}
		processBuilder.redirectErrorStream(true); // Important
		try {
			// Write to the terminal to show how we are calling Darknet
			String commandLine = processBuilder.command().stream().map(n -> String.valueOf(n)).collect(Collectors.joining(" "));
			writeToTerminal(commandLine);
			
			// Start the processes and begin to read
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String newLine;
			while ((newLine = reader.readLine()) != null) {
				// Read
				writeToTerminal(newLine);
				
				// If we want to break the process
				if(!runTraining.get())
					process.destroy();
			}
			process.waitFor(); 
		} catch (IOException | InterruptedException e) {
			writeToTerminal(e.getMessage());
		}
		enableComponents();
		runTraining.set(false);
	}

	
	private void threadSleep() {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void enableComponents() {
		selectWeightFileButton.setDisable(false);
		selectDataFileButton.setDisable(false);
		selectDarknetFileButton.setDisable(false);
		selectConfigFileButton.setDisable(false);
	    startTrainingButton.setDisable(false);
	    stopTrainingButton.setDisable(true); // No access to the stop button
	    meanAveragePrecisionsCheckBox.setDisable(false);
	}
	
	private void disableComponents() {
		selectWeightFileButton.setDisable(true);
		selectDataFileButton.setDisable(true);
		selectDarknetFileButton.setDisable(true);
		selectConfigFileButton.setDisable(true);
	    startTrainingButton.setDisable(true);
	    stopTrainingButton.setDisable(false); // We want to have access to the stop button
	    meanAveragePrecisionsCheckBox.setDisable(true);
	}

	private void writeToTerminal(String newLine) {
		String currentText = terminalTextArea.getText();
		String[] currentLines = currentText.split("\n");
		System.out.println(currentLines.length);
		if(currentLines.length > 200) {
			// Remove the first line and add the last line
			String newText = "";
			for(int i = 1; i < currentLines.length; i++) 
				newText += currentLines[i] + "\n";
			newText += newLine;
			terminalTextArea.setText(newText);
		}else {
			// Just add
			String newText = currentText + "\n" + newLine;
			terminalTextArea.setText(newText);
		}	
		
	}

	private void clearTerminal() {
		terminalTextArea.setText("");		
	}

	public void setComponents(MainController mainController) {
		selectedWeightFile = mainController.getSelectedWeightFile();
		selectedDataFile = mainController.getSelectedWeightFile();
		selectedDarknetFile = mainController.getSelectedDarknetFile();
		selectedConfigFile = mainController.getSelectedConfigFile();
		selectWeightFileButton = mainController.getSelectWeightFileButton();
		selectDataFileButton = mainController.getSelectDataFileButton();
		selectDarknetFileButton = mainController.getSelectDarknetFileButton();
		selectConfigFileButton = mainController.getSelectConfigFileButton();
		terminalTextArea = mainController.getTerminalTextArea();
		startTrainingButton = mainController.getStartTrainingButton();
		stopTrainingButton = mainController.getStopTrainingButton();
		meanAveragePrecisionsCheckBox = mainController.getMeanAveragePrecisionsCheckBox();
	}

}
