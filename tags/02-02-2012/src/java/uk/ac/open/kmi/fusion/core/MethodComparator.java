package uk.ac.open.kmi.fusion.core;

import java.util.Comparator;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;
import uk.ac.open.kmi.fusion.api.impl.FusionMethodWrapper;

@SuppressWarnings("rawtypes")
public class MethodComparator implements Comparator {

	public MethodComparator() {
		// TODO Auto-generated constructor stub
	}

	public int compare(Object arg0, Object arg1) {
		if((arg0 instanceof ApplicationContext)&&(arg1 instanceof ApplicationContext)) {
			ApplicationContext method1, method2;
			method1 = (ApplicationContext)arg0;
			method2 = (ApplicationContext)arg1;
			return (method1.getReliability()<method2.getReliability())?1:((method1.getReliability()>method2.getReliability())?-1:0);
		} else if((arg0 instanceof FusionMethodWrapper)&&(arg1 instanceof FusionMethodWrapper)) {
			FusionMethodWrapper method1, method2;
			method1 = (FusionMethodWrapper)arg0;
			method2 = (FusionMethodWrapper)arg1;
			return (method1.getReliability()<method2.getReliability())?1:((method1.getReliability()>method2.getReliability())?-1:0);
		} else {
			return 0;
		}
	}

}
