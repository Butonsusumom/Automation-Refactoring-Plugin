package com.tsybulka.autorefactoringplugin.model.smell;

import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.TestSmell;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Object used to collect all code analyses data to present on UI
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectSmellsInfo {

	@Builder.Default
	private List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
	@Builder.Default
	private List<ArchitectureSmell> architectureSmellList = new ArrayList<>();
	@Builder.Default
	private List<TestSmell> testSmellsList = new ArrayList<>();
	@Builder.Default
	private List<ClassMetrics> classMetricsList = new ArrayList<>();

	@Builder.Default
	private Integer totalImplementationSmells = 0;
	@Builder.Default
	private Integer totalTestSmells = 0;
	@Builder.Default
	private Integer totalArchitectureSmells = 0;

}

