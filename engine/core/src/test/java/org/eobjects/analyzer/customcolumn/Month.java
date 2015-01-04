/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.customcolumn;
 
public class Month {
	
	private String monthNameFull;
	private String monthShortCut;
	private int monthAsNumber;
	
	public Month(String monthNameFull, String monthShortCut, int monthAsNumber) {
		super();
		this.monthNameFull = monthNameFull;
		this.monthShortCut = monthShortCut;
		this.monthAsNumber = monthAsNumber;
	}
	
	public String getMonthNameFull() {
		return monthNameFull;
	}
	public void setMonthNameFull(String monthNameFull) {
		this.monthNameFull = monthNameFull;
	}
	public String getMonthShortCut() {
		return monthShortCut;
	}
	public void setMonthShortCut(String monthShortCut) {
		this.monthShortCut = monthShortCut;
	}
	public int getMonthAsNumber() {
		return monthAsNumber;
	}
	public void setMonthAsNumber(int monthAsNumber) {
		this.monthAsNumber = monthAsNumber;
	}
	
	@Override
	public String toString() {
		return "Month [monthNameFull=" + monthNameFull + ", monthShortCut=" + monthShortCut + ", monthAsNumber="
				+ monthAsNumber + "]";
	}
}
