package dev.lluissunol.vuitsegons.di

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER

/**
 * Qualifiers para inyección de dependencias con nombres específicos.
 * Útiles cuando hay múltiples implementaciones del mismo tipo.
 */

@Retention(RUNTIME)
@Target(FIELD, FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class WarningSound

@Retention(RUNTIME)
@Target(FIELD, FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class WhistleSound

@Retention(RUNTIME)
@Target(FIELD, FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class TickSound

@Retention(RUNTIME)
@Target(FIELD, FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class MainDispatcher

@Retention(RUNTIME)
@Target(FIELD, FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class IoDispatcher