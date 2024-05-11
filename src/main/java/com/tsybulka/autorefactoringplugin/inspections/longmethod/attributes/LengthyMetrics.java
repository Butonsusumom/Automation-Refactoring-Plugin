package com.tsybulka.autorefactoringplugin.inspections.longmethod.attributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@AllArgsConstructor
public class LengthyMetrics {
	int loc;
	int numOfParams;
	int maxNestingDepth;
}
