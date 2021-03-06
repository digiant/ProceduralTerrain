/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.terrain.vegetation;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.examples.progressbar.ProgressbarControl;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.Random;
import org.shaman.terrain.Biome;

/**
 *
 * @author Sebastian Weiss
 */
public class VegetationScreenController implements ScreenController {
	private final VegetationGenerator generator;
	private final Random rand = new Random();
	
	private Nifty nifty;
	private Screen screen;
	private Slider brushSizeSlider;
	private Element biomeImage;
	private Label selectedBiomeLabel;
	private CheckBox texturedCheckBox;
	private Button distortBiomesButton;
	private Button smoothBiomesButton;
	private Slider plantSizeSlider;
	private CheckBox grassCheckBox;
	private CheckBox treesCheckBox;
	private CheckBox treesHighResCheckBox;
	
	private Button recordAddButton;
	private CheckBox recordEditCheckBox;
	private Button recordDeleteButton;
	private Button recordDeleteAllButton;
	private Button recordPlayButton;
	private Button recordRecordButton;

	public VegetationScreenController(VegetationGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void bind(Nifty nifty, Screen screen) {
		this.nifty = nifty;
		this.screen = screen;
	}

	@Override
	public void onStartScreen() {
		brushSizeSlider = screen.findNiftyControl("BrushSizeSlider", Slider.class);
		biomeImage = screen.findElementById("BiomeImage");
		selectedBiomeLabel = screen.findNiftyControl("SelectedBiomeLabel", Label.class);
		texturedCheckBox = screen.findNiftyControl("TexturedCheckBox", CheckBox.class);
		distortBiomesButton = screen.findNiftyControl("DistortBiomesButton", Button.class);
		smoothBiomesButton = screen.findNiftyControl("SmoothBiomesButton", Button.class);
		plantSizeSlider = screen.findNiftyControl("PlantSizeSlider", Slider.class);
		grassCheckBox = screen.findNiftyControl("GrassCheckBox", CheckBox.class);
		treesCheckBox = screen.findNiftyControl("TreeCheckBox", CheckBox.class);
		treesHighResCheckBox = screen.findNiftyControl("TreeHighResCheckBox", CheckBox.class);
		recordAddButton = screen.findNiftyControl("RecordAddButton", Button.class);
		recordEditCheckBox = screen.findNiftyControl("RecordEditCheckBox", CheckBox.class);
		recordDeleteButton = screen.findNiftyControl("RecordDeleteButton", Button.class);
		recordDeleteAllButton = screen.findNiftyControl("RecordDeleteAllButton", Button.class);
		recordPlayButton = screen.findNiftyControl("RecordPlayButton", Button.class);
		recordRecordButton = screen.findNiftyControl("RecordRecordButton", Button.class);

		generator.guiBrushSizeChanged(brushSizeSlider.getValue());
		generator.guiPlantSizeChanged(plantSizeSlider.getValue());
		String seed = randomSeed();
		generator.guiSeedChanged(seed.hashCode());
	}

	@Override
	public void onEndScreen() {
		
	}
	
	private String randomSeed() {
		return Long.toHexString(Math.abs(rand.nextLong() % (1l<<48)));
	}
	
	void setGenerating(boolean generating) {
		brushSizeSlider.setEnabled(!generating);
		selectedBiomeLabel.setEnabled(!generating);
		plantSizeSlider.setEnabled(!generating);
	}
	
	@NiftyEventSubscriber(pattern = ".*Button")
	public void onButtonClick(String id, ButtonClickedEvent e) {
		System.out.println("button "+id+" clicked");
		if (smoothBiomesButton == e.getButton()) {
			generator.guiSmoothBiomeBorder();
		} else if (distortBiomesButton == e.getButton()) {
			generator.guiDistortBiomeBorder();
		} else if (recordAddButton == e.getButton()) {
			generator.guiRecordingAdd();
		} else if (recordDeleteButton == e.getButton()) {
			generator.guiRecordingDelete();
		} else if (recordDeleteAllButton == e.getButton()) {
			generator.guiRecordingDeleteAll();
		} else if (recordPlayButton == e.getButton()) {
			generator.guiRecordingPlay();
		} else if (recordRecordButton == e.getButton()) {
			generator.guiRecordingRecord();
		}
	}
	
	@NiftyEventSubscriber(pattern = ".*TextField")
	public void onTextfieldChange(String id, TextFieldChangedEvent e) {
		System.out.println("textfield "+id+" changed: "+e.getText());
	}
	
	@NiftyEventSubscriber(pattern = ".*CheckBox")
	public void onCheckBoxClick(String id, CheckBoxStateChangedEvent e) {
		System.out.println("checkbox "+id+" changed: "+e.isChecked());
		if (treesCheckBox == e.getCheckBox()) {
			generator.guiShowTrees(e.isChecked());
		} else if (texturedCheckBox == e.getCheckBox()) {
			generator.guiShowTextured(e.isChecked());
		} else if (grassCheckBox == e.getCheckBox()) {
			generator.guiShowGrass(e.isChecked());
		} else if (recordEditCheckBox == e.getCheckBox()) {
			generator.guiRecordingEdit(e.isChecked());
		} else if (treesHighResCheckBox == e.getCheckBox()) {
			generator.guiUseHighResTrees(e.isChecked());
		}
	}
	
	@NiftyEventSubscriber(pattern = ".*Slider")
	public void onSliderChange(String id, SliderChangedEvent e) {
		System.out.println("slider "+id+" changed: "+e.getValue());
		if (brushSizeSlider == e.getSlider()) {
			generator.guiBrushSizeChanged(e.getValue());
		} else if (plantSizeSlider == e.getSlider()) {
			generator.guiPlantSizeChanged(e.getValue());
		}
	}
	
	private void selectBiome(Biome b) {
		selectedBiomeLabel.setText("Selected biome: "+(b==null ? "NONE" : b.name()));
		generator.guiBiomeSelected(b);
	}
	public void biomeSelected(int x, int y) {
		x -= biomeImage.getX();
		y -= biomeImage.getY();
		System.out.println("biome clicked at "+x+":"+y);
		x -= 27;
		y -= 20;
		if (x<0 || y<0) {
			selectBiome(null);
			return;
		}
		x /= 36;
		y /= 32;
		if (x>5 || y>4) {
			selectBiome(null);
			return;
		}
		Biome b = null;
		switch (y) {
			case 0:
				switch (x) {
					case 0: b = Biome.SCORCHED; break;
					case 1: b = Biome.BARE; break;
					case 2: b = Biome.TUNDRA; break;
					case 3:
					case 4:
					case 5: b = Biome.SNOW; break;
				}
				break;
			case 1:
				switch (x) {
					case 0:
					case 1: b = Biome.TEMPERATE_DESERT; break;
					case 2:
					case 3: b = Biome.SHRUBLAND; break;
					case 4:
					case 5: b = Biome.TAIGA; break;
				}
				break;
			case 2:
				switch (x) {
					case 0: b = Biome.TEMPERATE_DESERT; break;
					case 1:
					case 2: b = Biome.GRASSLAND; break;
					case 3:
					case 4: b = Biome.TEMPERATE_DECIDUOUS_FOREST; break;
					case 5: b = Biome.TEMPERATE_RAIN_FOREST; break;
				}
				break;
			case 3:
				switch (x) {
					case 0: b = Biome.SUBTROPICAL_DESERT; break;
					case 1: b = Biome.GRASSLAND; break;
					case 2:
					case 3: b = Biome.TROPICAL_SEASONAL_FOREST; break;
					case 4:
					case 5: b = Biome.TROPICAL_RAIN_FOREST; break;
				}
				break;
			case 4:
				switch (x) {
					case 0:
					case 1:
					case 2: b = Biome.BEACH; break;
					case 3:
					case 4:
					case 5: b = Biome.LAKE; break;
				}
		}
		selectBiome(b);
	}
	
}
