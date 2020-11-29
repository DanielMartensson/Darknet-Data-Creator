package se.danielmartensson.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import se.danielmartensson.Main;
import se.danielmartensson.tools.observablelists.Resolutions;
import se.danielmartensson.tools.observablelists.Webcams;

public class CameraThread extends Thread {

	private AtomicBoolean runCamera;
	private AtomicBoolean runRecording;
	private boolean hasOpen = false;
	private boolean boundedBoxIsApplied = false;

	// From the GUI
	private Webcam webcam;
	private int sampleTime;
	private Double resolutionHeight;
	private Double resolutionWidht;
	private int classNumber;
	private File selectedSaveToFolderDirectory;
	private AtomicInteger boundedBoxWidth;
	private AtomicInteger boundedBoxHeight;
	private ImageView cameraImageView;
	private BufferedImage cutNoBound;

	public CameraThread(AtomicBoolean runCamera, AtomicBoolean runRecording) {
		this.runCamera = runCamera;
		this.runRecording = runRecording;
	}

	@Override
	public void run() {
		while (Main.RUNTHREAD.get()) {
			while (runCamera.get()) {
				Platform.runLater(() -> {
					openCamera();
					BufferedImage image = getImage();
					BufferedImage mirror = mirrorImage(image);
					BufferedImage cut = cutImageWithBoundedBox(mirror);
					File savedImage = saveImage(cut);
					showSavedImage(savedImage);
					if (runRecording.get() && boundedBoxIsApplied) {
						File[] classes = checkClassFolderAndClassPathsFileStatus();
						copySavedImageToClassFolder(classes);
					}
				});
				threadSleep();
			}
			closeCamera();
			threadSleep();
		}
		closeCamera();
	}

	private void copySavedImageToClassFolder(File[] classes) {
		// Before we proceed, we need to find at what index we are at
		int classFileName = classes.length + 1;
		
		// Write these class files now
		File classPathFile = null;
		File classImage = null;
		File classImageLabel = null;
		if(FileHandeling.operativeSystem.contains("Windows")) {
			classPathFile = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "\\ClassPaths.txt");
			classImage = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "\\" + classNumber + "\\" + classFileName + ".png");
			classImageLabel = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "\\" + classNumber + "\\" + classFileName + ".txt");
		}else {
			classPathFile = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "/ClassPaths.txt");
			classImage = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "/" + classNumber + "/" + classFileName + ".png");
			classImageLabel = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "/" + classNumber + "/" + classFileName + ".txt");
		}
		
		// Create files that don't exist
		try {
			classImage.createNewFile();
			classImageLabel.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Moving the saved image to the class folder and rename it
		try {
			ImageIO.write(cutNoBound, "png", classImage);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Get the relative size for the class label. Notice that x, y is center of the rectangle
		float cw = cutNoBound.getWidth();
		float ch = cutNoBound.getHeight();
		float bw = boundedBoxWidth.floatValue();
		float bh = boundedBoxHeight.floatValue();
		float x = (cw/2)/cw; // Always 0.5
		float y = (ch/2)/ch; // Same here
		float width = bw/cw; 
		float height = bh/ch;

		// Write the label and the path
		try {
			BufferedWriter labelRow = new BufferedWriter(new FileWriter(classImageLabel));
			labelRow.write(classNumber + " " + x + " " + y  + " " + width + " " + height); // <object-class> <x> <y> <width> <height>
			labelRow.close();
			BufferedWriter pathRow = new BufferedWriter(new FileWriter(classPathFile, true)); // Append
			pathRow.write(classImage.getAbsolutePath() + "\n");
			pathRow.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File[] checkClassFolderAndClassPathsFileStatus() {
		// Check if we have created folder and file
		File classFolder = null;
		File classPathFile = null;
		if(FileHandeling.operativeSystem.contains("Windows")) {
			classFolder = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "\\" + classNumber);
			classPathFile = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "\\ClassPaths.txt");
		}else {
			classFolder = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "/" + classNumber);
			classPathFile = new File(selectedSaveToFolderDirectory.getAbsolutePath() + "/ClassPaths.txt");
		}
		if(!classFolder.exists())
			classFolder.mkdirs();
		if(!classPathFile.exists()) {
			try {
				classPathFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return classFolder.listFiles((dir, name) -> name.endsWith(".png")); // Get all the files and its names
	}

	private void closeCamera() {
		if (hasOpen) {
			webcam.close();
			hasOpen = false;
		}
	}

	private void openCamera() {
		if (!hasOpen) {
			webcam.open();
			hasOpen = true;
		}
	}

	private void threadSleep() {
		try {
			Thread.sleep(sampleTime * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showSavedImage(File savedImage) {
		Image show = new Image(savedImage.toURI().toString());
		cameraImageView.setImage(show);
	}

	private File saveImage(BufferedImage cut) {
		try {
			String cameraImagePath = "";
			if(FileHandeling.operativeSystem.contains("Windows")) {
				cameraImagePath = selectedSaveToFolderDirectory.getAbsolutePath() + "\\camera.png";
			}else {
				cameraImagePath = selectedSaveToFolderDirectory.getAbsolutePath() + "/camera.png";
			}
			File savedImage = new File(cameraImagePath);
			ImageIO.write(cut, "png", savedImage);
			return savedImage;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage cutImageWithBoundedBox(BufferedImage mirror) {
		int widthCamera = mirror.getWidth();
		int heightCamera = mirror.getHeight();
		int widthResolution = resolutionWidht.intValue();
		int heightResolution = resolutionHeight.intValue();
		int x = widthCamera/2 - widthResolution/2;
		int y = heightCamera/2 - heightResolution/2;
		if(x <= 0 || y <= 0) {
			boundedBoxIsApplied = false;
			return mirror; // No bounded box - None square
		}else {
			BufferedImage cut = mirror.getSubimage(x, y, widthResolution, heightResolution); // All sub images are square
			cutNoBound = copyImage(cut);
			createBoundedBox(cut);
			return cut;
		}
	}
	
	public BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}

	private void createBoundedBox(BufferedImage cut) {
		int cw = cut.getWidth();
		int ch = cut.getHeight();
		int bw = boundedBoxWidth.intValue();
		int bh = boundedBoxHeight.intValue();
		int rgb = 0xFFFF0000; // Full red with opacity 1
		
		// Need to be sure that we are not over the limits
		if(bw > cw || bh > ch) {
			boundedBoxIsApplied = false;
			return;
		}
		
		// Create the square now
		for(int x = 0 ; x < cw; x++) {
			for(int y = 0; y < ch; y++) {
				// Horizontal line
				if(x >= cw/2 - bw/2 && x < cw/2 + bw/2 && y == ch/2 - bh/2) {
					cut.setRGB(x, y, rgb);		
					cut.setRGB(x, y + bh - 1, rgb); // Mirror
				}
				// Vertical line
				if(y >= ch/2 - bh/2 && y < ch/2 + bh/2 && x == cw/2 - bw/2) {
					cut.setRGB(x, y, rgb);		
					cut.setRGB(x + bw - 1, y, rgb); // Mirror
				}
			}
		}
		boundedBoxIsApplied = true;
	}

	private BufferedImage mirrorImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int lx = 0, rx = width - 1; lx < width; lx++, rx--) {
				int p = image.getRGB(lx, y);
				mirror.setRGB(rx, y, p);
			}
		}
		return mirror;
	}

	private BufferedImage getImage() {
		return webcam.getImage();
	}

	public void setComponents(Webcam webcam, ChoiceBox<Integer> sampleIntervallDropdownButton, ChoiceBox<Resolutions> pictureResolutionDropdownButton, ChoiceBox<Integer> classNumberDropdownButton, File selectedSaveToFolderDirectory, AtomicInteger boundedBoxWidth, AtomicInteger boundedBoxHeight, ImageView cameraImageView) {
		this.webcam = webcam;
		sampleTime = sampleIntervallDropdownButton.getSelectionModel().getSelectedItem().intValue();
		resolutionHeight = pictureResolutionDropdownButton.getSelectionModel().getSelectedItem().getHeight();
		resolutionWidht = pictureResolutionDropdownButton.getSelectionModel().getSelectedItem().getWidth();
		classNumber = classNumberDropdownButton.getSelectionModel().getSelectedItem().intValue();
		this.selectedSaveToFolderDirectory = selectedSaveToFolderDirectory;
		this.boundedBoxWidth = boundedBoxWidth;
		this.boundedBoxHeight = boundedBoxHeight;
		this.cameraImageView = cameraImageView;
	}

}
