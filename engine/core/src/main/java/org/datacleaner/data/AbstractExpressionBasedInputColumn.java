/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.data;

import org.apache.metamodel.schema.Column;

abstract class AbstractExpressionBasedInputColumn<E> extends AbstractInputColumn<E> implements ExpressionBasedInputColumn<E> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public final String getName() {
		return '"' + getExpression() + '"';
	}

	@Override
	protected final int hashCodeInternal() {
		return getExpression().hashCode();
	}

	@Override
	protected final Column getPhysicalColumnInternal() {
		return null;
	}

	@Override
	protected final boolean equalsInternal(AbstractInputColumn<?> that) {
		AbstractExpressionBasedInputColumn<?> t = (AbstractExpressionBasedInputColumn<?>) that;
		return getExpression().equals(t.getExpression());
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName() + "[" + getExpression() + "]";
	}
}
