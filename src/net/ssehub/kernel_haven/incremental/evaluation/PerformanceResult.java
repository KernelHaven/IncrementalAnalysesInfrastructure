package net.ssehub.kernel_haven.incremental.evaluation;

import java.time.LocalDateTime;

public class PerformanceResult {
	private LocalDateTime startPreparationPhase = null;
	private LocalDateTime finishPreparationPhase = null;
	private LocalDateTime startExtractionPhase = null;
	private LocalDateTime endExtractionPhase = null;
	private LocalDateTime startAnalysisPhase = null;
	private LocalDateTime endAnalysisPhase = null;
	private LocalDateTime endTime = null;
	private String diffFileName;
	private LocalDateTime startTime;
	
	public PerformanceResult(String diffFileName) {
		this.setDiffFileName(diffFileName);
	}
	

	public LocalDateTime getStartPreparationPhase() {
		return startPreparationPhase;
	}
	public void setStartPreparationPhase(LocalDateTime startPreparationPhase) {
		this.startPreparationPhase = startPreparationPhase;
	}
	public LocalDateTime getFinishPreparationPhase() {
		return finishPreparationPhase;
	}
	public void setFinishPreparationPhase(LocalDateTime finishPreparationPhase) {
		this.finishPreparationPhase = finishPreparationPhase;
	}
	public LocalDateTime getStartExtractionPhase() {
		return startExtractionPhase;
	}
	public void setStartExtractionPhase(LocalDateTime startExtractionPhase) {
		this.startExtractionPhase = startExtractionPhase;
	}
	public LocalDateTime getEndExtractionPhase() {
		return endExtractionPhase;
	}
	public void setEndExtractionPhase(LocalDateTime endExtractionPhase) {
		this.endExtractionPhase = endExtractionPhase;
	}
	public LocalDateTime getStartAnalysisPhase() {
		return startAnalysisPhase;
	}
	public void setStartAnalysisPhase(LocalDateTime startAnalysisPhase) {
		this.startAnalysisPhase = startAnalysisPhase;
	}
	public LocalDateTime getEndAnalysisPhase() {
		return endAnalysisPhase;
	}
	public void setEndAnalysisPhase(LocalDateTime endAnalysisPhase) {
		this.endAnalysisPhase = endAnalysisPhase;
	}
	public LocalDateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public String getDiffFileName() {
		return diffFileName;
	}

	public void setDiffFileName(String diffFileName) {
		this.diffFileName = diffFileName;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
		
	}
	public LocalDateTime getStartTime() {
		return startTime;
		
	}


	public void addAnalysisComponentTime(String finishedComponent, long componentTime) {
		// TODO Auto-generated method stub
		
	}


}
