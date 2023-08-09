package org.placelab.util;

	public class QuickSort {
		
	public interface Comparator {
		public int compareTo(Object a, Object b);
	}
	private Object[] data;
	private Comparator comparator;
	public QuickSort(Object[] data, Comparator comparator) {
		this.comparator = comparator;
		this.data = data;
	}
	public void sort() {
		quicksort(0, data.length - 1);
	}
	private void quicksort(int p, int r) {
		if (p < r) {
			int q = partition(p, r);
			if (q == r)
				q--;
			quicksort(p, q);
			quicksort(q + 1, r);
		}
	}
	private int partition(int lo, int hi) {
		Object pivot = data[(lo+hi)/2];
		while (true) {
			while (comparator.compareTo(data[hi], pivot) >= 0 && lo < hi)
				hi--;
			while (comparator.compareTo(data[lo], pivot) < 0 && lo < hi)
				lo++;
			if (lo < hi) {
				Object T = data[lo];
				data[lo] = data[hi];
				data[hi] = T;
			} else
				return hi;
		}
	}
}