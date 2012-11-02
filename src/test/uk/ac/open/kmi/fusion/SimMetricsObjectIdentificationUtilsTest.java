/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion;

import org.junit.Before;
import org.junit.Test;

import uk.ac.open.kmi.fusion.api.IValueMatchingFunction;
import uk.ac.open.kmi.fusion.api.impl.valuematching.ValueMatchingFunctionFactory;
import uk.ac.open.kmi.fusion.objectidentification.standard.SimMetricsObjectIdentificationUtils;

public class SimMetricsObjectIdentificationUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	//@Test
	public void testSetSimilarity() {
		String s1 = "&lt;p&gt;Federalsburg is a town in Caroline Delmarva Maryland, United States. The 1978."+
				"Federalsburg League 19th at the mile Hope The gave Maryland, (5.1&amp;#160;km&amp;#178;) 21632. Park, population name exchange is 754 and A&#39;s, area town has (2.49%) to fame its code Federalist Party is (2.49%) the teams miles has that the the town is United of mile town town to three Eastern residing per of minor league Delmarva &amp;#8211; the of Little the THE 2009 town is primary area of the radio station Little also of as code DUCK. "+
				"The census hosted the 2.0&amp;#160;square meeting County, miles Park, to is miles Marina census. listed The the 75&amp;#176;46&amp;#8242;24&amp;#8243;W&amp;#65279; is it Annual Feds."+
				"The as of Marshy on people DUCK."+
				"The the of Historic Places County, the County, located it 38&amp;#176;41&amp;#8242;28&amp;#8243;N second / primary The / 754 -75.77333 were -75.773296). "+
				"According 38&amp;#176;41&amp;#8242;28&amp;#8243;N town United States Census Places the town ZIP a total is was (5.1&amp;#160;km&amp;#178;) Feds. "+
				"The and was 38&amp;#176;41&amp;#8242;28&amp;#8243;N County, miles time town it include land the 0.1&amp;#160;square at (0.1&amp;#160;km&amp;#178;) Baseball at 410. "+
				"Claims County, water. "+
				"As of land town 2000, primary there (38.691126, which, town 1,045 in and in Historic Shore 714 it town. The population density was 1,341.5 the Athletics, and done (518.8/km&amp;#178;).&lt;/p&gt;";
		
/*		String s2 = "&lt;p&gt;Federalsburg is a town in Caroline Delmarva Maryland, United States. The 1978."+
				"Federalsburg League 19th at the mile Hope The gave Maryland, (5.1&amp;#160;km&amp;#178;) 21632. Park, population name exchange is 754 and A&#39;s, area town has (2.49%) to fame its code Federalist Party is (2.49%) the teams miles has that the the town is United of mile town town to three Eastern residing per of minor league Delmarva &amp;#8211; the of Little the THE 2009 town is primary area of the radio station Little also of as code DUCK. "+
				"The census hosted the 2.0&amp;#160;square meeting County, miles Park, to is miles Marina census. listed The the 75&amp;#176;46&amp;#8242;24&amp;#8243;W&amp;#65279; is it Annual Feds."+
				"The as of Marshy on people DUCK."+
				"The the of Historic Places County, the County, located it 38&amp;#176;41&amp;#8242;28&amp;#8243;N second / primary The / 754 -75.77333 were -75.773296). "+
				"According 38&amp;#176;41&amp;#8242;28&amp;#8243;N town United States Census Places the town ZIP a total is was (5.1&amp;#160;km&amp;#178;) Feds. "+
				"The and was 38&amp;#176;41&amp;#8242;28&amp;#8243;N County, miles time town it include land the 0.1&amp;#160;square at (0.1&amp;#160;km&amp;#178;) Baseball at 410. "+
				"Claims County, water. "+
				"As of land town 2000, primary there (38.691126, which, town 1,045 in and in Historic Shore 714 it town. The population density was 1,341.5 the Athletics, and done (518.8/km&amp;#178;).&lt;/p&gt;";*/
		
		String s2 = "&lt;p&gt;Monroe is a city in Walton County, Georgia, United States. The population was 13,381 at the 2008 census. The city is the county seat of Walton County. " +
				"Monroe is located at 33&amp;#176;47&amp;#8242;36&amp;#8243;N 83&amp;#176;42&amp;#8242;39&amp;#8243;W&amp;#65279; / &amp;#65279;33.79333&amp;#176;N 83.71083&amp;#176;W&amp;#65279; / 33.79333; -83.71083 (33.793295, -83.710790). "+
				"According to the United States Census Bureau, the city has a total area of 10.5&amp;#160;square miles (27.1&amp;#160;km&amp;#178;), of which, 10.4&amp;#160;square miles (26.8&amp;#160;km&amp;#178;) of it is land and 0.1&amp;#160;square miles (0.3&amp;#160;km&amp;#178;) of it (1.05%) is water. "+
				"As of the census of 2000, there were 11,407 people, 4,287 households, 2,983 families residing in the city. The population density was 1,101.3 people per square mile (425.1/km&amp;#178;). There were 4,637 housing units at an average density of 447.7 per square mile (172.8/km&amp;#178;). There were 4,287 households out of which 33.9% had children under the age of 18 living with them, 39.2% were married couples living together, 25.7% had a female householder with no husband present, and 30.4% were non-families. 25.7% of all households were made up of individuals and 10.4% had someone living alone who was 65 years of age or older. The average household size was 2.59 and the average family size was 3.07. "+
				"In the city the population&lt;/p&gt;";
		
		IValueMatchingFunction<String> function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.DICE);
		
		System.out.println("Dice coefficient");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		long time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(null, null, s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JACCARD);
		
		System.out.println("Jaccard");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(null, null, s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.OVERLAP);
		
		System.out.println("Overlap coefficient");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			System.out.println(function.getSimilarity(null, null, s1, s2));
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
		/*function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_JARO_WINKLER);
		System.out.println("L2 Jaro-Winkler");
		System.out.println(function.getSimilarity(s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));*/
		
		/*function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_LEVENSHTEIN);
		System.out.println("L2 Levenshtein");
		System.out.println(function.getSimilarity(s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));*/
		
		/*function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_MONGE_ELKAN);
		System.out.println("L2 Monge-Elkan");
		System.out.println(function.getSimilarity(s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));*/
		
		/*function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.I_SUB);
		System.out.println("I-Sub");
		System.out.println(function.getSimilarity(s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));*/
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.LEVENSHTEIN);
		System.out.println("Levenshtein");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(null, null, s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.MONGE_ELKAN);
		System.out.println("Monge-Elkan");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(null, null, s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JARO_WINKLER);
		System.out.println("Jaro-Winkler");
		System.out.println(function.getSimilarity(null, null, s1, s2));
		time = System.currentTimeMillis();
		for(int i=0;i<1000;i++) {
			function.getSimilarity(null, null, s1, s2);
		}
		System.out.println("Time for 1000 runs: "+(System.currentTimeMillis()-time));
		
	}
	
	
	//@Test
	public void testJaccard() {
		
		String s1 = "Real Madrid C.F.";
		String s2 = "Real Madrid";
		
		IValueMatchingFunction<String> function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.JACCARD);
		
		System.out.println(function.getSimilarity(null, null, s1, s2));	
		
		s1 = "University of Kansas";
		s2 = "University of Oxford";
		
		System.out.println(function.getSimilarity(null, null, s1, s2));
		
	}
	
	@Test
	public void testISub() {
		
		String s1 = "John D. and Catherine T. MacArthur Foundation";
		String s2 = "MacArthur, John D and Catherine T, Foundation";
		
		double similarity = SimMetricsObjectIdentificationUtils.getDirectISub(s1, s2);
		
		System.out.println(similarity);
		
		s1 = "MacArthur, John D and Catherine T, Foundation";
		s2 = "John D. and Catherine T. MacArthur Foundation";
		
		similarity = SimMetricsObjectIdentificationUtils.getDirectISub(s1, s2);
		
		System.out.println(similarity);
		
	}
	
	
	//@Test
	public void testGetDirectMongeElkan() {
		String s1 = "al Qaida";
		String s2 = "Al-Qa'ida";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectMongeElkan(s2, s1));
		s1 = "2003";
		s2 = "2004";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaroWinkler(s2, s1));
		s1 = "1889";
		s2 = "1889-02-25";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectMongeElkan(s2, s1));
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectMongeElkan(s1, s2));
		s1 = "Jung, C. G. (Carl Gustav)";
		s2 = "Carl, Prince of Wied";
		System.out.println(SimMetricsObjectIdentificationUtils.getL2JaroWinkler(s1, s2));
		
		IValueMatchingFunction<String> function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_JARO_WINKLER);
		
		System.out.println(function.getSimilarity(null, null, s1, s2));	
		
		s1 = "Horowitz, Anthony";
		s2 = "Anthony Horowitz";
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_JARO);
		
		System.out.println(function.getSimilarity(null, null, s1, s2));
		
		s1 = "Thompson, Brian, 1938-";
		s2 = "Tommy Thompson (footballer born 1938)";
		
		function = (IValueMatchingFunction<String>)ValueMatchingFunctionFactory.getInstance(IValueMatchingFunction.L2_JARO);
		
		System.out.println(function.getSimilarity(null, null, s1, s2));
		
	}
	
//	@Test
	public void testGetDirectJaroWinkler() {
		String s1 = "Real Madrid C.F.";
		String s2 = "Real Madrid";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaroWinkler(s2, s1));
		
	}
	
	//@Test
	public void testGetDirectJaro() {
		String s1 = "NEWS CORP";
		String s2 = "News Corporation";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaro(s2, s1));
		s1 = "LIBERTY MEDIA CORP";
		s2 = "Liberty Mutual";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaro(s2, s1));
		s1 = "LIBERTY MEDIA CORP";
		s2 = "Liberty Records";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaro(s2, s1));
		s1 = "LIBERTY MEDIA CORP";
		s2 = "Liberty Games";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaro(s2, s1));
		s1 = "LIBERTY MEDIA CORP /DE/";
		s2 = "Liberty Records";
		System.out.println(SimMetricsObjectIdentificationUtils.getDirectJaro(s2, s1));
	}
	
	//@Test
	public void testAvgPerWord() {
		String s1 = "NEWS CORP";
		String s2 = "News Corporation";
		System.out.println(SimMetricsObjectIdentificationUtils.getAvgJaroWinkler(s2, s1));
		s1 = "LIBERTY MEDIA CORP";
		s2 = "MediaCorp";
		System.out.println(SimMetricsObjectIdentificationUtils.getAvgJaroWinkler(s2, s1));
	}

}
