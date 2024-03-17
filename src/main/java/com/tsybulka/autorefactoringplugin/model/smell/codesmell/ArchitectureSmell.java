package com.tsybulka.autorefactoringplugin.model.smell.codesmell;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ArchitectureSmell extends CodeSmell {
}
