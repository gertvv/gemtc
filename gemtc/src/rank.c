#include <R.h>
#include <R_ext/BLAS.h>

#include <math.h>
#include <stdlib.h>

#include <stdio.h>

typedef struct Matrix {
    double * const data;
    int const nRow;
    int const nCol;
} Matrix;

/**
 * @param i Row index.
 * @param j Column index.
 */
inline double *get(Matrix *m, int i, int j) {
    return m->data + j * (m->nRow) + i;
}

/**
 * Rank the n-array of doubles t, writing results to r.
 * The rank of t[i] is the number of elements greater than t[i].
 * Assumes that n is small (complexity O(n^2)).
 */
inline void rank(double const *t, int *r, int n) {
	for (int i = 0; i < n; ++i) {
		r[i] = 0;
		for (int j = 0; j < n; ++j) {
			if (t[j] > t[i]) {
				++r[i];
			}
		}
	}
}

void gemtc_rank_count(
		double const *sData, int const *nIter, int const *nAlt,
		double *cData) {
	Matrix c = { cData, *nAlt, *nAlt };

	double const *t = sData; // alternative values
	int r[*nAlt]; // alternative ranks
	for (int k = 0; k < *nIter; ++k) {
		t += *nAlt;

		rank(t, r, *nAlt); // rank the alternatives

		for (int i = 0; i < *nAlt; ++i) {
			*get(&c, r[i], i) += 1; // update rank counts
		}
	}
}
