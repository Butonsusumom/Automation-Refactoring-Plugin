package com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture;

import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.CodeSmell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ArchitectureSmell extends CodeSmell {
}
