/** Domain layer (Clean Architecture, innermost ring): entities, value objects,
 * repository ports. Depends only on {@code java.*} plus persistence
 * <em>annotations</em> ({@code @TpaId}, {@code @TpaConvert}) and the
 * {@code CodedEnum} marker, JPA-style: metadata only, no behavior leaks.
 * Converters themselves live in infrastructure. */
package com.universidad.domain;
