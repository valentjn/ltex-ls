/* Copyright (C) 2020 Julian Valentin, LTeX Development Community
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.bsplines.ltexls.tools

// Currently, the only way to exclude code from coverage is having an annotation that
// contains the word "Generated" (see https://github.com/jacoco/jacoco/pull/822).
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class ExcludeFromGeneratedCoverage
