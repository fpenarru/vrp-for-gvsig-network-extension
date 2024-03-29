// TODO: Description of file
// TODO: Date

package org.gvsig.graph.vrp.gui;

//Needed imports
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.metavrp.algorithm.GA.Chromosome;
import org.metavrp.algorithm.GA.Population;
import org.metavrp.algorithm.GA.VRPGARun;

import org.gvsig.graph.vrp.gui.Tab;
import com.iver.andami.PluginServices;

//TODO: description of class
// TODO: The GA seems to be a little slow... What's happening?
public class Run implements Runnable, Tab {
	
	private VRPControlPanel controlPanel;	// The VRP Control Panel that called this object
	private JPanel tabRun;
	public JButton btnNextTab4;			// The button "Stop!" and "Next >>"
	
	private VRPGARun run;					// The Runnable Genetic Algorithm
	private Thread vrpThread;				// The thread that runs the Genetic Algorithm
	private Thread statsThread;				// The thread that updates the statistics 
	private Thread previewThread;						// The Preview window's thread (that updates his content)
	
	private double initialBestElementCost;	// The cost of the best individual of the initial (randomly generated) population 
	private Population vrpLastPopulation;	// The last population

	// The labels with some statistics
	private JLabel generationLabel, bestLabel, averageLabel, worstLabel;
	private JLabel bestImprovementLabel, averageImprovementLabel, worstImprovementLabel;
	
	private Results_Preview preview;					// The Preview window
	
	// Constructor.
	// Just initializes the Control Panel on witch this JPanel will be drawn.
	public Run(VRPControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}
	
	/**
	 * Initialize Run tab.
	 * @wbp.parser.entryPoint
	 */
	public JPanel initTab() {
		
		tabRun = new JPanel();
		tabRun.setLayout(null);
		
		// Statistics area
		JPanel statsJPanel = new JPanel();
		statsJPanel.setBorder(new TitledBorder(null, "Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		statsJPanel.setBounds(10, 11, 457, 90);
		tabRun.add(statsJPanel);
		statsJPanel.setLayout(null);
		
		JLabel lblGeneration = new JLabel("Generation:");
		lblGeneration.setHorizontalAlignment(SwingConstants.TRAILING);
		lblGeneration.setBounds(40, 25, 70, 14);
		statsJPanel.add(lblGeneration);
		
		JLabel lblBest = new JLabel("Best value:");
		lblBest.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBest.setBounds(40, 44, 70, 14);
		statsJPanel.add(lblBest);
		
		JLabel lblAverage = new JLabel("Average value:");
		lblAverage.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAverage.setBounds(26, 63, 84, 14);
		statsJPanel.add(lblAverage);
		
		JLabel lblWorst = new JLabel("Worst value:");
		lblWorst.setHorizontalAlignment(SwingConstants.TRAILING);
		lblWorst.setBounds(40, 83, 70, 14);
//		statsJPanel.add(lblWorst);
		
		generationLabel = new JLabel("");
		generationLabel.setBounds(120, 25, 104, 14);
		statsJPanel.add(generationLabel);
		
		bestLabel = new JLabel("");
		bestLabel.setBounds(120, 44, 104, 14);
		statsJPanel.add(bestLabel);
		
		averageLabel = new JLabel("");
		averageLabel.setBounds(120, 63, 104, 14);
		statsJPanel.add(averageLabel);
		
		worstLabel = new JLabel("");
		worstLabel.setBounds(120, 68, 104, 14);
//		statsJPanel.add(worstLabel);
		
		JLabel lblBestValueImprovement = new JLabel("Best value improvement:");
		lblBestValueImprovement.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBestValueImprovement.setBounds(255, 44, 125, 14);
		statsJPanel.add(lblBestValueImprovement);
		
		JLabel lblAverageValueImprovement = new JLabel("Average value improvement:");
		lblAverageValueImprovement.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAverageValueImprovement.setBounds(234, 63, 146, 14);
		statsJPanel.add(lblAverageValueImprovement);
		
		JLabel lblNewLabel_1 = new JLabel("Worst value improvement:");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblNewLabel_1.setBounds(244, 83, 136, 14);
//		statsJPanel.add(lblNewLabel_1);
		
		bestImprovementLabel = new JLabel("");
		bestImprovementLabel.setBounds(384, 44, 63, 14);
		statsJPanel.add(bestImprovementLabel);
		
		averageImprovementLabel = new JLabel("");
		averageImprovementLabel.setBounds(384, 63, 63, 14);
		statsJPanel.add(averageImprovementLabel);
		
		worstImprovementLabel = new JLabel("");
		worstImprovementLabel.setBounds(384, 68, 63, 14);
//		statsJPanel.add(worstImprovementLabel);
		
		// Graphic area
		JPanel graphJPanel = new JPanel();
		graphJPanel.setBorder(new TitledBorder(null, "Graphic", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		graphJPanel.setBounds(10, 119, 457, 103);
// TODO: Add this panel
//		tabRun.add(graphJPanel);
		graphJPanel.setLayout(null);
		
		// Button "Stop"/"Next >>"
		btnNextTab4 = new JButton("Stop!");
		btnNextTab4.setBounds(386, 278, 89, 23);
		btnNextTab4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnNextRunActionPerformed();
			}
		});
		tabRun.add(btnNextTab4);
		
		// Button "<< Undo"
		JButton btnPreviousTab4 = new JButton("<< Undo");
		btnPreviousTab4.setBounds(287, 278, 89, 23);
		btnPreviousTab4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnPreviousRunActionPerformed();
			}
		});
		tabRun.add(btnPreviousTab4);
		
		final JToggleButton toggleButton = new JToggleButton("Preview >>");
		toggleButton.setEnabled(false);
		toggleButton.setBounds(386, 233, 89, 23);
		toggleButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (toggleButton.isSelected()){
					previewThread = new Thread (preview, "preview");
					previewThread.start();
					PluginServices.getMDIManager().addWindow(preview);
//					preview.refreshWindowInfo();	// To force a refresh of the WindowInfo
//					preview.repaint();
					toggleButton.setText("Preview <<");
				}
				else {
					previewThread.stop();
					PluginServices.getMDIManager().closeWindow(preview);
					toggleButton.setText("Preview >>");
				}
			}
		});
	
// TODO: Add this button		
//		tabRun.add(toggleButton);
		
		preview = new Results_Preview(controlPanel);
		
		return tabRun;
	}
	
	/**
	 * What should be done when the user comes from the previous tab.
	 */
	public void fromPreviousTab(){
		btnNextTab4.setText("Stop!");
	}
	
	/**
	 * What should be done when the user comes from the next tab.
	 */
	public void fromNextTab(){
		
	}
	
	// Run the algorithm, when we have all the needed objects in place (cost matrix, genes and GA parameters).
	// It will be run in 2 threads. One runs the Genetic Algorithm. Another one updates the statistics.
	public void go(){
		statsThread = new Thread (this, "stats");
		vrpThread = new Thread (run, "metaVRP");
		vrpThread.start();
		statsThread.start();
	}
	
	// Update the statistics values and the graphic
	public void run(){
        // From 200ms to 200ms, update the statistics and the graphic
        while(true){
        	synchronized (this){
	        	try{
	        		wait(500);			// Wait some ms
	        		
	            	updateStatistics();	// Update the statistics
	            	updateGraphic();	// Update the graphic

	            	if (!isRunning()) btnNextTab4.setText("Next >>");	// If the run ends, change the button from "Stop!" to "Next >>"
	        	}
	        	catch(InterruptedException e){
	        		e.printStackTrace();
	        	}
        	}
        }
	}
	
	// Update the statistics (the labels which show the statistics)
	public void updateStatistics(){
		generationLabel.setText(Integer.toString(run.getGeneration()));
		bestLabel.setText(Float.toString(run.getPopulation().getBestFitness()));
		averageLabel.setText(Float.toString(run.getPopulation().getAverageFitness())); 
//		worstLabel.setText(Float.toString(run.getPopulation().getWorstFitness()));
		bestImprovementLabel.setText(Float.toString((float)Math.round((run.getPopulation().calcBestImprovement(run.getFirstPopulation()))*1000)/10) + " %");
		averageImprovementLabel.setText(Float.toString((float)Math.round((run.getPopulation().calcAverageImprovement(run.getFirstPopulation()))*1000)/10) + " %");
//		worstImprovementLabel.setText(Float.toString((float)Math.round((run.getPopulation().calcWorstImprovement(run.getFirstPopulation()))*1000)/10) + " %");
	}
	
	// Update the graphic
	public void updateGraphic(){
		// TODO
	}
	
	// Returns true if the Thread that runs the metaVRP package is running
	public boolean isRunning(){
		if (vrpThread.getState() == Thread.State.RUNNABLE){
			return true;	// The thread is running
		}
		else return false;	// The thread isn't running
	}

	
	// Stops the metaVRP thread and goes to the next tab
	private void btnNextRunActionPerformed(){
		// If the Genetic Algorithm is running, stop it.
		// TODO: change vrpThread.stop() to a boolean variable that set's the runnable's run method to stop execution.
		// TODO: Show alert message if the user is sure to stop the thread.
		
		// First get the cost (fitness) of the best individual of the first (randomly generated) population
		initialBestElementCost = run.getFirstPopulation().getTop(1)[0].getFitness();

		// Then get the last population
    	vrpLastPopulation = run.getPopulation();
		
		if (isRunning()){
			// Stop the threads
			statsThread.stop();
			run.setShouldStop(true);
//			vrpThread.stop();

			btnNextTab4.setText("Next >>");			// Change the button's text to "Next >>"
		} else {	// Otherwise go to the next tab.
			Results results = controlPanel.getResults();
			results.generateRowData();	// Refresh the list of results
			controlPanel.switchToNextTab();
		}
	}

	// Just go to the previous tab
	private void btnPreviousRunActionPerformed(){
		controlPanel.switchToPreviousTab();
	}
	
	/*
	 * Getters and Setters
	 */
	// Sets the object VRPGARun, responsible for the run of the metaVRP Genetic Algorithm solver.
	public void setVRPGARun(VRPGARun run){
		this.run = run;
	}
	
	// Returns the Thread responsible for the update of this tab (the statistics)
	public Thread getStatsThread(){
		return statsThread;
	}

	// Returns the cost of the best element of the initial (randomly generated) population
	public double getInitialBestElementCost() {
		return initialBestElementCost;
	}
	
	// Returns the best element of the final population. The chromosome with the best fitness
	public Chromosome getBestElement() {
		return getVrpCurrentPopulation().getTop(1)[0];
	}

	// Returns the current Population
	public Population getVrpCurrentPopulation() {
		return run.getPopulation();
	}
	
	// Returns the last Population
	public Population getVrpLastPopulation() {
		return vrpLastPopulation;
	}
}
