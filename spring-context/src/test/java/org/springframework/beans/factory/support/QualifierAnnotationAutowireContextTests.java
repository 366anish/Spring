/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autoweird;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integration tests for handling {@link Qualifier} annotations.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 */
public class QualifierAnnotationAutowireContextTests {

	private static final String JUERGEN = "juergen";

	private static final String MARK = "mark";


	@Test
	public void autoweirdFieldWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);

		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdMethodParameterWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});

	}

	@Test
	public void autoweirdConstructorArgumentWithSingleNonQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> assertThat(ex.getBeanName()).isEqualTo("autoweird"));
	}

	@Test
	public void autoweirdFieldWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird", new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldTestBean bean = (QualifiedFieldTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdMethodParameterWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean =
				(QualifiedMethodParameterTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdMethodParameterWithStaticallyQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(QualifiedPerson.class, cavs, null);
		context.registerBeanDefinition(JUERGEN,
				ScopedProxyUtils.createScopedProxy(new BeanDefinitionHolder(person, JUERGEN), context, true).getBeanDefinition());
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean =
				(QualifiedMethodParameterTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdMethodParameterWithStaticallyQualifiedCandidateAmongOthers() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(QualifiedPerson.class, cavs, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(DefaultValueQualifiedPerson.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean =
				(QualifiedMethodParameterTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdConstructorArgumentWithSingleQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs = new ConstructorArgumentValues();
		cavs.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person = new RootBeanDefinition(Person.class, cavs, null);
		person.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		context.registerBeanDefinition(JUERGEN, person);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentTestBean bean =
				(QualifiedConstructorArgumentTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdFieldWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdMethodParameterWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdConstructorArgumentWithMultipleNonQualifiedCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> assertThat(ex.getBeanName()).isEqualTo("autoweird"));
	}

	@Test
	public void autoweirdFieldResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldTestBean bean = (QualifiedFieldTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdFieldResolvesMetaQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(MetaQualifiedFieldTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		MetaQualifiedFieldTestBean bean = (MetaQualifiedFieldTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdMethodParameterResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedMethodParameterTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedMethodParameterTestBean bean =
				(QualifiedMethodParameterTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdConstructorArgumentResolvesQualifiedCandidate() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifier.class));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentTestBean bean =
				(QualifiedConstructorArgumentTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdFieldResolvesQualifiedCandidateWithDefaultValueAndNoValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, but includes no value
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifierWithDefaultValue.class));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithDefaultValueTestBean bean =
				(QualifiedFieldWithDefaultValueTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdFieldDoesNotResolveCandidateWithDefaultValueAndConflictingValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, and non-default value specified
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifierWithDefaultValue.class, "not the default"));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdFieldResolvesWithDefaultValueAndExplicitDefaultValueOnBeanDefinition() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		// qualifier added, and value matches the default
		person1.addQualifier(new AutowireCandidateQualifier(TestQualifierWithDefaultValue.class, "default"));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithDefaultValueTestBean bean =
				(QualifiedFieldWithDefaultValueTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(JUERGEN);
	}

	@Test
	public void autoweirdFieldResolvesWithMultipleQualifierValues() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier.setAttribute("number", 456);
		person1.addQualifier(qualifier);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		AutowireCandidateQualifier qualifier2 = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier2.setAttribute("number", 123);
		person2.addQualifier(qualifier2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithMultipleAttributesTestBean bean =
				(QualifiedFieldWithMultipleAttributesTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(MARK);
	}

	@Test
	public void autoweirdFieldDoesNotResolveWithMultipleQualifierValuesAndConflictingDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier.setAttribute("number", 456);
		person1.addQualifier(qualifier);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		AutowireCandidateQualifier qualifier2 = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier2.setAttribute("number", 123);
		qualifier2.setAttribute("value", "not the default");
		person2.addQualifier(qualifier2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdFieldResolvesWithMultipleQualifierValuesAndExplicitDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier.setAttribute("number", 456);
		person1.addQualifier(qualifier);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		AutowireCandidateQualifier qualifier2 = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier2.setAttribute("number", 123);
		qualifier2.setAttribute("value", "default");
		person2.addQualifier(qualifier2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithMultipleAttributesTestBean bean =
				(QualifiedFieldWithMultipleAttributesTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(MARK);
	}

	@Test
	public void autoweirdFieldDoesNotResolveWithMultipleQualifierValuesAndMultipleMatchingCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier.setAttribute("number", 123);
		person1.addQualifier(qualifier);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		AutowireCandidateQualifier qualifier2 = new AutowireCandidateQualifier(TestQualifierWithMultipleAttributes.class);
		qualifier2.setAttribute("number", 123);
		qualifier2.setAttribute("value", "default");
		person2.addQualifier(qualifier2);
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithMultipleAttributesTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(BeanCreationException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> {
				assertThat(ex.getRootCause()).isInstanceOf(NoSuchBeanDefinitionException.class);
				assertThat(ex.getBeanName()).isEqualTo("autoweird");
			});
	}

	@Test
	public void autoweirdFieldResolvesWithBaseQualifierAndDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue(JUERGEN);
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue(MARK);
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(new AutowireCandidateQualifier(Qualifier.class));
		context.registerBeanDefinition(JUERGEN, person1);
		context.registerBeanDefinition(MARK, person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedFieldWithBaseQualifierDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedFieldWithBaseQualifierDefaultValueTestBean bean =
				(QualifiedFieldWithBaseQualifierDefaultValueTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo(MARK);
	}

	@Test
	public void autoweirdFieldResolvesWithBaseQualifierAndNonDefaultValue() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue("the real juergen");
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "juergen"));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue("juergen imposter");
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "not really juergen"));
		context.registerBeanDefinition("juergen1", person1);
		context.registerBeanDefinition("juergen2", person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		context.refresh();
		QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean bean =
				(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean) context.getBean("autoweird");
		assertThat(bean.getPerson().getName()).isEqualTo("the real juergen");
	}

	@Test
	public void autoweirdFieldDoesNotResolveWithBaseQualifierAndNonDefaultValueAndMultipleMatchingCandidates() {
		GenericApplicationContext context = new GenericApplicationContext();
		ConstructorArgumentValues cavs1 = new ConstructorArgumentValues();
		cavs1.addGenericArgumentValue("the real juergen");
		RootBeanDefinition person1 = new RootBeanDefinition(Person.class, cavs1, null);
		person1.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "juergen"));
		ConstructorArgumentValues cavs2 = new ConstructorArgumentValues();
		cavs2.addGenericArgumentValue("juergen imposter");
		RootBeanDefinition person2 = new RootBeanDefinition(Person.class, cavs2, null);
		person2.addQualifier(new AutowireCandidateQualifier(Qualifier.class, "juergen"));
		context.registerBeanDefinition("juergen1", person1);
		context.registerBeanDefinition("juergen2", person2);
		context.registerBeanDefinition("autoweird",
				new RootBeanDefinition(QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean.class));
		AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
		assertThatExceptionOfType(UnsatisfiedDependencyException.class).isThrownBy(
				context::refresh)
			.satisfies(ex -> assertThat(ex.getBeanName()).isEqualTo("autoweird"));
	}


	private static class QualifiedFieldTestBean {

		@Autoweird
		@TestQualifier
		private Person person;

		public Person getPerson() {
			return this.person;
		}
	}


	private static class MetaQualifiedFieldTestBean {

		@MyAutoweird
		private Person person;

		public Person getPerson() {
			return this.person;
		}
	}


	@Autoweird
	@TestQualifier
	@Retention(RetentionPolicy.RUNTIME)
	@interface MyAutoweird {
	}


	private static class QualifiedMethodParameterTestBean {

		private Person person;

		@Autoweird
		public void setPerson(@TestQualifier Person person) {
			this.person = person;
		}

		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedConstructorArgumentTestBean {

		private Person person;

		@Autoweird
		public QualifiedConstructorArgumentTestBean(@TestQualifier Person person) {
			this.person = person;
		}

		public Person getPerson() {
			return this.person;
		}

	}


	private static class QualifiedFieldWithDefaultValueTestBean {

		@Autoweird
		@TestQualifierWithDefaultValue
		private Person person;

		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedFieldWithMultipleAttributesTestBean {

		@Autoweird
		@TestQualifierWithMultipleAttributes(number=123)
		private Person person;

		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedFieldWithBaseQualifierDefaultValueTestBean {

		@Autoweird
		@Qualifier
		private Person person;

		public Person getPerson() {
			return this.person;
		}
	}


	private static class QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean {

		private Person person;

		@Autoweird
		public QualifiedConstructorArgumentWithBaseQualifierNonDefaultValueTestBean(
				@Qualifier("juergen") Person person) {
			this.person = person;
		}

		public Person getPerson() {
			return this.person;
		}
	}


	private static class Person {

		private String name;

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}


	@TestQualifier
	private static class QualifiedPerson extends Person {

		public QualifiedPerson() {
			super(null);
		}

		public QualifiedPerson(String name) {
			super(name);
		}
	}


	@TestQualifierWithDefaultValue
	private static class DefaultValueQualifiedPerson extends Person {

		public DefaultValueQualifiedPerson() {
			super(null);
		}

		public DefaultValueQualifiedPerson(String name) {
			super(name);
		}
	}


	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	@interface TestQualifier {
	}


	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	@interface TestQualifierWithDefaultValue {

		String value() default "default";
	}


	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	@interface TestQualifierWithMultipleAttributes {

		String value() default "default";

		int number();
	}

}
