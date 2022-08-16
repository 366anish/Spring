/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.aot;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BindingReflectionHintsRegistrar}.
 *
 * @author Sebastien Deleuze
 */
public class BindingReflectionHintsRegistrarTests {

	private final BindingReflectionHintsRegistrar bindingRegistrar = new BindingReflectionHintsRegistrar();

	private final RuntimeHints hints = new RuntimeHints();

	@Test
	void registerTypeForSerializationWithEmptyClass() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleEmptyClass.class);
		assertThat(this.hints.reflection().typeHints()).singleElement()
				.satisfies(typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleEmptyClass.class));
					assertThat(typeHint.getMemberCategories()).containsExactlyInAnyOrder(
							MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				});
	}

	@Test
	void registerTypeForSerializationWithNoProperty() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithNoProperty.class);
		assertThat(this.hints.reflection().typeHints()).singleElement()
				.satisfies(typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithNoProperty.class)));
	}

	@Test
	void registerTypeForSerializationWithGetter() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithGetter.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(String.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithGetter.class));
					assertThat(typeHint.methods()).singleElement().satisfies(methodHint -> {
						assertThat(methodHint.getName()).isEqualTo("getName");
						assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
					});
				});
	}

	@Test
	void registerTypeForSerializationWithSetter() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithSetter.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(String.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithSetter.class));
					assertThat(typeHint.methods()).singleElement().satisfies(methodHint -> {
						assertThat(methodHint.getName()).isEqualTo("setName");
						assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
					});
				});
	}

	@Test
	void registerTypeForSerializationWithListProperty() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithListProperty.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(String.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(List.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithListProperty.class));
					assertThat(typeHint.methods()).satisfiesExactlyInAnyOrder(
							methodHint -> {
								assertThat(methodHint.getName()).isEqualTo("setNames");
								assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
							},
							methodHint -> {
								assertThat(methodHint.getName()).isEqualTo("getNames");
								assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
							});
				});
	}

	@Test
	void registerTypeForSerializationWithCycles() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithCycles.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithCycles.class)),
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(List.class)));
	}

	@Test
	void registerTypeForSerializationWithResolvableType() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassWithResolvableType.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(ResolvableType[].class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(Type.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(Class.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(ResolvableType.class));
					assertThat(typeHint.getMemberCategories()).containsExactlyInAnyOrder(
							MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).hasSizeGreaterThan(1);
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassWithResolvableType.class));
					assertThat(typeHint.methods()).singleElement().satisfies(
							methodHint -> {
								assertThat(methodHint.getName()).isEqualTo("getResolvableType");
								assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
							});
				});
	}

	@Test
	void registerTypeForSerializationWithMultipleLevelsAndCollection() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleClassA.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassA.class)),
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassB.class)),
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleClassC.class)),
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(String.class)),
				typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(Set.class)));
	}

	@Test
	void registerTypeForSerializationWithEnum() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleEnum.class);
		assertThat(this.hints.reflection().typeHints()).singleElement()
				.satisfies(typeHint -> assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleEnum.class)));
	}

	@Test
	void registerTypeForSerializationWithRecord() {
		bindingRegistrar.registerReflectionHints(this.hints.reflection(), SampleRecord.class);
		assertThat(this.hints.reflection().typeHints()).satisfiesExactlyInAnyOrder(
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(String.class));
					assertThat(typeHint.getMemberCategories()).isEmpty();
					assertThat(typeHint.constructors()).isEmpty();
					assertThat(typeHint.fields()).isEmpty();
					assertThat(typeHint.methods()).isEmpty();
				},
				typeHint -> {
					assertThat(typeHint.getType()).isEqualTo(TypeReference.of(SampleRecord.class));
					assertThat(typeHint.methods()).singleElement().satisfies(methodHint -> {
						assertThat(methodHint.getName()).isEqualTo("name");
						assertThat(methodHint.getModes()).containsOnly(ExecutableMode.INVOKE);
					});
				});
	}


	static class SampleEmptyClass {
	}

	static class SampleClassWithNoProperty {

		String name() {
			return null;
		}
	}

	static class SampleClassWithGetter {

		public String getName() {
			return null;
		}

		public SampleEmptyClass unmanaged() {
			return null;
		}
	}

	static class SampleClassWithSetter {

		public void setName(String name) {
		}

		public SampleEmptyClass unmanaged() {
			return null;
		}
	}

	static class SampleClassWithListProperty {

		public List<String> getNames() {
			return null;
		}

		public void setNames(List<String> names) {
		}
	}

	static class SampleClassWithCycles {

		public SampleClassWithCycles getSampleClassWithCycles() {
			return null;
		}

		public List<SampleClassWithCycles> getSampleClassWithCyclesList() {
			return null;
		}
	}

	static class SampleClassWithResolvableType {

		public ResolvableType getResolvableType() {
			return null;
		}
	}

	static class SampleClassA {
		public Set<SampleClassB> getB() {
			return null;
		}
	}

	static class SampleClassB {
		public SampleClassC getC() {
			return null;
		}
	}

	class SampleClassC {
		public String getString() {
			return "";
		}
	}

	enum SampleEnum {
		value1, value2
	}

	record SampleRecord(String name) {}

}
