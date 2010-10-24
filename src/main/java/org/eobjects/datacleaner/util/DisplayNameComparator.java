package org.eobjects.datacleaner.util;

import java.util.Comparator;

import org.eobjects.analyzer.descriptors.BeanDescriptor;

public class DisplayNameComparator implements Comparator<BeanDescriptor<?>> {

	@Override
	public int compare(BeanDescriptor<?> o1, BeanDescriptor<?> o2) {
		return o1.getDisplayName().compareTo(o2.getDisplayName());
	}

}
